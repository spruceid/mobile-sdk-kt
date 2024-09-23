package com.spruceid.mobilesdkexample.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [VerificationActivityLogs::class], version = 1)
@TypeConverters(*[DateConverter::class])
abstract class AppDatabase : RoomDatabase() {
  abstract fun verificationActivityLogsDao(): VerificationActivityLogsDao

  companion object {
    @Volatile
    private var dbInstance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return dbInstance ?: synchronized(this) {
        val instance =
          Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "verification_activity_logs",
          )
            .allowMainThreadQueries()
            .build()
        dbInstance = instance
        instance
      }
    }
  }
}
