package com.example.spendsense20

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_table")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int =0,
    val username: String,
    val email: String,
    val password: String
)
