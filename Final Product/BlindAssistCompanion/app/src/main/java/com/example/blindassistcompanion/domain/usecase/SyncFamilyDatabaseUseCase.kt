package com.example.blindassistcompanion.domain.usecase

import android.util.Log
import com.example.blindassistcompanion.domain.model.DeviceCommand
import com.example.blindassistcompanion.domain.model.FamilyMember
import com.example.blindassistcompanion.domain.repository.BleRepository
import com.example.blindassistcompanion.domain.repository.HotspotRepository
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class SyncFamilyDatabaseUseCase @Inject constructor(
    private val bleRepository: BleRepository,
    private val hotspotRepository: HotspotRepository
) {
    suspend operator fun invoke(member: FamilyMember): Result<Unit> {
        return try {
            // 1. Send START_WIFI via BLE
            bleRepository.writeCommand(DeviceCommand.StartWifi)
            
            // 2. Connect to Pi SoftAP (MVP assumes static SSID/Pass or BLE exchange already handled)
            hotspotRepository.connectToHotspot("BlindAssist_Pi", "blindassist123")
            
            // 3. Serialize Embedding to JSON
            val embeddingArray = JSONArray()
            member.embedding.forEach { embeddingArray.put(it) }
            
            val jsonPayload = JSONObject().apply {
                put("uuid", member.uuid)
                put("name", member.name)
                put("relationship", member.relationship)
                put("embedding", embeddingArray)
            }.toString()
            
            // 4. POST to HTTPS API
            Log.d("Sync", "Posting to Pi: $jsonPayload")
            // In full implementation: piRestClient.syncEmbedding(jsonPayload)
            
            // 5. Send STOP_WIFI via BLE
            bleRepository.writeCommand(DeviceCommand.StopWifi)
            hotspotRepository.disconnectFromHotspot()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
