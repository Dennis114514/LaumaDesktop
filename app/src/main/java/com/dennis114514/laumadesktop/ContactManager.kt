package com.dennis114514.laumadesktop

import android.os.Environment
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
        return imagesDir
    }
    
    /**
     * 异步读取联系人信息列表
     * @return 联系人信息列表，如果文件不存在或读取失败则返回空列表
     */
    suspend fun loadContacts(): List<ContactInfo> = withContext(Dispatchers.IO) {
        try {
            val filePath = getContactInfoFilePath()
            
            val contactFile = File(filePath)
            if (!contactFile.exists()) {
                return@withContext emptyList()
            }
            
            val inputStream = FileInputStream(contactFile)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()
            
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
                contacts.add(contact)
            }
            contacts
        } catch (e: Exception) {
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
            return null
        }
        
        val imagesDir = getImagesDirPath()
        val imagePath = File(imagesDir, imageName).absolutePath
        val imageFile = File(imagePath)
        
        return if (imageFile.exists()) {
            imagePath
        } else {
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