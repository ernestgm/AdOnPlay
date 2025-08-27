package com.geniusdevelops.adonplay.app.util

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException

fun parseJsonToMap(jsonString: String): Map<String, Any?>? {
    return try {
        val mainJsonObject = JSONObject(jsonString)
        val resultMap = mutableMapOf<String, Any?>()

        // Iterar sobre las claves del objeto JSON principal
        mainJsonObject.keys().forEach { key ->
            when (val value = mainJsonObject.get(key)) {
                is String -> {
                    // Intentar parsear si la cadena es a su vez un JSON
                    try {
                        val nestedJson = JSONObject(value)
                        resultMap[key] =
                            parseJsonToMap(nestedJson.toString()) // Recursivo para el anidado
                    } catch (e: JSONException) {
                        // No es un JSON anidado, es una simple cadena
                        resultMap[key] = value
                    }
                }

                is JSONObject -> {
                    resultMap[key] = jsonObjectToMap(value)
                }

                is JSONArray -> {
                    // Si tuvieras arrays, necesitarías una función para convertirlos a List
                    // resultMap[key] = jsonArrayToList(value)
                    resultMap[key] = value.toString() // O simplemente su representación String
                }

                JSONObject.NULL -> { // Manejar valores nulos explícitos de JSON
                    resultMap[key] = null
                }

                else -> {
                    resultMap[key] = value
                }
            }
        }
        resultMap
    } catch (e: JSONException) {
        println("Error al parsear JSON: ${e.message}")
        null
    }
}

// Helper para convertir un JSONObject interno a Map
fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    jsonObject.keys().forEach { key ->
        when (val value = jsonObject.get(key)) {
            is String -> map[key] = value
            is JSONObject -> map[key] = jsonObjectToMap(value) // Recursivo
            is Boolean -> map[key] = value
            is Int -> map[key] = value
            is Double -> map[key] = value
            is Long -> map[key] = value
            JSONObject.NULL -> map[key] = null
            // Añadir más tipos si es necesario, o un 'else' para convertir a String
            else -> map[key] = value.toString()
        }
    }
    return map
}

//fun main() {
//    val jsonString =
//        """{"identifier":"{\"channel\":\"ChangeDevicesActionsChannel\",\"status\":1}","message":{"type":"ejecute_portrait_change","payload":{"portrait":true}}}"""
//    val resultMap = parseJsonToMap(jsonString)
//
//    if (resultMap != null) {
//        println("Mapa resultante: $resultMap")
//
//        // Acceder a los valores
//        val identifierMap = resultMap["identifier"] as? Map<String, Any?>
//        val channel = identifierMap?.get("channel")
//        val status = identifierMap?.get("status")
//
//        val messageMap = resultMap["message"] as? Map<String, Any?>
//        val messageType = messageMap?.get("type")
//        val payloadMap = messageMap?.get("payload") as? Map<String, Any?>
//        val portrait = payloadMap?.get("portrait")
//
//        println("--- Valores específicos ---")
//        println("Identifier Channel: $channel") // ChangeDevicesActionsChannel
//        println("Identifier Status: $status")   // 1
//        println("Message Type: $messageType")   // ejecute_portrait_change
//        println("Payload Portrait: $portrait")  // true
//    }
//}