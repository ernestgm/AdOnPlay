//package com.geniusdevelop.playmyscreens.app.util
//
//import android.content.Context
//import android.util.Log
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleObserver
//import androidx.lifecycle.OnLifecycleEvent
//import com.geniusdevelop.playmyscreens.app.api.conection.Repository
//import com.geniusdevelop.playmyscreens.app.provider.AppStateProvider
//import com.google.firebase.Firebase
//import com.google.firebase.perf.FirebasePerformance
//import com.google.firebase.perf.performance
//import com.google.firebase.perf.trace
//
//class AppLifecycleObserver(private val deviceId: String, private val context: Context) : LifecycleObserver {
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    fun onEnterForeground() {
//        AppStateProvider.setAppRunning()
//        Repository.wsManager.connect()
//        println("App Running")
//        Firebase.performance.newTrace("app_start_$deviceId").trace {
//            // Update scenario.
//            putAttribute("deviceId", deviceId)
//        }
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    fun onEnterBackground() {
//        AppStateProvider.setAppStopped()
//        Repository.wsManager.disconnect()
//        println("App Stop")
//        Firebase.performance.newTrace("app_stop_$deviceId").trace {
//            // Update scenario.
//            putAttribute("deviceId", deviceId)
//        }
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun onDestroy() {
//        AppStateProvider.setAppRunning()
//        Repository.wsManager.disconnect()
//        println("App Destroy")
//        Firebase.performance.newTrace("app_destroy_$deviceId").trace {
//            // Update scenario.
//            putAttribute("deviceId", deviceId)
//        }
//    }
//}