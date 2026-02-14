package com.dennis114514.laumadesktop.FirstUseGuide
//引导用户设置联系人
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dennis114514.laumadesktop.FirstUseGuide.ui.theme.LaumaDesktopTheme
import java.io.File

class ContactSettingsGuide : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ContactSettingsScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 示例文本
        Text(
            text = "示例文本",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )
        
        // 打开配置文件按钮
        Button(
            onClick = {
                // 构建配置文件路径
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val configFile = File(downloadDir, "LaumaDesktop/ContractInformation.json")
                
                // 检查文件是否存在
                if (configFile.exists()) {
                    try {
                        // 使用FileProvider生成content:// URI
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            configFile
                        )
                        
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/json")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // 直接提示用户无法打开文件
                        android.widget.Toast.makeText(
                            context,
                            "无法找到合适的应用打开配置文件，请手动查找文件进行编辑\n错误: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "配置文件不存在，请确保已完成初始化流程",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(48.dp)
        ) {
            Text(
                text = "打开配置文件",
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 我已完成配置按钮
        Button(
            onClick = {
                // 跳转到结束引导页面
                val intent = Intent(context, EndGuide::class.java)
                context.startActivity(intent)
                // 安全地关闭当前Activity
                (context as? ComponentActivity)?.finish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "我已完成配置",
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactSettingsPreview() {
    LaumaDesktopTheme {
        ContactSettingsScreen()
    }
}