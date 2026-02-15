package com.dennis114514.laumadesktop.Settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dennis114514.laumadesktop.Settings.ui.theme.LaumaDesktopTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                AboutScreen()
            }
        }
    }
}

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var clickCount by remember { mutableIntStateOf(0) }
    var showEasterEgg by remember { mutableStateOf(false) }
    
    // 彩蛋文本
    val easterEggText = """
        他们过于古老，过于刻板，信仰旧时代的残余。
        
        他们相信旧时代是他们庇护所，相信老旧的经验主义将驱散所有的伤病。
        
        他们向「发展」祈祷，问「时间」啊，您将何时回应我们的需要？
        
        得不到回答，他们便又问，「时代」啊，将何时回应我们的愿望？
        
        于是Lauma走出，走到「便携式亚分子质能衍构核心造物」中。
        
        于是现代朝着古老的行礼，于是LaumaDesktop成为了「旧世界之余晖」的代理。
    """.trimIndent()
    
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 标题
            Text(
                text = "关于",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 应用名称
            Text(
                text = "LaumaDesktop",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            
            // 引号内的神秘文字（可点击触发彩蛋）
            val quoteText = """
                “他们过于古老，过于刻板，信仰……。
                
                他们相信……是他们庇护所，相信……将驱散所有的伤病。
                
                他们向……祈祷，问……啊，您将何时回应……？
                
                得不到回答，他们便又问，……啊，……将何时回应我们的愿望？
                
                于是……走出，走到……。
                
                于是……朝着……行礼，于是……成为了……的代理。”
            """.trimIndent()
            
            Text(
                text = quoteText,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clickCount++
                        if (clickCount >= 7) {
                            showEasterEgg = true
                            clickCount = 0 // 重置计数器
                        }
                    }
                    .padding(16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 应用介绍
            Text(
                text = "LaumaDesktop是一个专门为无法使用手机的老人制做的软件，旨在为他们能够使用智能手机进行基本的通信和娱乐。\n视频通话功能尚未实装，敬您期待",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 开源协议
            Text(
                text = "本软件依据GPLv3开源，请自觉遵守开源协议",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 镇压链接
            ClickableTextItem(
                text = "镇压",
                url = "https://enipc.court.gov.cn/zh-cn/news/view-3655.html"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // GitHub链接
            ClickableTextItem(
                text = "Github：https://github.com/Dennis114514/LaumaDesktop",
                url = "https://github.com/Dennis114514/LaumaDesktop"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 反馈说明
            Text(
                text = "如果软件有问题，欢迎在Github Issues中反馈，我们也鼓励更多的人提交Pull Request，为软件添加功能。",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 作者信息
            ClickableTextItem(
                text = "作者：Dennis114514",
                url = "https://space.bilibili.com/1759492467"
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 用户协议
            ClickableTextItem(
                text = "使用此软件即视为同意用户协议",
                url = "https://github.com/Dennis114514/LaumaDesktop/blob/master/UserAgreement.md"
            )
        }
    }
    
    // 彩蛋对话框
    if (showEasterEgg) {
        AlertDialog(
            onDismissRequest = { showEasterEgg = false },
            text = {
                Text(
                    text = easterEggText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showEasterEgg = false }) {
                    Text("知道了")
                }
            }
        )
    }
}

@Composable
fun ClickableTextItem(
    text: String,
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Text(
        text = text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // 忽略打开链接的异常
                }
            }
    )
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    LaumaDesktopTheme {
        AboutScreen()
    }
}