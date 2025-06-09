package com.example.spendsense20.viewmodel

import androidx.lifecycle.ViewModel
import com.example.spendsense20.User
import com.google.firebase.database.FirebaseDatabase

class UserViewModel : ViewModel() {
    // Firebase reference for users
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")


    // Reference: Anon (2019). Saving Data | Firebase Realtime Database. [online] Firebase. Available at: https://firebase.google.com/docs/database/admin/save-data.
    // [Accessed 6 Jun. 2025].
    fun registerUser(user: User, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val userKey = user.email.replace(".", ",")
//check if user already exists
        dbRef.child(userKey).get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                dbRef.child(userKey).setValue(user)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it) }
            } else {
                onError(Exception("Email already exists"))
            }
        }.addOnFailureListener {
            onError(it)
        }
    }


    // Reference: Anon (2020b). Read and Write Data on the Web | Firebase Realtime Database. [online] Firebase. Available at: https://firebase.google.com/docs/database/web/read-and-write.
    // [Accessed 6 Jun. 2025].
    //fetch user by email and log them in
    fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        val userKey = email.replace(".", ",")

        dbRef.child(userKey).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                callback(user)
            }
            //error handling
            .addOnFailureListener {
                callback(null)
            }
    }
}
