package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VaultFile
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_files ORDER BY date DESC, createdAt DESC")
    fun getAllFiles(): Flow<List<VaultFile>>

    @Query("SELECT * FROM vault_files WHERE date = :date ORDER BY createdAt DESC")
    fun getFilesByDate(date: String): Flow<List<VaultFile>>

    @Query("SELECT * FROM vault_files WHERE month = :month ORDER BY date DESC, createdAt DESC")
    fun getFilesByMonth(month: String): Flow<List<VaultFile>>

    @Query("SELECT * FROM vault_files WHERE year = :year ORDER BY date DESC, createdAt DESC")
    fun getFilesByYear(year: Int): Flow<List<VaultFile>>

    @Query("SELECT * FROM vault_files WHERE isFavorite = 1 ORDER BY date DESC, createdAt DESC")
    fun getFavoriteFiles(): Flow<List<VaultFile>>

    @Query("SELECT * FROM vault_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): VaultFile?

    @Query("SELECT * FROM vault_files WHERE title LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchFiles(query: String): Flow<List<VaultFile>>

    @Query("SELECT DISTINCT date FROM vault_files WHERE month = :month")
    fun getDatesWithFilesForMonth(month: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM vault_files")
    fun getTotalFileCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(fileSize), 0) FROM vault_files")
    fun getTotalStorageBytes(): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<VaultFile>)

    @Update
    suspend fun updateFile(file: VaultFile)

    @Delete
    suspend fun deleteFile(file: VaultFile)

    @Delete
    suspend fun deleteFiles(files: List<VaultFile>)

    @Query("DELETE FROM vault_files WHERE id = :id")
    suspend fun deleteFileById(id: String)

    @Query("UPDATE vault_files SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())
}
