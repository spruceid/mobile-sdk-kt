package com.spruceid.mobilesdkexample.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VerificationActivityLogsDao {
  @Insert
  suspend fun insertVerificationActivity(verificationActivityLogs: VerificationActivityLogs)

  @Query("SELECT * FROM verification_activity_logs")
  fun getAllVerificationActivityLogs(): List<VerificationActivityLogs>
}
