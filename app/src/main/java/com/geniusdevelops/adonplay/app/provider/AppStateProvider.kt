package com.geniusdevelops.adonplay.app.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

// AppAContentProvider.kt en App A
class AppStateProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.geniusdevelop.adonplay.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/app_a_state")

        private const val APP_STATE_RUNNING = 1
        private const val APP_STATE_STOPPED = 0

        private var appState = APP_STATE_STOPPED

        // Método para que App A registre su estado como "running"
        fun setAppRunning() {
            appState = APP_STATE_RUNNING
        }

        // Método para que App A registre su estado como "stopped"
        fun setAppStopped() {
            appState = APP_STATE_STOPPED
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val matrixCursor = MatrixCursor(arrayOf("appState"))
        matrixCursor.addRow(arrayOf(appState))
        return matrixCursor
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }
}