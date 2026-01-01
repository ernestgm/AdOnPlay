import com.geniusdevelops.adonplay.BuildConfig
import com.geniusdevelops.adonplay.app.api.services.DeviceServices
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // Or MoshiConverterFactory
import java.util.concurrent.TimeUnit

object APIManager {

    val devices: DeviceServices by lazy {
        val retrofit = retrofitBuilder()
        retrofit.create(DeviceServices::class.java)
    }

    private fun retrofitBuilder(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Or MoshiConverterFactory
            .client(okHttpClient)
            .build()
    }
}
   