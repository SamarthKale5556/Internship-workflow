package com.example.blindassistcompanion.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    @Query("SELECT * FROM family_members ORDER BY lastSeenTimestamp DESC")
    fun getAllFamilyMembers(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members WHERE uuid = :uuid LIMIT 1")
    suspend fun getMemberById(uuid: String): FamilyMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMemberEntity): Unit

    @Query("DELETE FROM family_members WHERE uuid = :uuid")
    suspend fun deleteFamilyMember(uuid: String): Unit
}
