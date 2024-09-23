package com.spruceid.mobilesdkexample.db

import androidx.annotation.WorkerThread

class VerificationActivityLogsRepository(private val verificationActivityLogsDao: VerificationActivityLogsDao) {
  val verificationActivityLogs: List<VerificationActivityLogs> = verificationActivityLogsDao.getAllVerificationActivityLogs()

  @WorkerThread
  suspend fun insertVerificationActivityLog(verificationActivityLogs: VerificationActivityLogs) {
    verificationActivityLogsDao.insertVerificationActivity(verificationActivityLogs)
  }

  @WorkerThread
  suspend fun getVerificationActivityLogs(): List<VerificationActivityLogs> {
    return verificationActivityLogsDao.getAllVerificationActivityLogs()
  }
}
