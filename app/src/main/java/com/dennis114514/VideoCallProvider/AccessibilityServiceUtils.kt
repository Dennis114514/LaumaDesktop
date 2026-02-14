package com.dennis114514.VideoCallProvider

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 无障碍服务工具类
 * 提供常用的无障碍操作方法
 */
class AccessibilityServiceUtils {
    
    companion object {
        private const val TAG = "AccessibilityServiceUtils"
        
        /**
         * 查找包含指定文本的节点
         */
        fun findNodeContainingText(rootNode: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
            if (rootNode == null) return null
            
            // 检查当前节点文本是否包含目标文本
            if (rootNode.text?.toString()?.contains(text, ignoreCase = true) == true) {
                return rootNode
            }
            
            // 递归检查子节点
            for (i in 0 until rootNode.childCount) {
                rootNode.getChild(i)?.let { childNode ->
                    val result = findNodeContainingText(childNode, text)
                    if (result != null) {
                        return result
                    }
                }
            }
            
            return null
        }
        
        /**
         * 查找指定类名的节点
         */
        fun findNodeByClassName(rootNode: AccessibilityNodeInfo?, className: String): AccessibilityNodeInfo? {
            if (rootNode == null) return null
            
            // 检查当前节点类名
            if (className.equals(rootNode.className?.toString(), ignoreCase = true)) {
                return rootNode
            }
            
            // 递归检查子节点
            for (i in 0 until rootNode.childCount) {
                rootNode.getChild(i)?.let { childNode ->
                    val result = findNodeByClassName(childNode, className)
                    if (result != null) {
                        return result
                    }
                }
            }
            
            return null
        }
        
        /**
         * 点击包含指定文本的节点
         */
        fun clickNodeContainingText(service: AccessibilityService, text: String): Boolean {
            val rootNode = service.rootInActiveWindow
            val node = findNodeContainingText(rootNode, text)
            
            return if (node != null && node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "成功点击包含文本 '$text' 的节点")
                true
            } else {
                Log.d(TAG, "未找到可点击的包含文本 '$text' 的节点")
                false
            }
        }
        
        /**
         * 输入文本到指定节点
         */
        fun inputTextToNode(node: AccessibilityNodeInfo?, text: String): Boolean {
            if (node == null) return false
            
            val arguments = android.os.Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            
            val result = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            if (result) {
                Log.d(TAG, "成功输入文本: $text")
            } else {
                Log.d(TAG, "输入文本失败: $text")
            }
            
            return result
        }
        
        /**
         * 在包含指定文本的节点中输入文本
         */
        fun inputTextToNodeContainingText(service: AccessibilityService, searchText: String, inputText: String): Boolean {
            val rootNode = service.rootInActiveWindow
            val node = findNodeContainingText(rootNode, searchText)
            
            return if (node != null) {
                inputTextToNode(node, inputText)
            } else {
                Log.d(TAG, "未找到包含文本 '$searchText' 的节点")
                false
            }
        }
        
        /**
         * 滚动到指定文本的节点
         */
        fun scrollToNodeContainingText(service: AccessibilityService, text: String): Boolean {
            val rootNode = service.rootInActiveWindow
            val node = findNodeContainingText(rootNode, text)
            
            return if (node != null) {
                val result = node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ||
                           node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                if (result) {
                    Log.d(TAG, "成功滚动到包含文本 '$text' 的节点")
                } else {
                    Log.d(TAG, "滚动到包含文本 '$text' 的节点失败")
                }
                result
            } else {
                Log.d(TAG, "未找到包含文本 '$text' 的节点")
                false
            }
        }
        
        /**
         * 执行长按操作
         */
        fun longClickNode(node: AccessibilityNodeInfo?): Boolean {
            if (node == null) return false
            
            val result = node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
            if (result) {
                Log.d(TAG, "成功长按节点")
            } else {
                Log.d(TAG, "长按节点失败")
            }
            
            return result
        }
        
        /**
         * 长按包含指定文本的节点
         */
        fun longClickNodeContainingText(service: AccessibilityService, text: String): Boolean {
            val rootNode = service.rootInActiveWindow
            val node = findNodeContainingText(rootNode, text)
            
            return if (node != null) {
                longClickNode(node)
            } else {
                Log.d(TAG, "未找到包含文本 '$text' 的节点")
                false
            }
        }
        
        /**
         * 获取当前活动窗口的所有节点信息（用于调试）
         */
        fun dumpWindowHierarchy(service: AccessibilityService) {
            val rootNode = service.rootInActiveWindow
            if (rootNode != null) {
                Log.d(TAG, "=== 当前窗口层次结构 ===")
                dumpNodeHierarchy(rootNode, 0)
                Log.d(TAG, "========================")
            } else {
                Log.d(TAG, "无法获取当前窗口根节点")
            }
        }
        
        /**
         * 递归打印节点层次结构
         */
        private fun dumpNodeHierarchy(node: AccessibilityNodeInfo, level: Int) {
            val indent = "  ".repeat(level)
            val clickable = if (node.isClickable) "✓" else "✗"
            val focusable = if (node.isFocusable) "✓" else "✗"
            val enabled = if (node.isEnabled) "✓" else "✗"
            
            Log.d(TAG, "${indent}[${level}] ${node.className} " +
                    "| 文本: '${node.text}' " +
                    "| 可点击: $clickable " +
                    "| 可聚焦: $focusable " +
                    "| 启用: $enabled " +
                    "| 包名: ${node.packageName}")
            
            // 递归打印子节点
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { childNode ->
                    dumpNodeHierarchy(childNode, level + 1)
                }
            }
        }
        
        /**
         * 等待指定文本出现
         */
        fun waitForText(service: AccessibilityService, text: String, timeoutMs: Long = 10000): Boolean {
            val startTime = System.currentTimeMillis()
            
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                val rootNode = service.rootInActiveWindow
                if (findNodeContainingText(rootNode, text) != null) {
                    Log.d(TAG, "找到文本 '$text'")
                    return true
                }
                
                try {
                    Thread.sleep(500) // 每500ms检查一次
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
            
            Log.d(TAG, "等待文本 '$text' 超时")
            return false
        }
    }
}