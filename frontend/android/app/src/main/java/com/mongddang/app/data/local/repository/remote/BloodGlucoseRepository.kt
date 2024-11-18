package com.mongddang.app.data.local.repository.remote

import com.mongddang.app.data.local.dao.api.ApiResponse
import com.mongddang.app.data.local.entity.BloodGlucoseRequest
import com.mongddang.app.data.local.entity.BloodGlucoseResponse
import kotlinx.coroutines.flow.Flow

interface BloodGlucoseRepository {
    suspend fun sendSamsungBloodGlucose(bloodGlucoseRequest: BloodGlucoseRequest): Flow<ApiResponse<BloodGlucoseResponse>>
}