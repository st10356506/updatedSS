package com.example.spendsense20

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

// expand image logic
class ImageViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)
//display full image on the view
        val imageUri = intent.getStringExtra("imageUri")
        val imageView = findViewById<ImageView>(R.id.fullImageView)

        imageUri?.let {
            imageView.setImageURI(Uri.parse(it))
        }
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish() // back to previous screen
        }

    }
}
