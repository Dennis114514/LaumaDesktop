package com.dennis114514.laumadesktop.FirstUseGuide

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dennis114514.laumadesktop.FirstUseGuide.ui.theme.LaumaDesktopTheme
import java.io.File
import java.io.FileOutputStream

class InitializeProvider : ComponentActivity() {
    
    // 应用初始化状态
    private var isInitialized by mutableStateOf(false)
    private var initializationProgress by mutableStateOf(0f)
    private var currentStatus by mutableStateOf("正在检查目录结构...")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LaumaDesktopTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InitializationContent(
                        isInitialized = isInitialized,
                        progress = initializationProgress,
                        status = currentStatus,
                        onNavigateToAppDownload = { navigateToAppDownload() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    @Composable
    private fun InitializationContent(
        isInitialized: Boolean,
        progress: Float,
        status: String,
        onNavigateToAppDownload: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        // 页面加载后开始检查和初始化
        LaunchedEffect(Unit) {
            checkAndInitialize()
        }
        
        InitializationScreen(
            isInitialized = isInitialized,
            progress = progress,
            status = status,
            onNavigateToAppDownload = onNavigateToAppDownload,
            modifier = modifier
        )
    }
    
    /**
     * 检查并初始化应用所需的目录结构
     */
    private fun checkAndInitialize() {
        Thread {
            try {
                // 获取Download目录路径
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val laumaDesktopDir = File(downloadDir, "LaumaDesktop")
                
                updateProgress(0.1f, "检查LaumaDesktop主目录...")
                Thread.sleep(500)
                
                // 检查主目录是否存在
                if (!laumaDesktopDir.exists()) {
                    updateProgress(0.2f, "创建LaumaDesktop主目录...")
                    if (!laumaDesktopDir.mkdirs()) {
                        throw Exception("无法创建LaumaDesktop目录")
                    }
                    Thread.sleep(500)
                }
                
                // 检查子目录
                val imagesDir = File(laumaDesktopDir, "Images")
                val audiosDir = File(laumaDesktopDir, "Audios")
                
                updateProgress(0.4f, "检查Images和Audios子目录...")
                Thread.sleep(500)
                
                // 创建缺失的子目录
                if (!imagesDir.exists()) {
                    updateProgress(0.5f, "创建Images目录...")
                    if (!imagesDir.mkdirs()) {
                        throw Exception("无法创建Images目录")
                    }
                    Thread.sleep(300)
                }
                
                if (!audiosDir.exists()) {
                    updateProgress(0.6f, "创建Audios目录...")
                    if (!audiosDir.mkdirs()) {
                        throw Exception("无法创建Audios目录")
                    }
                    Thread.sleep(300)
                }
                
                // 检查并复制JSON配置文件
                updateProgress(0.7f, "检查配置文件...")
                Thread.sleep(500)
                
                val settingsFile = File(laumaDesktopDir, "Settings.json")
                val contactInfoFile = File(laumaDesktopDir, "ContractInformation.json")
                val readmeFile = File(laumaDesktopDir, "README.txt")
                
                if (!settingsFile.exists() || !contactInfoFile.exists() || !readmeFile.exists()) {
                    updateProgress(0.8f, "复制配置文件...")
                    
                    // 先复制Settings.json
                    try {
                        copyAssetFile("Settings.json", settingsFile)
                        updateProgress(0.85f, "Settings.json复制完成")
                        Thread.sleep(300)
                    } catch (e: Exception) {
                        throw Exception("复制Settings.json失败: ${e.message}")
                    }
                    
                    // 再复制ContractInformation.json
                    try {
                        // 添加调试信息
                        val assetFiles = assets.list("")
                        println("Assets文件列表: ${assetFiles?.joinToString(", ")}")
                        
                        copyAssetFile("ContractInformation.json", contactInfoFile)
                        updateProgress(0.87f, "ContractInformation.json复制完成")
                        Thread.sleep(300)
                        
                        // 验证复制结果
                        if (contactInfoFile.exists()) {
                            println("ContractInformation.json文件已创建，大小: ${contactInfoFile.length()} 字节")
                        } else {
                            throw Exception("文件未成功创建")
                        }
                    } catch (e: Exception) {
                        val errorMsg = "复制ContractInformation.json失败: ${e.message}"
                        println(errorMsg)
                        throw Exception(errorMsg)
                    }
                    
                    // 复制README.txt
                    try {
                        copyAssetFile("README.txt", readmeFile)
                        updateProgress(0.9f, "README.txt复制完成")
                        Thread.sleep(300)
                        
                        // 验证复制结果
                        if (readmeFile.exists()) {
                            println("README.txt文件已创建，大小: ${readmeFile.length()} 字节")
                        } else {
                            throw Exception("README文件未成功创建")
                        }
                    } catch (e: Exception) {
                        val errorMsg = "复制README.txt失败: ${e.message}"
                        println(errorMsg)
                        throw Exception(errorMsg)
                    }
                }
                
                // 最终验证
                updateProgress(0.9f, "验证目录结构完整性...")
                Thread.sleep(500)
                
                if (validateDirectoryStructure(laumaDesktopDir)) {
                    updateProgress(1.0f, "初始化完成！")
                    Thread.sleep(300)
                    runOnUiThread {
                        isInitialized = true
                        currentStatus = "初始化完成"
                    }
                } else {
                    throw Exception("目录结构验证失败")
                }
                
            } catch (e: Exception) {
                println("初始化过程发生异常: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    val errorMessage = "初始化失败: ${e.message ?: "未知错误"}"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    currentStatus = errorMessage
                }
            }
        }.start()
    }
    
    /**
     * 更新初始化进度
     */
    private fun updateProgress(progress: Float, status: String) {
        runOnUiThread {
            initializationProgress = progress
            currentStatus = status
        }
    }
    
    /**
     * 从assets复制文件到指定位置
     * 增加详细的错误处理和验证
     */
    private fun copyAssetFile(assetName: String, targetFile: File) {
        try {
            println("开始复制文件: $assetName 到 ${targetFile.absolutePath}")
            
            // 检查assets中是否存在该文件
            val assetList = assets.list("")
            if (assetList != null && !assetList.contains(assetName)) {
                val fileList = assetList.joinToString(", ")
                throw Exception("Assets中不存在文件: $assetName，可用文件: $fileList")
            }
            
            // 确保目标目录存在
            val parentDir = targetFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw Exception("无法创建目标目录: ${parentDir.absolutePath}")
                }
                println("创建目录: ${parentDir.absolutePath}")
            }
            
            // 如果目标文件已存在，先删除
            if (targetFile.exists()) {
                targetFile.delete()
                println("删除已存在的文件: ${targetFile.absolutePath}")
            }
            
            // 执行文件复制
            assets.open(assetName).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    var totalBytes = 0L
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead
                    }
                    outputStream.flush()
                    println("复制完成: $assetName，总字节数: $totalBytes")
                }
            }
            
            // 验证文件是否成功创建
            if (!targetFile.exists()) {
                throw Exception("文件复制后不存在: ${targetFile.absolutePath}")
            }
            
            if (targetFile.length() == 0L) {
                throw Exception("文件复制后大小为0: ${targetFile.absolutePath}")
            }
            
            println("文件复制验证通过: ${targetFile.absolutePath}，大小: ${targetFile.length()} 字节")
            
        } catch (e: Exception) {
            println("复制文件 $assetName 失败: ${e.message}")
            e.printStackTrace()
            throw Exception("复制文件 $assetName 失败: ${e.message}")
        }
    }
    
    /**
     * 验证目录结构完整性
     */
    private fun validateDirectoryStructure(baseDir: File): Boolean {
        val imagesDir = File(baseDir, "Images")
        val audiosDir = File(baseDir, "Audios")
        val settingsFile = File(baseDir, "Settings.json")
        val contactInfoFile = File(baseDir, "ContractInformation.json")
        val readmeFile = File(baseDir, "README.txt")
                        
        return baseDir.exists() && 
               imagesDir.exists() && imagesDir.isDirectory &&
               audiosDir.exists() && audiosDir.isDirectory &&
               settingsFile.exists() && settingsFile.isFile &&
               contactInfoFile.exists() && contactInfoFile.isFile &&
               readmeFile.exists() && readmeFile.isFile
    }
    
    /**
     * 跳转到应用下载页面
     */
    private fun navigateToAppDownload() {
        val intent = Intent(this, AppDownload::class.java)
        startActivity(intent)
        finish() // 关闭当前页面
    }
}

@Composable
fun InitializationScreen(
    isInitialized: Boolean,
    progress: Float,
    status: String,
    onNavigateToAppDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 标题
        Text(
            text = "应用初始化",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 状态卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 进度指示器
                AnimatedVisibility(
                    visible = !isInitialized,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 状态文本
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = if (isInitialized) MaterialTheme.colorScheme.primary else Color.Unspecified
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 下一步按钮
                AnimatedVisibility(
                    visible = isInitialized,
                    enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut(tween(300)) + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Button(
                        onClick = onNavigateToAppDownload,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "下一步",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 说明文本
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "初始化说明：",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "• 在Download目录创建LaumaDesktop文件夹\n" +
                           "• 创建Images和Audios子目录\n" +
                           "• 复制Settings.json和ContractInformation.json配置文件，以及README.txt文档\n" +
                           "• 准备应用运行所需的基础环境",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InitializationScreenPreview() {
    LaumaDesktopTheme {
        InitializationScreen(
            isInitialized = false,
            progress = 0.6f,
            status = "正在初始化...",
            onNavigateToAppDownload = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InitializationCompletePreview() {
    LaumaDesktopTheme {
        InitializationScreen(
            isInitialized = true,
            progress = 1.0f,
            status = "初始化完成",
            onNavigateToAppDownload = {}
        )
    }
}