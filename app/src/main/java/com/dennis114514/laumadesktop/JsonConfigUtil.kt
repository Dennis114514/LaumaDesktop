package com.dennis114514.laumadesktop

import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * JSON配置文件工具类
 * 用于读取和写入位于Download/LaumaDesktop目录下的Settings.json配置文件
 * 遵循项目规范：JSON文件必须使用数组作为顶层结构
 */
object JsonConfigUtil {
    
    /**
     * 获取配置文件路径
     * @return 配置文件的绝对路径
     */
    private fun getConfigFilePath(): String {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadDir, "LaumaDesktop/Settings.json").absolutePath
    }
    
    /**
     * 异步读取配置文件
     * @return JSONObject配置对象，如果文件不存在或读取失败则返回null
     */
    suspend fun readConfig(): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val configFile = File(getConfigFilePath())
            if (!configFile.exists()) {
                return@withContext null
            }
            
            val inputStream = FileInputStream(configFile)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()
            
            val jsonArray = JSONArray(jsonString)
            if (jsonArray.length() > 0) {
                jsonArray.getJSONObject(0)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 异步写入配置文件
     * @param config 要写入的配置对象
     * @return 写入是否成功
     */
    suspend fun writeConfig(config: JSONObject): Boolean = withContext(Dispatchers.IO) {
        try {
            val configFile = File(getConfigFilePath())
            val parentDir = configFile.parentFile
            
            // 确保父目录存在
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }
            
            val jsonArray = JSONArray()
            jsonArray.put(config)
            
            val outputStream = FileOutputStream(configFile)
            outputStream.write(jsonArray.toString(2).toByteArray())
            outputStream.close()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取Apps字段值（桌面应用包名列表）
     * @return 逗号分隔的应用包名字符串
     */
    suspend fun getApps(): String {
        val config = readConfig()
        return config?.optString("Apps", "") ?: ""
    }
    
    /**
     * 设置Apps字段值
     * @param apps 逗号分隔的应用包名字符串
     * @return 设置是否成功
     */
    suspend fun setApps(apps: String): Boolean {
        val config = readConfig() ?: JSONObject()
        config.put("Apps", apps)
        return writeConfig(config)
    }
    
    /**
     * 获取Apptv字段值（电视应用包名）
     * @return 电视应用包名
     */
    suspend fun getApptv(): String {
        val config = readConfig()
        return config?.optString("Apptv", "") ?: ""
    }
    
    /**
     * 设置Apptv字段值
     * @param apptv 电视应用包名
     * @return 设置是否成功
     */
    suspend fun setApptv(apptv: String): Boolean {
        val config = readConfig() ?: JSONObject()
        config.put("Apptv", apptv)
        return writeConfig(config)
    }
    
    /**
     * 获取Password字段值
     * @return 密码字符串
     */
    suspend fun getPassword(): String {
        val config = readConfig()
        return config?.optString("Password", "") ?: ""
    }
    
    /**
     * 设置Password字段值
     * @param password 密码字符串
     * @return 设置是否成功
     */
    suspend fun setPassword(password: String): Boolean {
        val config = readConfig() ?: JSONObject()
        config.put("Password", password)
        return writeConfig(config)
    }
    
    /**
     * 检查配置文件是否存在
     * @return 配置文件是否存在
     */
    suspend fun isConfigExists(): Boolean = withContext(Dispatchers.IO) {
        File(getConfigFilePath()).exists()
    }
    
    /**
     * 创建默认配置文件
     * @return 创建是否成功
     */
    suspend fun createDefaultConfig(): Boolean {
        val defaultConfig = JSONObject().apply {
            put("Apps", "")
            put("Apptv", "")
            put("Password", "")
        }
        return writeConfig(defaultConfig)
    }
}