package com.ltm.finance.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val latestVersion: String,
    val currentVersion: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean,
    val fileSizeMb: Double = 0.0
)

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class Available(val info: AppUpdateInfo) : UpdateStatus()
    object UpToDate : UpdateStatus()
    data class Downloading(val progressPercent: Int) : UpdateStatus()
    data class ReadyToInstall(val apkFile: File) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

class AppUpdateManager(private val context: Context) {

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status = _status.asStateFlow()

    fun getCurrentVersionName(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    suspend fun checkForUpdates(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        _status.value = UpdateStatus.Checking
        try {
            val url = URL("https://api.github.com/repos/tranlocbtr99/ltm-finance-android/releases/latest")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "LTM-Finance-App")
                connectTimeout = 8000
                readTimeout = 8000
            }

            if (conn.responseCode != 200) {
                _status.value = UpdateStatus.Error("Không thể kết nối máy chủ cập nhật (HTTP ${conn.responseCode})")
                return@withContext null
            }

            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JsonParser.parseString(responseText).asJsonObject

            val tagName = json.get("tag_name")?.asString?.removePrefix("v") ?: "1.0.0"
            val body = json.get("body")?.asString ?: "Cập nhật cải tiến giao diện và hiệu năng."
            val assets = json.getAsJsonArray("assets")

            var downloadUrl = ""
            var sizeBytes = 0L

            for (element in assets) {
                val assetObj = element.asJsonObject
                val name = assetObj.get("name")?.asString ?: ""
                if (name.endsWith(".apk", ignoreCase = true)) {
                    downloadUrl = assetObj.get("browser_download_url")?.asString ?: ""
                    sizeBytes = assetObj.get("size")?.asLong ?: 0L
                    break
                }
            }

            val currentVer = getCurrentVersionName()
            val isNewer = compareVersions(tagName, currentVer) > 0

            val updateInfo = AppUpdateInfo(
                latestVersion = tagName,
                currentVersion = currentVer,
                releaseNotes = body,
                downloadUrl = downloadUrl,
                isUpdateAvailable = isNewer && downloadUrl.isNotBlank(),
                fileSizeMb = sizeBytes / (1024.0 * 1024.0)
            )

            if (updateInfo.isUpdateAvailable) {
                _status.value = UpdateStatus.Available(updateInfo)
            } else {
                _status.value = UpdateStatus.UpToDate
            }

            updateInfo
        } catch (e: Exception) {
            _status.value = UpdateStatus.Error("Lỗi kiểm tra cập nhật: ${e.localizedMessage ?: "Mạng không khả dụng"}")
            null
        }
    }

    suspend fun downloadAndInstall(downloadUrl: String) = withContext(Dispatchers.IO) {
        try {
            _status.value = UpdateStatus.Downloading(0)
            val url = URL(downloadUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 15000
                readTimeout = 30000
            }

            val fileLength = conn.contentLength
            val apkFile = File(context.cacheDir, "ltm_update.apk")
            if (apkFile.exists()) apkFile.delete()

            conn.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val data = ByteArray(8192)
                    var total: Long = 0
                    var count: Int
                    var lastPercent = 0

                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        output.write(data, 0, count)
                        if (fileLength > 0) {
                            val percent = ((total * 100) / fileLength).toInt()
                            if (percent != lastPercent) {
                                lastPercent = percent
                                _status.value = UpdateStatus.Downloading(percent)
                            }
                        }
                    }
                }
            }

            _status.value = UpdateStatus.ReadyToInstall(apkFile)
            installApk(apkFile)
        } catch (e: Exception) {
            _status.value = UpdateStatus.Error("Lỗi tải bản cập nhật: ${e.localizedMessage ?: "Đã ngắt kết nối"}")
        }
    }

    fun installApk(apkFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _status.value = UpdateStatus.Error("Không thể mở trình cài đặt: ${e.localizedMessage}")
        }
    }

    fun resetStatus() {
        _status.value = UpdateStatus.Idle
    }

    private fun compareVersions(ver1: String, ver2: String): Int {
        val parts1 = ver1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = ver2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val v1 = parts1.getOrElse(i) { 0 }
            val v2 = parts2.getOrElse(i) { 0 }
            if (v1 != v2) return v1.compareTo(v2)
        }
        return 0
    }
}
