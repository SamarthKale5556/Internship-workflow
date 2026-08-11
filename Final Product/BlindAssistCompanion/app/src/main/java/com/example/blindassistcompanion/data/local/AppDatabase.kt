package com.example.blindassistcompanion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// NOTE: In a full production environment, we use net.zetetic:android-database-sqlcipher 
// to encrypt the DB and supply a SupportSQLiteOpenHelper.Factory using the Android Keystore.
@Database(
    entities = [FamilyMemberEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyDao(): FamilyDao
}
