package io.github.koolunkle.yakallim.data.datasource.remote.api

import io.github.koolunkle.yakallim.data.datasource.remote.dto.OcrJobResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface OcrApiService {
    @Multipart
    @POST("api/v1/ocr/enqueue")
    suspend fun enqueueOcrJob(
        @Part file: MultipartBody.Part,
        @Part("fcmToken") fcmToken: RequestBody?
    ): OcrJobResponse

    @GET("api/v1/ocr/jobs/{jobId}")
    suspend fun getOcrJob(
        @Path("jobId") jobId: String
    ): OcrJobResponse

    @POST("api/v1/ocr/jobs/{jobId}/cancel")
    suspend fun cancelOcrJob(
        @Path("jobId") jobId: String
    ): Response<Unit>
}
