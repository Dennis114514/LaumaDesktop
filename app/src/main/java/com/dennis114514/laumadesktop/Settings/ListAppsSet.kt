package com.dennis114514.laumadesktop.Settings

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.dennis114514.laumadesktop.JsonConfigUtil
import com.dennis114514.laumadesktop.Settings.ui.theme.LaumaDesktopTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * 快捷应用配置页面
 * 用于选择显示在桌面左上角抽屉中的应用
 */
class ListAppsSet : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                ListAppsSetScreen(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListAppsSetScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var appList by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
    val selectedApps = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    
    // 加载已安装的应用列表
    LaunchedEffect(Unit) {
        appList = loadDesktopApps(context)
        // 加载已保存的选择
        val savedApps = runBlocking { JsonConfigUtil.getApps() }
        selectedApps.addAll(savedApps.split(",").filter { it.isNotBlank() })
        isLoading = false
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "快捷应用设置",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text(
                text = "选择桌面快捷应用",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            // 说明文字
            Text(
                text = "请选择您希望在桌面左上角抽屉中显示的应用程序，\n这些应用可以通过抽屉快速访问。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // 应用列表
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(appList) { appInfo ->
                        AppListItem(
                            appInfo = appInfo,
                            isSelected = appInfo.packageName in selectedApps,
                            onToggleSelection = { isSelected ->
                                if (isSelected) {
                                    selectedApps.add(appInfo.packageName)
                                } else {
                                    selectedApps.remove(appInfo.packageName)
                                }
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            // 保存按钮
            Button(
                onClick = {
                    isSaving = true
                    // 保存选择到配置文件
                    runBlocking {
                        val result = JsonConfigUtil.setApps(selectedApps.joinToString(","))
                        withContext(Dispatchers.Main) {
                            isSaving = false
                            if (result) {
                                Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            } else {
                                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isSaving && selectedApps.isNotEmpty()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "保存中...")
                } else {
                    Text(text = "保存设置", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * 可安装应用信息数据类
 */
data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val icon: androidx.compose.ui.graphics.painter.Painter
)

/**
 * 应用列表项（复选框样式）
 */
@Composable
fun AppListItem(
    appInfo: InstalledAppInfo,
    isSelected: Boolean,
    onToggleSelection: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelection(!isSelected) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 应用图标
            Image(
                painter = appInfo.icon,
                contentDescription = "${appInfo.appName}图标",
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 应用名称
            Text(
                text = appInfo.appName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            // 复选框
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // 空白占位符保持对齐
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

/**
 * 加载桌面应用列表
 */
suspend fun loadDesktopApps(context: android.content.Context): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
    val packageManager = context.packageManager
    val apps = mutableListOf<InstalledAppInfo>()
    
    try {
        // 获取所有已安装的应用
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (appInfo in installedApps) {
            // 过滤掉系统应用和不可启动的应用
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                try {
                    // 检查是否有启动Activity
                    val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                    if (launchIntent != null) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val icon = packageManager.getApplicationIcon(appInfo)
                        
                        // 将Drawable转换为Compose Painter
                        val bitmap = icon.toBitmap()
                        val painter = androidx.compose.ui.graphics.painter.BitmapPainter(bitmap.asImageBitmap())
                        
                        apps.add(
                            InstalledAppInfo(
                                appName = appName,
                                packageName = appInfo.packageName,
                                icon = painter
                            )
                        )
                    }
                } catch (e: Exception) {
                    // 忽略无法获取信息的应用
                    continue
                }
            }
        }
    } catch (e: Exception) {
        // 忽略异常
    }
    
    // 按应用名称排序
    apps.sortedBy { it.appName }
}

@Preview(showBackground = true)
@Composable
fun ListAppsSetPreview() {
    LaumaDesktopTheme {
        ListAppsSetScreen(onNavigateBack = {})
    }
}