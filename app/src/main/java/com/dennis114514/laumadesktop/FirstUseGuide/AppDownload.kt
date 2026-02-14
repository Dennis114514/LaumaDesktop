package com.dennis114514.laumadesktop.FirstUseGuide

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

class AppDownload : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                AppDownloadScreen(
                    onNavigateToNext = { navigateToAccessibilityManagerGuide() }
                )
            }
        }
    }
    
    private fun navigateToAccessibilityManagerGuide() {
        val intent = Intent(this, AccessibilityManagerGuide::class.java)
        startActivity(intent)
        finish()
    }
}

// 应用信息数据类
data class AppInfo(
    val name: String,
    val purpose: String,
    val downloadLinks: List<LinkInfo>,
    var isInstalled: Boolean = false
)

data class LinkInfo(
    val name: String,
    val url: String
)

@Composable
fun AppDownloadScreen(
    onNavigateToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    // 应用列表数据
    val appList = remember {
        listOf(
            AppInfo(
                name = "AccessibilityManager",
                purpose = "用于方便地启动无障碍服务",
                downloadLinks = listOf(
                    LinkInfo("Github", "https://github.com/Dennis114514/AccessibilityManager")
                )
            ),
            AppInfo(
                name = "MT管理器",
                purpose = "用于方便地编辑联系人信息",
                downloadLinks = listOf(
                    LinkInfo("官网", "https://www.mt2.cn/")
                )
            ),
            AppInfo(
                name = "QQ",
                purpose = "视频通话",
                downloadLinks = listOf(
                    LinkInfo("官网", "https://im.qq.com/")
                )
            )
        )
    }
    
    var apps by remember { mutableStateOf(appList) }
    
    // 检查应用安装状态
    LaunchedEffect(Unit) {
        apps = apps.map { app ->
            val isInstalled = try {
                when (app.name) {
                    "AccessibilityManager" -> packageManager.getPackageInfo("com.accessibilitymanager", 0) != null
                    "MT管理器" -> packageManager.getPackageInfo("bin.mt.plus", 0) != null
                    "QQ" -> packageManager.getPackageInfo("com.tencent.mobileqq", 0) != null
                    else -> false
                }
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
            app.copy(isInstalled = isInstalled)
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text(
                text = "请下载以下应用",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // 应用列表
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                apps.forEach { app ->
                    AppItem(
                        app = app,
                        onLinkClick = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // 处理打开链接失败的情况
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 下一步按钮
            Button(
                onClick = onNavigateToNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
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

@Composable
fun AppItem(
    app: AppInfo,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 应用名称和安装状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (app.isInstalled) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已安装",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "已安装",
                            color = Color.Green,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "未安装",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 用途说明
            Text(
                text = app.purpose,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 下载链接
            Text(
                text = "下载链接：",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                app.downloadLinks.forEach { link ->
                    Row(
                        modifier = Modifier
                            .clickable { onLinkClick(link.url) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = link.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppDownloadScreenPreview() {
    LaumaDesktopTheme {
        AppDownloadScreen(
            onNavigateToNext = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppItemPreview() {
    LaumaDesktopTheme {
        AppItem(
            app = AppInfo(
                name = "AccessibilityManager",
                purpose = "用于方便地启动无障碍服务",
                downloadLinks = listOf(
                    LinkInfo("Github", "https://github.com/Dennis114514/AccessibilityManagerSpecial")
                ),
                isInstalled = true
            ),
            onLinkClick = {}
        )
    }
}