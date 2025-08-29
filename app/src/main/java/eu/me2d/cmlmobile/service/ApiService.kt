package eu.me2d.cmlmobile.service

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import eu.me2d.cmlmobile.CmlMobileApp
import eu.me2d.cmlmobile.dto.ApiCommand
import eu.me2d.cmlmobile.dto.RegisterRequest
import eu.me2d.cmlmobile.dto.RegisterResponse
import eu.me2d.cmlmobile.state.GlobalStateViewModel
import eu.me2d.cmlmobile.state.StateSettings
import io.jsonwebtoken.Jwts
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import timber.log.Timber
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import kotlin.io.encoding.Base64

interface ApiInterface {
    @POST("clients")
    @Headers("Content-Type: application/json")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @GET("commands")
    suspend fun getCommands(@Header("Authorization") authorization: String): Response<List<ApiCommand>>

    @POST("commands/{commandNumber}")
    suspend fun executeCommand(
        @Path("commandNumber") commandNumber: Int,
        @Header("Authorization") authorization: String
    ): Response<Unit>
}

class ApiService(
    private val networkService: NetworkService
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    /**
     * Creates a configured Retrofit instance with logging interceptor
     */
    private fun createRetrofitInstance(settings: StateSettings): ApiInterface {
        val baseUrl = getBaseUrl(settings)
        Timber.d("ApiService: Selected base URL: $baseUrl")

        val contentType = "application/json".toMediaType()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(ApiInterface::class.java)
    }

    /**
     * Determines the appropriate base URL based on wifi connection and pattern matching
     */
    private fun getBaseUrl(settings: StateSettings): String {
        Timber.d("ApiService: Determining base URL...")

        val currentWifiName = networkService.getCurrentWifiName()
        Timber.d("ApiService: Current WiFi name: $currentWifiName")

        val baseUrl = if (currentWifiName != null && settings.wifiPattern.isNotBlank()) {
            Timber.d("ApiService: Device connected to WiFi and pattern exists: '${settings.wifiPattern}'")

            val wifiPattern = settings.wifiPattern.toRegex()
            val matches = wifiPattern.matches(currentWifiName)

            if (matches) {
                Timber.d("ApiService: WiFi name matches pattern. Using wifiUrl: '${settings.wifiUrl}'")
                settings.wifiUrl
            } else {
                Timber.d("ApiService: WiFi name doesn't match pattern. Using apiUrl: '${settings.apiUrl}'")
                settings.apiUrl
            }
        } else {
            if (currentWifiName == null) {
                Timber.d("ApiService: Not connected to WiFi. Using apiUrl: '${settings.apiUrl}'")
            } else {
                Timber.d("ApiService: No WiFi pattern configured. Using apiUrl: '${settings.apiUrl}'")
            }
            settings.apiUrl
        }

        // Ensure the base URL always has a trailing slash
        return if (baseUrl.endsWith("/")) {
            baseUrl
        } else {
            "$baseUrl/"
        }
    }

    suspend fun register(settings: StateSettings, globalStateViewModel: GlobalStateViewModel) {
        val callType = "register"
        Timber.d("ApiService: Starting register call...")

        // Set API call as in progress
        globalStateViewModel.setApiCallInProgress(callType)

        val api = createRetrofitInstance(settings)

        Timber.d("ApiService: Making POST request to register endpoint...")

        try {
            val keys = CmlMobileApp.appModule.cryptoService.generateKeys()
            val publicKeyStr = "-----BEGIN PUBLIC KEY-----\n${keys.publicKey}-----END PUBLIC KEY-----\n"
            val response = api.register(
                RegisterRequest(
                    key = publicKeyStr,
                    message = settings.myId
                )
            )
            Timber.d("ApiService: Register call completed. Response code: ${response.code()}")

            // Log raw response details
            Timber.d("ApiService: Response headers: ${response.headers()}")
            Timber.d("ApiService: Response message: ${response.message()}")

            // Log raw response body if available
            if (response.errorBody() != null) {
                val errorBody = response.errorBody()?.string()
                Timber.d("ApiService: Error response body: $errorBody")
            }

            if (response.body() != null) {
                Timber.d("ApiService: Success response body: ${response.body()}")
            }

            if (response.isSuccessful) {
                Timber.i("ApiService: Register successful")
                globalStateViewModel.setApiCallSuccess(callType, response.body()?.status ?: "Success")
                globalStateViewModel.onRegistrationComplete(keys.privateKey)
            } else {
                val errorMsg = "Registration failed with HTTP ${response.code()}"
                Timber.w("ApiService: $errorMsg")
                globalStateViewModel.setApiCallError(callType, errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Registration failed: ${e.message}"
            Timber.e(e, "ApiService: Register call failed with exception")
            globalStateViewModel.setApiCallError(callType, errorMsg)
        }
    }

    suspend fun fetchCommands(
        settings: StateSettings,
        privateKeyEncoded: String
    ): List<ApiCommand> {
        val decodedKey = Base64.decode(privateKeyEncoded)
        val kf: KeyFactory = KeyFactory.getInstance("RSA")
        val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(decodedKey))
        val jws = Jwts.builder()
            .subject("Commands")
            .id(Date().time.toString())
            .signWith(privateKey)
            .compact()

        val api = createRetrofitInstance(settings)

        Timber.d("ApiService: Making GET request to commands endpoint...")

        val response = api.getCommands("Bearer $jws")
        Timber.d("ApiService: Commands call completed. Response code: ${response.code()}")

        // Log raw response details
        Timber.d("ApiService: Response headers: ${response.headers()}")
        Timber.d("ApiService: Response message: ${response.message()}")

        // Log raw response body if available
        if (response.errorBody() != null) {
            val errorBody = response.errorBody()?.string()
            Timber.d("ApiService: Error response body: $errorBody")
        }

        if (response.body() != null) {
            Timber.d("ApiService: Success response body: ${response.body()}")
        }

        if (response.isSuccessful) {
            val commands = response.body() ?: emptyList()
            Timber.i("ApiService: Successfully fetched ${commands.size} commands.")
            return commands
        } else {
            val errorMsg = "Commands fetch failed with HTTP ${response.code()}"
            Timber.w("ApiService: $errorMsg")
            throw Exception(errorMsg)
        }
    }

    suspend fun executeCommand(
        settings: StateSettings,
        privateKeyEncoded: String,
        commandNumber: Int
    ) {
        val decodedKey = Base64.decode(privateKeyEncoded)
        val kf: KeyFactory = KeyFactory.getInstance("RSA")
        val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(decodedKey))
        val jws = Jwts.builder()
            .subject("ExecuteCommand")
            .id(Date().time.toString())
            .signWith(privateKey)
            .compact()

        val api = createRetrofitInstance(settings)

        Timber.d("ApiService: Making POST request to execute command $commandNumber...")

        val response = api.executeCommand(commandNumber, "Bearer $jws")
        Timber.d("ApiService: Execute command call completed. Response code: ${response.code()}")

        // Log raw response details
        Timber.d("ApiService: Response message: ${response.message()}")

        // Log raw response body if available
        if (response.errorBody() != null) {
            val errorBody = response.errorBody()?.string()
            Timber.d("ApiService: Error response body: $errorBody")
        }

        if (response.isSuccessful) {
            Timber.i("ApiService: Successfully executed command $commandNumber")
        } else {
            val errorMsg = "Command execution failed with HTTP ${response.code()}"
            Timber.w("ApiService: $errorMsg")
            throw Exception(errorMsg)
        }
    }
}