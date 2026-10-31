package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.DriveTestSample
import com.example.data.entity.DriveTestSession
import kotlinx.coroutines.flow.Flow

@Dao
interface DriveTestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: DriveTestSession): Long

    @Update
    suspend fun updateSession(session: DriveTestSession)

    @Delete
    suspend fun deleteSession(session: DriveTestSession)

    @Query("SELECT * FROM drive_test_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<DriveTestSession>>

    @Query("SELECT * FROM drive_test_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): DriveTestSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: DriveTestSample): Long

    @Query("SELECT * FROM drive_test_samples WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getSamplesForSession(sessionId: Long): Flow<List<DriveTestSample>>

    @Query("SELECT * FROM drive_test_samples WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getSamplesListForSession(sessionId: Long): List<DriveTestSample>

    @Query("SELECT COUNT(*) FROM drive_test_samples WHERE sessionId = :sessionId")
    suspend fun getSampleCount(sessionId: Long): Int

    @Query("DELETE FROM drive_test_samples WHERE sessionId = :sessionId")
    suspend fun deleteSamplesForSession(sessionId: Long)
}
