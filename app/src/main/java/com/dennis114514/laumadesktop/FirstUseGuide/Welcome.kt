package com.dennis114514.laumadesktop.FirstUseGuide
//首次打开的欢迎页面
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dennis114514.laumadesktop.FirstUseGuide.ui.theme.LaumaDesktopTheme

class Welcome : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WelcomeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isChecked by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // 欢迎文本
            Text(
                text = "你好，欢迎使用LaumaDesktop",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 48.dp)
            )
        
        // 用户协议复选框区域
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            // 复选框和文本行
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isChecked = !isChecked }
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                
                // 可点击的用户协议文本
                Text(
                    text = buildAnnotatedString {
                        append("同意")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("用户协议")
                        }
                    },
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        // 跳转到用户协议网页
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Dennis114514/LaumaDesktop/blob/master/UserAgreement.md"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
        
        // 下一步按钮
        Button(
            onClick = {
                if (isChecked) {
                    // 跳转到权限申请页面
                    val intent = Intent(context, PermissionApplication::class.java)
                    context.startActivity(intent)
                    // 关闭当前页面
                    (context as? ComponentActivity)?.finish()
                } else {
                    // 显示提示信息
                    Toast.makeText(context, "请同意用户协议", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isChecked,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isChecked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                },
                contentColor = if (isChecked) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            ),
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

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    LaumaDesktopTheme {
        WelcomeScreen()
    }
}
