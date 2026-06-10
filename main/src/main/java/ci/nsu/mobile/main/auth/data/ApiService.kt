package ci.nsu.mobile.main.auth.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Response
import ci.nsu.mobile.main.auth.data.models.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<Unit>

    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>

    @GET("groups")
    suspend fun getGroups(): Response<List<GroupDto>>

    @GET("me")
    suspend fun getMe(): Response<UserDto>
}

const val BASE_URL = "http://10.0.2.2:8080/api/"

fun createApiService(tokenManager: TokenManager): ApiService {
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
            tokenManager.token?.let {
                req.addHeader("Authorization", "Bearer $it")
            }
            chain.proceed(req.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}