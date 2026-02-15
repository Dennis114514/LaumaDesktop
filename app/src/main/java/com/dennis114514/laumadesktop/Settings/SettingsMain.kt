package com.dennis114514.laumadesktop.Settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dennis114514.laumadesktop.Settings.ui.theme.LaumaDesktopTheme

/**
 * 设置主页面
 * 包含所有设置选项的入口
 */
class SettingsMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                SettingsScreen(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "设置",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            
            // 应用管理部分
            SettingsSection(title = "应用管理") {
                SettingItem(
                    icon = Icons.Default.Star,
                    title = "全部应用",
                    description = "查看系统中的所有应用",
                    onClick = { 
                        context.startActivity(Intent(context, AllAppsActivity::class.java))
                    }
                )
                
                SettingItem(
                    icon = Icons.Default.Home,
                    title = "设为默认启动器",
                    description = "将LaumaDesktop设置为系统默认桌面",
                    onClick = { 
                        context.startActivity(Intent(context, SetLauncher::class.java))
                    }
                )
                
                SettingItem(
                    icon = Icons.Default.PlayArrow,
                    title = "快捷应用",
                    description = "设置常用应用的快捷方式",
                    onClick = { 
                        context.startActivity(Intent(context, ListAppsSet::class.java))
                    }
                )
                
                SettingItem(
                    icon = Icons.Default.Phone,
                    title = "快速打开",
                    description = "配置点击时间时可以快速打开的应用",
                    onClick = { 
                        context.startActivity(Intent(context, QuickStartSet::class.java))
                    }
                )
            }
            
            // 个人化设置部分
            SettingsSection(title = "个性化") {
                SettingItem(
                    icon = Icons.Default.Person,
                    title = "联系人设置",
                    description = "管理联系人信息",
                    onClick = { 
                        context.startActivity(Intent(context, ContractSettings::class.java))
                    }
                )
            }
            
            // 关于部分
            SettingsSection(title = "关于") {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "关于LaumaDesktop",
                    description = "查看应用版本和开发者信息",
                    onClick = { 
                        context.startActivity(Intent(context, AboutActivity::class.java))
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            content()
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "进入设置",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "详细说明内容占位符",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    LaumaDesktopTheme {
        SettingsScreen(onNavigateBack = {})
    }
}