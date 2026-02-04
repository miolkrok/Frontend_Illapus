package com.example.illapus.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Convierte una cadena Base64 a Bitmap
     */
    fun base64ToBitmap(base64String: String?): Bitmap? {
        return try {
            if (base64String.isNullOrEmpty()) return null

            // Limpiar la cadena Base64 de caracteres de nueva línea o espacios
            val cleanBase64 = base64String.replace("\n", "").replace("\r", "")

            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte una imagen Uri a Base64
     */
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene el nombre del archivo desde una Uri
     */
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = "imagen_${System.currentTimeMillis()}.jpg"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex) ?: fileName
            }
        }

        return fileName
    }

    /**
     * Obtiene el tipo MIME desde una Uri
     */
    fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri) ?: "image/jpeg"
    }

    /**
     * Obtiene el tamaño del archivo desde una Uri
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst()) {
                size = cursor.getLong(sizeIndex)
            }
        }

        return size
    }
}
