package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PersonalityProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalityDao {
    @Query("SELECT * FROM personality_profiles WHERE id = 1 LIMIT 1")
    fun getActiveProfile(): Flow<PersonalityProfile?>

    @Query("SELECT * FROM personality_profiles WHERE id = 1 LIMIT 1")
    suspend fun getActiveProfileSync(): PersonalityProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PersonalityProfile)

    @Update
    suspend fun updateProfile(profile: PersonalityProfile)

    @Query("UPDATE personality_profiles SET affinityPoints = affinityPoints + :delta WHERE id = 1")
    suspend fun addAffinityPoints(delta: Int)
}
