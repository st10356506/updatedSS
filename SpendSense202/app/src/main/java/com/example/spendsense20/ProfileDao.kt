package com.example.spendsense20

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProfileDao {

    @Insert
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profile_table WHERE username = :username")
    suspend fun getProfile(username: String): ProfileEntity?

    @Query("DELETE FROM profile_table WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Int)
}