package com.dennis114514.laumadesktop.FirstUseGuide

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dennis114514.laumadesktop.FirstUseGuide.ui.theme.LaumaDesktopTheme

class AccessibilityManagerGuide : ComponentActivity() {
    
    private var isAccessibilityServiceEnabled by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LaumaDesktopTheme {
                AccessibilityManagerGuideScreen(
                    isAccessibilityServiceEnabled = isAccessibilityServiceEnabled,
                    onOpenAccessibilityManager = { openAccessibilityManager() },
                    onNextStep = { navigateToAppChoice() }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 检查无障碍服务是否已启用
        isAccessibilityServiceEnabled = isLaumaDesktopAccessibilityServiceEnabled()
    }
    
    /**
     * 检查LaumaDesktop的无障碍服务是否已启用
     */
    private fun isLaumaDesktopAccessibilityServiceEnabled(): Boolean {
        return try {
            val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
            val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            
            enabledServices?.contains("com.dennis114514.VideoCallProvider.BaseAccessibilityService") == true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 打开AccessibilityManager应用
     */
    private fun openAccessibilityManager() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.accessibilitymanager")
            if (intent != null) {
                startActivity(intent)
            } else {
                // 如果应用未安装，引导用户到AppDownload页面
                Toast.makeText(this, "请先下载并安装AccessibilityManager应用", Toast.LENGTH_LONG).show()
                navigateToAppDownload()
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "无法打开AccessibilityManager，请确保应用已安装", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * 跳转到应用下载页面
     */
    private fun navigateToAppDownload() {
        val intent = Intent(this, AppDownload::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * 跳转到应用选择页面
     */
    private fun navigateToAppChoice() {
        val intent = Intent(this, AppChoise::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun AccessibilityManagerGuideScreen(
    isAccessibilityServiceEnabled: Boolean,
    onOpenAccessibilityManager: () -> Unit,
    onNextStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 标题
            Text(
                text = "无障碍服务设置",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // 说明卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "请打开AccessibilityManager，按该应用内的提示操作\n无障碍权限将用于自动操作视频通话\n你可以暂时关闭此应用",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 打开AccessibilityManager按钮
            Button(
                onClick = onOpenAccessibilityManager,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "打开AccessibilityManager",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 下一步按钮（仅当无障碍服务已启用时显示）
            if (isAccessibilityServiceEnabled) {
                Button(
                    onClick = onNextStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "下一步",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "请先按照AccessibilityManager应用内的提示完成无障碍服务设置",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccessibilityManagerGuideScreenPreview() {
    LaumaDesktopTheme {
        AccessibilityManagerGuideScreen(
            isAccessibilityServiceEnabled = false,
            onOpenAccessibilityManager = {},
            onNextStep = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccessibilityManagerGuideScreenEnabledPreview() {
    LaumaDesktopTheme {
        AccessibilityManagerGuideScreen(
            isAccessibilityServiceEnabled = true,
            onOpenAccessibilityManager = {},
            onNextStep = {}
        )
    }
}