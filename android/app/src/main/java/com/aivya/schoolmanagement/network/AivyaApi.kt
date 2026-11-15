package com.aivya.schoolmanagement.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AivyaApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/v1/classes")
    suspend fun getMyClasses(): Response<List<ClassDto>>

    @GET("api/v1/classes/{classId}/roster")
    suspend fun getRoster(
        @Path("classId") classId: String,
        @Query("date") date: String? = null,
    ): Response<List<StudentAttendanceDto>>

    @POST("api/v1/classes/{classId}/attendance")
    suspend fun submitAttendance(
        @Path("classId") classId: String,
        @Body request: AttendanceSubmitRequest,
    ): Response<AttendanceSummaryDto>

    @POST("api/v1/classes/{classId}/attendance/qr-checkin")
    suspend fun qrCheckIn(
        @Path("classId") classId: String,
        @Body request: QrCheckInRequest,
    ): Response<QrCheckInResponseDto>

    @GET("api/v1/classes/{classId}/attendance/history")
    suspend fun getHistory(
        @Path("classId") classId: String,
        @Query("days") days: Int = 7,
    ): Response<List<AttendanceHistoryItemDto>>
}
