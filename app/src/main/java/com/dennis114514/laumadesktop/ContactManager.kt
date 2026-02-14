package com.dennis114514.laumadesktop

import android.os.Environment
import android.util.Log
import com.dennis114514.laumadesktop.model.ContactInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream

/**
 * 联系人信息管理工具类
 * 用于读取和管理Download/LaumaDesktop/ContractInformation.json中的联系人信息
 */
object ContactManager {
    
    /**
     * 获取联系人信息文件路径
     * @return 联系人信息文件的绝对路径
     */
    private fun getContactInfoFilePath(): String {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadDir, "LaumaDesktop/ContractInformation.json").absolutePath
    }
    
    /**
     * 获取图片存储目录路径
     * @return 图片存储目录的绝对路径
     */
    private fun getImagesDirPath(): String {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val imagesDir = File(downloadDir, "LaumaDesktop/Images").absolutePath
        Log.d("ContactManager", "图片目录路径: $imagesDir, 目录存在: ${File(imagesDir).exists()}")
        return imagesDir
    }
    
    /**
     * 异步读取联系人信息列表
     * @return 联系人信息列表，如果文件不存在或读取失败则返回空列表
     */
    suspend fun loadContacts(): List<ContactInfo> = withContext(Dispatchers.IO) {
        try {
            val filePath = getContactInfoFilePath()
            Log.d("ContactManager", "尝试读取联系人文件: $filePath")
            
            val contactFile = File(filePath)
            if (!contactFile.exists()) {
                Log.w("ContactManager", "联系人文件不存在: $filePath")
                return@withContext emptyList()
            }
            
            Log.d("ContactManager", "文件存在，大小: ${contactFile.length()} 字节")
            
            val inputStream = FileInputStream(contactFile)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()
            
            Log.d("ContactManager", "读取到的JSON内容: $jsonString")
            
            val jsonArray = JSONArray(jsonString)
            val contacts = mutableListOf<ContactInfo>()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val contact = ContactInfo(
                    name = jsonObject.optString("name", ""),
                    qq = jsonObject.optString("QQ", ""),
                    phone = jsonObject.optString("Phone", ""),
                    image = jsonObject.optString("Image", ""),
                    audio = jsonObject.optString("Audio", "")
                )
                Log.d("ContactManager", "加载联系人: ${contact.name}, 图片: ${contact.image}")
                contacts.add(contact)
            }
            
            Log.d("ContactManager", "总共加载了 ${contacts.size} 个联系人")
            contacts
        } catch (e: Exception) {
            Log.e("ContactManager", "加载联系人失败", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 获取联系人头像文件的完整路径
     * @param imageName 头像文件名
     * @return 头像文件的完整路径，如果文件不存在则返回null
     */
    fun getContactImagePath(imageName: String): String? {
        if (imageName.isBlank()) {
            Log.w("ContactManager", "图片文件名为空")
            return null
        }
        
        val imagesDir = getImagesDirPath()
        val imagePath = File(imagesDir, imageName).absolutePath
        val imageFile = File(imagePath)
        
        Log.d("ContactManager", "解析图片路径 - 文件名: $imageName, 图片目录: $imagesDir, 完整路径: $imagePath, 文件存在: ${imageFile.exists()}")
        
        return if (imageFile.exists()) {
            Log.d("ContactManager", "图片文件存在: $imagePath")
            imagePath
        } else {
            Log.w("ContactManager", "图片文件不存在: $imagePath")
            null
        }
    }
    
    /**
     * 检查联系人信息文件是否存在
     * @return 联系人信息文件是否存在
     */
    suspend fun isContactInfoExists(): Boolean = withContext(Dispatchers.IO) {
        File(getContactInfoFilePath()).exists()
    }
    
    /**
     * 获取联系人总数
     * @return 联系人数量
     */
    suspend fun getContactCount(): Int {
        return loadContacts().size
    }
}