package com.example.spendsense20

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendsense20.databinding.CameraPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraPage : AppCompatActivity() {

    private lateinit var binding: CameraPageBinding
    private lateinit var financeAdapter: FinanceAdapter
    private lateinit var imageUri: Uri
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var databaseRef: DatabaseReference
    private val allEntries = mutableListOf<FinanceEntity>()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CameraPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get current Firebase user
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userId = currentUser.uid
        databaseRef = FirebaseDatabase.getInstance().getReference("finances").child(userId!!)

        financeAdapter = FinanceAdapter(
            onImageClick = { imageUri ->
                val intent = Intent(this, ImageViewActivity::class.java)
                intent.putExtra("imageUri", imageUri)
                startActivity(intent)
            },
            onDeleteClick = { finance ->
                databaseRef.child(finance.id).removeValue()
                allEntries.removeIf { it.id == finance.id }
                financeAdapter.submitList(allEntries.toList())
            }
        )

        binding.recyclerView.adapter = financeAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        imageUri = createImageUri()

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.imageView.setImageURI(uri)
                imageUri = uri
                Toast.makeText(this, "Image Picked: $uri", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                binding.imageView.setImageURI(imageUri)
                Toast.makeText(this, "Image Captured", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        val categories = listOf("Food", "Transport", "Bills", "Shopping", "Salary", "Cheque", "Investments", "Bonus", "Other")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = spinnerAdapter

        val typeOptions = listOf("Expense", "Income")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = typeAdapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.editCustomCategory.visibility =
                    if (categories[position] == "Other") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnUpload.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnCapture.setOnClickListener {
            imageUri = createImageUri()
            takePicture.launch(imageUri)
        }

        binding.btnDelete.setOnClickListener {
            val imagePath = imageUri.toString()
            val financeToDelete = allEntries.find { it.imageUri == imagePath }

            if (financeToDelete != null) {
                databaseRef.child(financeToDelete.id).removeValue()
                allEntries.remove(financeToDelete)
                financeAdapter.submitList(allEntries.toList())
                Toast.makeText(this, "Finance entry deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No matching finance entry found", Toast.LENGTH_SHORT).show()
            }

            binding.imageView.setImageResource(0)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDone.setOnClickListener {
            saveFinanceData()
        }

        loadFinances()
    }

    private fun saveFinanceData() {
        val category = if (binding.editCustomCategory.visibility == View.VISIBLE &&
            binding.editCustomCategory.text.isNotBlank()) {
            binding.editCustomCategory.text.toString()
        } else {
            binding.spinnerCategory.selectedItem.toString()
        }

        val financeData = FinanceEntity(
            id = databaseRef.push().key ?: UUID.randomUUID().toString(),
            amount = binding.editAmount.text.toString().toDoubleOrNull() ?: 0.0,
            name = category,
            description = binding.editDescription.text.toString(),
            type = binding.spinnerType.selectedItem.toString().lowercase(),
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            imageUri = imageUri.toString()
        )

        databaseRef.child(financeData.id).setValue(financeData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFinances() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allEntries.clear()
                for (child in snapshot.children) {
                    if (child.key == "balance" || child.key == "budget_editable") continue
                    // Skip non-finance entries
                    val entry = child.getValue(FinanceEntity::class.java)
                    entry?.let { allEntries.add(it) }
                }
                financeAdapter.submitList(allEntries.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CameraPage, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "camera_photo_$timeStamp.png"
        val imageFile = File(filesDir, imageFileName)
        return FileProvider.getUriForFile(
            applicationContext,
            "${packageName}.fileprovider",
            imageFile
        )
    }
}
