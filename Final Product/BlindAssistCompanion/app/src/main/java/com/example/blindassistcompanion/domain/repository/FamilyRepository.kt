package com.example.blindassistcompanion.domain.repository

import com.example.blindassistcompanion.domain.model.FamilyMember
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun getAllFamilyMembers(): Flow<List<FamilyMember>>
    suspend fun getMemberById(uuid: String): FamilyMember?
    suspend fun saveFamilyMember(member: FamilyMember)
    suspend fun deleteFamilyMember(uuid: String)
}
