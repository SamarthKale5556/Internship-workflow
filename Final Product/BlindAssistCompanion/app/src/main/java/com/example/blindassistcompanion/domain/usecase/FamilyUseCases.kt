package com.example.blindassistcompanion.domain.usecase

import com.example.blindassistcompanion.domain.model.FamilyMember
import com.example.blindassistcompanion.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFamilyMembersUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    operator fun invoke(): Flow<List<FamilyMember>> = familyRepository.getAllFamilyMembers()
}

class EnrollFaceUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(member: FamilyMember) {
        familyRepository.saveFamilyMember(member)
    }
}

class DeleteFamilyMemberUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(uuid: String) {
        familyRepository.deleteFamilyMember(uuid)
    }
}
