package com.example.blindassistcompanion.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.blindassistcompanion.domain.model.FamilyMember

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey val uuid: String,
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val emergencyPriority: Boolean,
    
    // Encrypted byte array of the 512-float vector
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val encryptedEmbedding: ByteArray,
    
    val createdTimestamp: Long,
    val lastSeenTimestamp: Long,
    val recognitionCount: Int,
    
    val averageConfidence: Float,
    val highestConfidence: Float,
    val lowestConfidence: Float
) {
    fun toDomainModel(decryptedEmbedding: FloatArray): FamilyMember {
        return FamilyMember(
            uuid = uuid,
            name = name,
            relationship = relationship,
            phoneNumber = phoneNumber,
            emergencyPriority = emergencyPriority,
            embedding = decryptedEmbedding,
            createdTimestamp = createdTimestamp,
            lastSeenTimestamp = lastSeenTimestamp,
            recognitionCount = recognitionCount,
            averageConfidence = averageConfidence,
            highestConfidence = highestConfidence,
            lowestConfidence = lowestConfidence
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FamilyMemberEntity

        if (uuid != other.uuid) return false
        if (name != other.name) return false
        if (relationship != other.relationship) return false
        if (phoneNumber != other.phoneNumber) return false
        if (emergencyPriority != other.emergencyPriority) return false
        if (!encryptedEmbedding.contentEquals(other.encryptedEmbedding)) return false
        if (createdTimestamp != other.createdTimestamp) return false
        if (lastSeenTimestamp != other.lastSeenTimestamp) return false
        if (recognitionCount != other.recognitionCount) return false
        if (averageConfidence != other.averageConfidence) return false
        if (highestConfidence != other.highestConfidence) return false
        if (lowestConfidence != other.lowestConfidence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + relationship.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + emergencyPriority.hashCode()
        result = 31 * result + encryptedEmbedding.contentHashCode()
        result = 31 * result + createdTimestamp.hashCode()
        result = 31 * result + lastSeenTimestamp.hashCode()
        result = 31 * result + recognitionCount
        result = 31 * result + averageConfidence.hashCode()
        result = 31 * result + highestConfidence.hashCode()
        result = 31 * result + lowestConfidence.hashCode()
        return result
    }
}
