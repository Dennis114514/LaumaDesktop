package com.dennis114514.laumadesktop

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * 农历日期管理工具类
 * 用于查询和获取农历日期信息
 */
object LunarDateManager {
    private const val TAG = "LunarDateManager"
    private const val API_TOKEN = "LwExDtUWhF3rH5ib"
    private const val API_URL = "https://v2.alapi.cn/api/lunar"
    
    // SSL上下文，用于处理SSL握手问题
    private val sslContext: SSLContext by lazy {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            
            SSLContext.getInstance("TLS").apply {
                init(null, trustAllCerts, SecureRandom())
            }
        } catch (e: Exception) {
            Log.e(TAG, "SSL上下文初始化失败", e)
            SSLContext.getDefault()
        }
    }
    
    /**
     * 农历日期信息数据类
     */
    data class LunarDateInfo(
        val lunarMonthChinese: String = "",  // 农历月份（中文）
        val lunarDayChinese: String = "",    // 农历日期（中文）
        val isValid: Boolean = false         // 数据是否有效
    )
    
    /**
     * 检查网络连接状态
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }
    
    /**
     * 异步获取指定日期的农历信息
     * @param date 指定日期，默认为当前日期
     * @param context 上下文，用于网络状态检查
     * @return 农历日期信息，如果查询失败则返回无效数据
     */
    suspend fun getLunarDate(context: Context, date: Date = Date()): LunarDateInfo = withContext(Dispatchers.IO) {
        try {
            // 检查网络连接
            if (!isNetworkAvailable(context)) {
                Log.e(TAG, "网络不可用")
                return@withContext LunarDateInfo(isValid = false)
            }
            
            // 格式化日期为API需要的格式
            val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH", Locale.getDefault())
            val dateString = dateFormat.format(date)
            
            // 构建请求URL
            val url = URL("$API_URL?token=$API_TOKEN&date=$dateString")
            
            // 发送HTTP请求
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            // 配置SSL（如果是HTTPS连接）
            if (connection is HttpsURLConnection) {
                connection.sslSocketFactory = sslContext.socketFactory
                connection.hostnameVerifier = javax.net.ssl.HostnameVerifier { _, _ -> true }
            }
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                // 解析JSON响应
                val jsonResponse = JSONObject(response)
                val data = jsonResponse.optJSONObject("data")
                
                if (data != null) {
                    val lunarMonth = data.optString("lunar_month_chinese", "")
                    val lunarDay = data.optString("lunar_day_chinese", "")
                    
                    if (lunarMonth.isNotEmpty() && lunarDay.isNotEmpty()) {
                        return@withContext LunarDateInfo(
                            lunarMonthChinese = lunarMonth,
                            lunarDayChinese = lunarDay,
                            isValid = true
                        )
                    }
                }
            } else {
                Log.e(TAG, "API请求失败，响应码: $responseCode")
            }
            
            connection.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "获取农历日期失败", e)
        }
        
        // 返回默认的无效数据
        LunarDateInfo(isValid = false)
    }
    
    /**
     * 获取当前日期的农历信息
     * @param context 上下文
     * @return 农历日期信息
     */
    suspend fun getCurrentLunarDate(context: Context): LunarDateInfo {
        return getLunarDate(context, Date())
    }
    
    /**
     * 格式化农历日期显示文本
     * @param lunarInfo 农历信息
     * @return 格式化后的显示文本，如果数据无效则返回空字符串
     */
    fun formatLunarDisplay(lunarInfo: LunarDateInfo): String {
        return if (lunarInfo.isValid) {
            "${lunarInfo.lunarMonthChinese}${lunarInfo.lunarDayChinese}"
        } else {
            ""
        }
    }
}