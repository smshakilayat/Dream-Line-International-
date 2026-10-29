package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MemoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_items ORDER BY isPinned DESC, importance DESC, timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memory_items ORDER BY isPinned DESC, importance DESC, timestamp DESC")
    suspend fun getAllMemoriesSync(): List<MemoryItem>

    @Query("SELECT * FROM memory_items WHERE category = :category ORDER BY isPinned DESC, timestamp DESC")
    fun getMemoriesByCategory(category: String): Flow<List<MemoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<MemoryItem>)

    @Update
    suspend fun updateMemory(memory: MemoryItem)

    @Delete
    suspend fun deleteMemory(memory: MemoryItem)

    @Query("DELETE FROM memory_items WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("UPDATE memory_items SET isPinned = NOT isPinned WHERE id = :id")
    suspend fun togglePin(id: Long)

    @Query("UPDATE memory_items SET lastRecalledAt = :recalledTime, recalledCount = recalledCount + 1 WHERE id = :id")
    suspend fun markRecalled(id: Long, recalledTime: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM memory_items")
    suspend fun getMemoryCount(): Int
}
