package com.dennis114514.VideoCallProvider

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 无障碍服务工具类
 * 提供常用的无障碍操作方法
 */
class AccessibilityServiceUtils {
    
    companion object {
        
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
                true
            } else {
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
                node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ||
                node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            } else {
                false
            }
        }
        
        /**
         * 执行长按操作
         */
        fun longClickNode(node: AccessibilityNodeInfo?): Boolean {
            if (node == null) return false
            
            val result = node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
            
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
                false
            }
        }
    }
}