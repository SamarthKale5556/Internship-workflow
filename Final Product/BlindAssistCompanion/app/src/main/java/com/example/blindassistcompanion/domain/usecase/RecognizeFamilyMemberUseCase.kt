package com.example.blindassistcompanion.domain.usecase

import android.graphics.Rect
import com.example.blindassistcompanion.domain.repository.FamilyRepository
import com.example.blindassistcompanion.domain.repository.TtsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.sqrt

class RecognizeFamilyMemberUseCase @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val ttsRepository: TtsRepository
) {
    // 112x112 or standard bounding box sizes. We'll use percentages.
    // X center < 33% = Left, > 66% = Right, else Ahead.
    
    suspend operator fun invoke(incomingEmbedding: FloatArray, boundingBoxCenterXPercent: Float) = withContext(Dispatchers.Default) {
        val familyMembers = familyRepository.getAllFamilyMembers().firstOrNull() ?: return@withContext

        var bestMatchName: String? = null
        var highestSimilarity = -1.0

        for (member in familyMembers) {
            val similarity = cosineSimilarity(incomingEmbedding, member.embedding)
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity
                bestMatchName = member.name
            }
        }

        // Standard MVP threshold for normalized vectors
        if (highestSimilarity >= 0.65 && bestMatchName != null) {
            val direction = when {
                boundingBoxCenterXPercent < 0.33f -> "on left"
                boundingBoxCenterXPercent > 0.66f -> "on right"
                else -> "ahead"
            }
            
            val announcement = "$bestMatchName detected $direction"
            ttsRepository.speak(announcement)
        }
    }

    private fun cosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Double {
        if (vectorA.size != vectorB.size || vectorA.isEmpty()) return 0.0

        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in vectorA.indices) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i] * vectorA[i]
            normB += vectorB[i] * vectorB[i]
        }
        
        return if (normA == 0.0 || normB == 0.0) 0.0 else dotProduct / (sqrt(normA) * sqrt(normB))
    }
}
