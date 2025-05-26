<<<<<<< HEAD
=======
/*Developers, A. (2024). Get started with camera on Android. [online] Android Developers. Available at: https://developer.android.com/media/camera/get-started-with-camera.
(Developers, A., 2024)
*/

>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendsense20.databinding.CameraPageBinding
<<<<<<< HEAD
import com.google.firebase.database.*
=======
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
<<<<<<< HEAD
import java.util.*
=======
import java.util.Date
import java.util.Locale
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446

class CameraPage : AppCompatActivity() {

    private lateinit var binding: CameraPageBinding
<<<<<<< HEAD
=======
    private lateinit var financeDao: FinanceDao
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
    private lateinit var financeAdapter: FinanceAdapter
    private lateinit var imageUri: Uri
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePicture: ActivityResultLauncher<Uri>
<<<<<<< HEAD
    private lateinit var databaseRef: DatabaseReference
    private val allEntries = mutableListOf<FinanceEntity>()
=======
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CameraPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

<<<<<<< HEAD
        databaseRef = FirebaseDatabase.getInstance().getReference("finances")
=======
        financeDao = FinanceDB.getDatabase(this).FinanceDao()
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446

        financeAdapter = FinanceAdapter(
            onImageClick = { imageUri ->
                val intent = Intent(this, ImageViewActivity::class.java)
                intent.putExtra("imageUri", imageUri)
                startActivity(intent)
            },
            onDeleteClick = { finance ->
<<<<<<< HEAD
                databaseRef.child(finance.id).removeValue()
                allEntries.removeIf { it.id == finance.id }
                financeAdapter.submitList(allEntries.toList())
=======
                lifecycleScope.launch {
                    financeDao.deleteFinance(finance.id)
                    val updatedList = financeDao.getAllFinances()
                    financeAdapter.submitList(updatedList)
                }
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
            }
        )

        binding.recyclerView.adapter = financeAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        imageUri = createImageUri()

<<<<<<< HEAD
=======
        // Register pickers
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
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

<<<<<<< HEAD
=======
        // Category spinner
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
        val categories = listOf("Food", "Transport", "Bills", "Shopping", "Salary", "Cheque", "Investments", "Bonus", "Other")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = spinnerAdapter

<<<<<<< HEAD
=======
        // Type spinner
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
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

<<<<<<< HEAD
=======
        // Button listeners
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
        binding.btnUpload.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnCapture.setOnClickListener {
            imageUri = createImageUri()
            takePicture.launch(imageUri)
        }
<<<<<<< HEAD

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
=======
        binding.btnDelete.setOnClickListener {
            lifecycleScope.launch {
                val allFinances = financeDao.getAllFinances()
                val imagePath = imageUri.toString()
                val financeToDelete = allFinances.find { it.imageUri == imagePath }

                if (financeToDelete != null) {
                    financeDao.deleteFinance(financeToDelete.id)
                    loadFinances()
                    Toast.makeText(this@CameraPage, "Finance entry deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CameraPage, "No matching finance entry found", Toast.LENGTH_SHORT).show()
                }

                binding.imageView.setImageResource(0)
            }
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDone.setOnClickListener {
            saveFinanceData()
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
<<<<<<< HEAD

            // Navigate back to AddFragment's activity (replace MainActivity with your host activity class)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // close CameraPage so it's removed from back stack
=======
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
        }

        loadFinances()
    }
<<<<<<< HEAD

=======
//save to db
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
    private fun saveFinanceData() {
        val category = if (binding.editCustomCategory.visibility == View.VISIBLE &&
            binding.editCustomCategory.text.isNotBlank()) {
            binding.editCustomCategory.text.toString()
        } else {
            binding.spinnerCategory.selectedItem.toString()
        }

        val financeData = FinanceEntity(
<<<<<<< HEAD
            id = databaseRef.push().key ?: UUID.randomUUID().toString(),
=======
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
            amount = binding.editAmount.text.toString().toDoubleOrNull() ?: 0.0,
            name = category,
            description = binding.editDescription.text.toString(),
            type = binding.spinnerType.selectedItem.toString().lowercase(),
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            imageUri = imageUri.toString()
        )

<<<<<<< HEAD
        databaseRef.child(financeData.id).setValue(financeData)
    }

    private fun loadFinances() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allEntries.clear()
                for (child in snapshot.children) {
                    val entry = child.getValue(FinanceEntity::class.java)
                    entry?.let { allEntries.add(it) }
                }
                financeAdapter.submitList(allEntries.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CameraPage, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
=======
        lifecycleScope.launch(Dispatchers.IO) {
            financeDao.insertFinance(financeData)
            loadFinances()
        }
    }

    private fun loadFinances() {
        lifecycleScope.launch {
            val allFinances = financeDao.getAllFinances()
            financeAdapter.submitList(allFinances)
        }
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
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
