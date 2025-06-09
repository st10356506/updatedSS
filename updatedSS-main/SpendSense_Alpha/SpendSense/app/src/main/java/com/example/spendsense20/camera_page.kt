package com.example.spendsense20

import android.R.layout.simple_spinner_dropdown_item
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Spinner
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
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CameraPage : AppCompatActivity() {
//initialize views
    private lateinit var binding: CameraPageBinding
    private lateinit var financeAdapter: FinanceAdapter
    private lateinit var imageUri: Uri
    private lateinit var spinner_item : Spinner
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var databaseRef: DatabaseReference
    private val allEntries = mutableListOf<FinanceEntity>()
    private var userId: String? = null
    private lateinit var pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // bind views
        binding = CameraPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup date picker after binding is initialized
        binding.editDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    binding.editDate.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
            // Android Developers. (n.d.). DatePickerDialog. Retrieved June 7, 2025, from https://developer.android.com/reference/android/app/DatePickerDialog
        }
//get the logged in users
        val currentUser = FirebaseAuth.getInstance().currentUser
        // Google Firebase. (n.d.). Firebase Authentication documentation. Retrieved June 7, 2025, from https://firebase.google.com/docs/auth
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userId = currentUser.uid
        databaseRef = FirebaseDatabase.getInstance().getReference("finances").child(userId!!)
        // Google Firebase. (n.d.). Firebase Realtime Database documentation. Retrieved June 7, 2025, from https://firebase.google.com/docs/database
//inflate the view
        financeAdapter = FinanceAdapter(
            onImageClick = { imageUri ->
                val intent = Intent(this, ImageViewActivity::class.java)
                intent.putExtra("imageUri", imageUri)
                startActivity(intent)
            },
            onDeleteClick = { finance ->
                databaseRef.child(finance.id).removeValue()
                // Google Firebase. (n.d.). Delete data. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/delete-data
                allEntries.removeIf { it.id == finance.id }
                financeAdapter.submitList(allEntries.toList())
            }
        )

        binding.recyclerView.adapter = financeAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        // Android Developers. (n.d.). RecyclerView and LinearLayoutManager. Retrieved June 7, 2025, from https://developer.android.com/guide/topics/ui/layout/recyclerview

        imageUri = createImageUri()
        // Android Developers. (n.d.). FileProvider. Retrieved June 7, 2025, from https://developer.android.com/reference/androidx/core/content/FileProvider

        // Register for the photo picker result
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.imageView.setImageURI(uri)
                imageUri = uri
                Toast.makeText(this, "Image Picked: $uri", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
        // Android Developers. (n.d.). ActivityResultContracts.PickVisualMedia. Retrieved June 7, 2025, from https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.PickVisualMedia

        // Register for the camera capture result
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                //copy image to internal storage
                //“Capture an Image.” Android Developers, 2025, developer.android.com/media/camera/camerax/take-photo.
                val savedPath = copyImageToInternalStorage(this, imageUri)
                if (savedPath != null) {
                    File(imageUri.path!!).delete()
                    imageUri = Uri.fromFile(File(savedPath)) //update imageUri
                    binding.imageView.setImageURI(imageUri)
                    Toast.makeText(this, "Image Captured", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save captured image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show()
            }
        }
        // Android Developers. (n.d.). ActivityResultContracts.TakePicture. Retrieved June 7, 2025, from https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.TakePicture

        val categories = listOf("Food", "Transport", "Bills", "Shopping", "Salary", "Cheque", "Investments", "Bonus", "Other")
        val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = spinnerAdapter
        // Android Developers. (n.d.). Spinner and ArrayAdapter. Retrieved June 7, 2025, from https://developer.android.com/guide/topics/ui/controls/spinner

        val typeOptions = listOf("Expense", "Income")
        val typeAdapter = ArrayAdapter(this, R.layout.spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = typeAdapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.editCustomCategory.visibility =
                    if (categories[position] == "Other") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
//save the data to the database
    private fun saveFinanceData() {
        val category = if (binding.editCustomCategory.visibility == View.VISIBLE &&
            binding.editCustomCategory.text.isNotBlank()) {
            binding.editCustomCategory.text.toString()
        } else {
            binding.spinnerCategory.selectedItem.toString()
        }
//setup data to be saved
        val financeData = FinanceEntity(
            id = databaseRef.push().key ?: UUID.randomUUID().toString(),
            amount = binding.editAmount.text.toString().toDoubleOrNull() ?: 0.0,
            name = category,
            description = binding.editDescription.text.toString(),
            type = binding.spinnerType.selectedItem.toString().lowercase(),
            date = binding.editDate.text.toString().ifBlank {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            },
            imageUri = imageUri.toString()
        )
//toasts to confirm entry/error
        databaseRef.child(financeData.id).setValue(financeData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
            }
        // Google Firebase. (n.d.). Write data to Firebase Realtime Database. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/read-and-write#write_data
    }
//load the finances from the database
    private fun loadFinances() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allEntries.clear()
                for (child in snapshot.children) {
                    if (child.key == "balance" || child.key == "budget_editable") continue
                    val entry = child.getValue(FinanceEntity::class.java)
                    entry?.let { allEntries.add(it) }
                }
                financeAdapter.submitList(allEntries.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CameraPage, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        // Google Firebase. (n.d.). Read data from Firebase Realtime Database. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/read-and-write#read_data
    }
//inflate the image
    private fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "camera_photo_$timeStamp.png"
        val imageFile = File(filesDir, imageFileName)
        return FileProvider.getUriForFile(
            applicationContext,
            "${packageName}.fileprovider",
            imageFile
        )
        // Android Developers. (n.d.). FileProvider. Retrieved June 7, 2025, from https://developer.android.com/reference/androidx/core/content/FileProvider
    }

    // Function to save selected image to internal storage
    private fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        // Android Developers. (n.d.). Internal Storage. Retrieved June 7, 2025, from https://developer.android.com/training/data-storage/app-specific
    }
}
