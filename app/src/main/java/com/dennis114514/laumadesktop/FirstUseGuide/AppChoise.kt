package com.dennis114514.laumadesktop.FirstUseGuide

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.dennis114514.laumadesktop.FirstUseGuide.ui.theme.LaumaDesktopTheme
import com.dennis114514.laumadesktop.JsonConfigUtil
import kotlinx.coroutines.runBlocking

/**
 * 应用选择页面
 * 分两个阶段：
 * 1. 选择桌面显示的应用（多选）
 * 2. 选择用于看电视的应用（单选）
 */
class AppChoise : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppChoiceScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * 可安装应用信息数据类
 * 避免与已有AppInfo类名冲突
 */
data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val icon: androidx.compose.ui.graphics.painter.Painter
)

/**
 * 应用选择屏幕
 */
@Composable
fun AppChoiceScreen(modifier: Modifier = Modifier) {
    // 页面状态：true为选择桌面应用，false为选择电视应用
    var isDesktopAppSelection by remember { mutableStateOf(true) }
    // 已选择的应用包名列表
    val selectedApps = remember { mutableStateListOf<String>() }
    // 单选的电视应用包名
    var selectedTvApp by remember { mutableStateOf("") }
    // 应用列表数据
    var appList by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
    
    val context = LocalContext.current
    
    // 加载应用列表
    LaunchedEffect(Unit) {
        appList = loadInstalledApps(context)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isDesktopAppSelection) {
            // 第一阶段：选择桌面应用
            DesktopAppSelection(
                appList = appList,
                selectedApps = selectedApps,
                onNextClick = {
                    // 保存桌面应用选择结果
                    runBlocking {
                        JsonConfigUtil.setApps(selectedApps.joinToString(","))
                    }
                    // 切换到电视应用选择
                    isDesktopAppSelection = false
                }
            )
        } else {
            // 第二阶段：选择电视应用
            TvAppSelection(
                appList = appList,
                selectedTvApp = selectedTvApp,
                onTvAppSelected = { packageName ->
                    selectedTvApp = packageName
                },
                onNextClick = {
                    // 保存电视应用选择结果
                    runBlocking {
                        JsonConfigUtil.setApptv(selectedTvApp)
                    }
                    // 跳转到联系人设置引导页面
                    val intent = Intent(context, ContactSettingsGuide::class.java)
                    context.startActivity(intent)
                    (context as AppChoise).finish()
                }
            )
        }
    }
}

/**
 * 桌面应用选择界面
 */
@Composable
fun DesktopAppSelection(
    appList: List<InstalledAppInfo>,
    selectedApps: MutableList<String>,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题
        Text(
            text = "选择桌面应用",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // 说明文字
        Text(
            text = "请选择您希望在桌面上显示的应用程序",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // 应用列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
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
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 下一步按钮
        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = selectedApps.isNotEmpty()
        ) {
            Text(text = "下一步", fontSize = 16.sp)
        }
    }
}

/**
 * 电视应用选择界面
 */
@Composable
fun TvAppSelection(
    appList: List<InstalledAppInfo>,
    selectedTvApp: String,
    onTvAppSelected: (String) -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 主标题
        Text(
            text = "选择电视应用",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        // 说明文本
        Text(
            text = "请选择您常用的视频播放或电视应用，该应用将在特定场景下被快速启动",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // 应用列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(appList) { appInfo ->
                TvAppListItem(
                    appInfo = appInfo,
                    isSelected = appInfo.packageName == selectedTvApp,
                    onSelected = {
                        onTvAppSelected(appInfo.packageName)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 下一步按钮
        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = selectedTvApp.isNotEmpty()
        ) {
            Text(text = "完成设置", fontSize = 16.sp)
        }
    }
}

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
            .padding(vertical = 4.dp)
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
 * 电视应用列表项（单选样式）
 */
@Composable
fun TvAppListItem(
    appInfo: InstalledAppInfo,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelected() },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
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
                modifier = Modifier.weight(1f),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            // 选中标记
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 加载已安装的可启动应用列表
 */
fun loadInstalledApps(context: android.content.Context): List<InstalledAppInfo> {
    val packageManager = context.packageManager
    val apps = mutableListOf<InstalledAppInfo>()
    
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
    
    // 按应用名称排序
    return apps.sortedBy { it.appName }
}

@Preview(showBackground = true)
@Composable
fun AppChoicePreview() {
    LaumaDesktopTheme {
        AppChoiceScreen()
    }
}