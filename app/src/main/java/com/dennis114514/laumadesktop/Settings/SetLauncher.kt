package com.dennis114514.laumadesktop.Settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dennis114514.laumadesktop.Settings.ui.theme.LaumaDesktopTheme

class SetLauncher : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                LauncherSetupScreen(
                    onSetDefaultLauncher = { openDefaultLauncherSettings() }
                )
            }
        }
    }
    
    /**
     * 打开系统默认启动器设置页面
     */
    private fun openDefaultLauncherSettings() {
        try {
            // 方式1：尝试打开默认应用设置
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                // 方式2：如果上面的方法不支持，尝试通用的应用设置
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (ex: Exception) {
                // 方式3：打开应用管理列表
                val intent = Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)
                startActivity(intent)
            }
        }
    }
}

@Composable
fun LauncherSetupScreen(
    onSetDefaultLauncher: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 应用图标
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "启动器图标",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 标题
            Text(
                text = "设置为默认启动器",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 描述文本
            Text(
                text = "将LaumaDesktop设置为您的默认桌面启动器，\n享受更便捷的操作体验。\n\n点击下方按钮前往系统设置进行配置。\n\n注意：请确保你配置的联系人信息正确，否则应用无法使用！",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 设置按钮
            Button(
                onClick = onSetDefaultLauncher,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "设为默认启动器",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 提示信息
            Text(
                text = "提示：在系统设置中找到LaumaDesktop并设为默认",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LauncherSetupPreview() {
    LaumaDesktopTheme {
        LauncherSetupScreen(
            onSetDefaultLauncher = {}
        )
    }
}