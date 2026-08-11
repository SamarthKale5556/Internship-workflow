package com.example.blindassistcompanion.data.repository

import com.example.blindassistcompanion.data.local.FamilyDao
import com.example.blindassistcompanion.data.local.FamilyMemberEntity
import com.example.blindassistcompanion.domain.model.FamilyMember
import com.example.blindassistcompanion.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FamilyRepositoryImpl @Inject constructor(
    private val familyDao: FamilyDao
) : FamilyRepository {

    override fun getAllFamilyMembers(): Flow<List<FamilyMember>> {
        return familyDao.getAllFamilyMembers().map { entities ->
            entities.map { entity ->
                // In production, we decrypt the embedding byte array here
                // using the Keystore-backed cipher.
                val decryptedFloatArray = decodeEmbedding(entity.encryptedEmbedding)
                entity.toDomainModel(decryptedFloatArray)
            }
        }
    }

    override suspend fun getMemberById(uuid: String): FamilyMember? {
        val entity = familyDao.getMemberById(uuid) ?: return null
        val decryptedFloatArray = decodeEmbedding(entity.encryptedEmbedding)
        return entity.toDomainModel(decryptedFloatArray)
    }

    override suspend fun saveFamilyMember(member: FamilyMember) {
        // In production, we encrypt the float array here
        val encryptedBytes = encodeEmbedding(member.embedding)
        val entity = FamilyMemberEntity(
            uuid = member.uuid,
            name = member.name,
            relationship = member.relationship,
            phoneNumber = member.phoneNumber,
            emergencyPriority = member.emergencyPriority,
            encryptedEmbedding = encryptedBytes,
            createdTimestamp = member.createdTimestamp,
            lastSeenTimestamp = member.lastSeenTimestamp,
            recognitionCount = member.recognitionCount,
            averageConfidence = member.averageConfidence,
            highestConfidence = member.highestConfidence,
            lowestConfidence = member.lowestConfidence
        )
        familyDao.insertFamilyMember(entity)
    }

    override suspend fun deleteFamilyMember(uuid: String) {
        familyDao.deleteFamilyMember(uuid)
    }

    // Stub for encoding float array to encrypted byte array
    private fun encodeEmbedding(embedding: FloatArray): ByteArray {
        // Implementation omitted for brevity. Would use ByteBuffer + Cipher.
        return ByteArray(embedding.size * 4) 
    }

    // Stub for decoding encrypted byte array to float array
    private fun decodeEmbedding(bytes: ByteArray): FloatArray {
        // Implementation omitted for brevity.
        return FloatArray(bytes.size / 4)
    }
}
