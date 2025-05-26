package com.example.spendsense20.viewmodel

import androidx.lifecycle.ViewModel
import com.example.spendsense20.User
import com.google.firebase.database.FirebaseDatabase

class UserViewModel : ViewModel() {
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")

    fun registerUser(user: User, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val userKey = user.email.replace(".", ",")

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

    fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        val userKey = email.replace(".", ",")
        dbRef.child(userKey).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                callback(user)
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
