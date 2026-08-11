package com.example.blindassistcompanion.domain.model

enum class FaceAngle {
    FRONT,
    LEFT,
    RIGHT,
    UP,
    DOWN
}

data class FaceCaptureResult(
    val angle: FaceAngle,
    val qualityScore: Float,
    val brightnessScore: Float,
    val blurScore: Float
) {
    val isAcceptable: Boolean
        get() = qualityScore > 0.8f && brightnessScore > 0.4f && blurScore < 0.3f
}

data class FamilyMember(
    val uuid: String,
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val emergencyPriority: Boolean,
    val embedding: FloatArray,
    val createdTimestamp: Long,
    val lastSeenTimestamp: Long,
    val recognitionCount: Int,
    val averageConfidence: Float,
    val highestConfidence: Float,
    val lowestConfidence: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FamilyMember

        if (uuid != other.uuid) return false
        if (name != other.name) return false
        if (relationship != other.relationship) return false
        if (phoneNumber != other.phoneNumber) return false
        if (emergencyPriority != other.emergencyPriority) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + relationship.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + emergencyPriority.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}
