package com.dennis114514.laumadesktop.Settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.dennis114514.laumadesktop.Settings.ui.theme.LaumaDesktopTheme
import java.io.File

/**
 * 联系人设置页面
 * 提供跳转到联系人配置文件的按钮
 */
class ContractSettings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                ContractSettingsScreen(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "联系人设置",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "联系人配置",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            Text(
                text = "点击下方按钮编辑联系人配置文件，\n您可以添加或修改联系人信息。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
            
            // 编辑配置文件按钮
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
                            Toast.makeText(
                                context,
                                "无法找到合适的应用打开配置文件，请手动查找文件进行编辑\n错误: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "配置文件不存在，请确保已完成初始化流程",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "编辑"
                )
                Text(
                    text = "编辑联系人配置文件",
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "请点击上面的按钮，在弹出的窗口中选择MT管理器的”定位到文件位置“，先阅读文件夹中的README.txt文件，然后按照提示进行配置\n如果README文件不慎丢失,请访问https://github.com/Dennis114514/LaumaDesktop/blob/master/app/src/main/assets/README.txt",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContractSettingsPreview() {
    LaumaDesktopTheme {
        ContractSettingsScreen(onNavigateBack = {})
    }
}