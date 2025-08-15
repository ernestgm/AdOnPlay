/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geniusdevelops.adonplay.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import kotlin.random.Random

class DeviceUtils(
    private val context: Context,
) {
    fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun generateSixDigitRandom(): Int {
        return Random.nextInt(100000, 1000000)
    }

    fun isAppInstalled(packageManager: PackageManager, packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            //FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }

    suspend fun isInternetAvailable(): Boolean {
        return (isNetworkAvailable(context) && hasInternetAccess())
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            // For devices below Android M (API level 23)
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo?.isConnected == true
        }
    }

    private suspend fun hasInternetAccess(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://www.google.com") // Known reliable server
                    .build()
                val response = client.newCall(request).execute()
                println("Response: ${response.isSuccessful}")
                response.isSuccessful
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    fun checkWDStatus(): Boolean {
        var running = false
        val uri = Uri.parse("content://com.geniusdevelop.watchdog.provider/app_a_state")
        val cursor = context.contentResolver.query(uri, null, null, null, null)

        cursor?.let {
            if (it.moveToFirst()) {
                val appState = it.getInt(it.getColumnIndexOrThrow("appState"))
                if (appState == 1) {
                    running = true
                }
            }
            it.close()
        } ?: run {
            println("I can verify player state")
        }
        return running
    }
}
