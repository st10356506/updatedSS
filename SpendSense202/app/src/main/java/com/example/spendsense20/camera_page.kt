/*Developers, A. (2024). Get started with camera on Android. [online] Android Developers. Available at: https://developer.android.com/media/camera/get-started-with-camera.
(Developers, A., 2024)
*/

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraPage : AppCompatActivity() {

    private lateinit var binding: CameraPageBinding
    private lateinit var financeDao: FinanceDao
    private lateinit var financeAdapter: FinanceAdapter
    private lateinit var imageUri: Uri
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePicture: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CameraPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        financeDao = FinanceDB.getDatabase(this).FinanceDao()

        financeAdapter = FinanceAdapter(
            onImageClick = { imageUri ->
                val intent = Intent(this, ImageViewActivity::class.java)
                intent.putExtra("imageUri", imageUri)
                startActivity(intent)
            },
            onDeleteClick = { finance ->
                lifecycleScope.launch {
                    financeDao.deleteFinance(finance.id)
                    val updatedList = financeDao.getAllFinances()
                    financeAdapter.submitList(updatedList)
                }
            }
        )

        binding.recyclerView.adapter = financeAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        imageUri = createImageUri()

        // Register pickers
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

        // Category spinner
        val categories = listOf("Food", "Transport", "Bills", "Shopping", "Salary", "Cheque", "Investments", "Bonus", "Other")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = spinnerAdapter

        // Type spinner
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

        // Button listeners
        binding.btnUpload.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnCapture.setOnClickListener {
            imageUri = createImageUri()
            takePicture.launch(imageUri)
        }
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
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDone.setOnClickListener {
            saveFinanceData()
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
        }

        loadFinances()
    }
//save to db
    private fun saveFinanceData() {
        val category = if (binding.editCustomCategory.visibility == View.VISIBLE &&
            binding.editCustomCategory.text.isNotBlank()) {
            binding.editCustomCategory.text.toString()
        } else {
            binding.spinnerCategory.selectedItem.toString()
        }

        val financeData = FinanceEntity(
            amount = binding.editAmount.text.toString().toDoubleOrNull() ?: 0.0,
            name = category,
            description = binding.editDescription.text.toString(),
            type = binding.spinnerType.selectedItem.toString().lowercase(),
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            imageUri = imageUri.toString()
        )

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
