import com.geniusdevelops.adonplay.BuildConfig
import com.geniusdevelops.adonplay.app.api.services.DeviceServices
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // Or MoshiConverterFactory

object APIManager {

    val devices: DeviceServices by lazy {
        val retrofit = retrofitBuilder()
        retrofit.create(DeviceServices::class.java)
    }

    private fun retrofitBuilder(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Or MoshiConverterFactory
            .build()
    }
}
   