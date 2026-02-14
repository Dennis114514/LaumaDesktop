package com.dennis114514.laumadesktop

import android.content.Context
import android.content.SharedPreferences

/**
 * 首次使用引导状态管理工具类
 * 用于保存和读取用户是否已完成首次使用引导的状态
 */
class PreferencesManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "lauma_desktop_prefs"
        private const val KEY_FIRST_USE_COMPLETED = "first_use_completed"
        
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        /**
         * 获取PreferencesManager单例实例
         */
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 设置首次使用引导已完成
     */
    fun setFirstUseCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_FIRST_USE_COMPLETED, completed).apply()
    }
    
    /**
     * 检查是否已完成首次使用引导
     */
    fun isFirstUseCompleted(): Boolean {
        return prefs.getBoolean(KEY_FIRST_USE_COMPLETED, false)
    }
}