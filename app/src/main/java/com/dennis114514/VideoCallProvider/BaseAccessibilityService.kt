package com.dennis114514.VideoCallProvider

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 基础无障碍服务类
 * 提供基本的无障碍服务功能和事件监听
 */
class BaseAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "BaseAccessibilityService"
        private var instance: BaseAccessibilityService? = null
        
        /**
         * 获取服务实例
         */
        fun getInstance(): BaseAccessibilityService? {
            return instance
        }
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "无障碍服务已连接")
        
        // 服务连接成功后的初始化操作
        initializeService()
    }
    
    override fun onUnbind(intent: android.content.Intent?): Boolean {
        Log.d(TAG, "无障碍服务已断开")
        instance = null
        return super.onUnbind(intent)
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        try {
            // 记录事件信息
            Log.d(TAG, "事件类型: ${AccessibilityEvent.eventTypeToString(event.eventType)}")
            Log.d(TAG, "包名: ${event.packageName}")
            Log.d(TAG, "类名: ${event.className}")
            
            // 根据不同事件类型进行处理
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    handleWindowStateChange(event)
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    handleClickEvent(event)
                }
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                    handleFocusEvent(event)
                }
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    handleNotificationEvent(event)
                }
            }
            
            // 处理当前窗口的节点信息
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                processRootNode(rootNode)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "处理无障碍事件时出错: ${e.message}")
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "无障碍服务被中断")
    }
    
    /**
     * 初始化服务
     */
    private fun initializeService() {
        Log.d(TAG, "初始化无障碍服务")
        // 可以在这里添加服务初始化逻辑
    }
    
    /**
     * 处理窗口状态变化事件
     */
    private fun handleWindowStateChange(event: AccessibilityEvent) {
        Log.d(TAG, "窗口状态变化 - 包名: ${event.packageName}, 类名: ${event.className}")
        // 可以在这里添加窗口状态变化的处理逻辑
    }
    
    /**
     * 处理点击事件
     */
    private fun handleClickEvent(event: AccessibilityEvent) {
        Log.d(TAG, "点击事件 - 包名: ${event.packageName}, 类名: ${event.className}")
        // 可以在这里添加点击事件的处理逻辑
    }
    
    /**
     * 处理焦点事件
     */
    private fun handleFocusEvent(event: AccessibilityEvent) {
        Log.d(TAG, "焦点事件 - 包名: ${event.packageName}, 类名: ${event.className}")
        // 可以在这里添加焦点事件的处理逻辑
    }
    
    /**
     * 处理通知事件
     */
    private fun handleNotificationEvent(event: AccessibilityEvent) {
        Log.d(TAG, "通知事件 - 包名: ${event.packageName}")
        // 可以在这里添加通知事件的处理逻辑
    }
    
    /**
     * 处理根节点信息
     */
    private fun processRootNode(node: AccessibilityNodeInfo) {
        // 可以在这里遍历和处理UI节点
        traverseNodes(node)
    }
    
    /**
     * 遍历节点树
     */
    private fun traverseNodes(node: AccessibilityNodeInfo, level: Int = 0) {
        // 打印当前节点信息
        val indent = "  ".repeat(level)
        Log.d(TAG, "${indent}节点: ${node.className} - 文本: ${node.text} - 可点击: ${node.isClickable}")
        
        // 递归遍历子节点
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { childNode ->
                traverseNodes(childNode, level + 1)
            }
        }
    }
    
    /**
     * 查找特定文本的节点
     */
    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        return findNodeByText(rootInActiveWindow, text)
    }
    
    private fun findNodeByText(node: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (node == null) return null
        
        // 检查当前节点
        if (text.equals(node.text?.toString(), ignoreCase = true)) {
            return node
        }
        
        // 递归检查子节点
        for (i in 0 until node.childCount) {
            val result = findNodeByText(node.getChild(i), text)
            if (result != null) {
                return result
            }
        }
        
        return null
    }
    
    /**
     * 点击指定文本的节点
     */
    fun clickNodeByText(text: String): Boolean {
        val node = findNodeByText(text)
        return if (node != null && node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "成功点击节点: $text")
            true
        } else {
            Log.d(TAG, "未找到可点击的节点: $text")
            false
        }
    }
    

}