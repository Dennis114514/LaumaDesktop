package com.dennis114514.laumadesktop

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import com.dennis114514.laumadesktop.FirstUseGuide.Welcome
import com.dennis114514.laumadesktop.Settings.SettingsMain
import com.dennis114514.laumadesktop.model.ContactInfo
import com.dennis114514.laumadesktop.ui.theme.LaumaDesktopTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefsManager = PreferencesManager.getInstance(this)
        if (!prefsManager.isFirstUseCompleted()) {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var contacts by remember { mutableStateOf<List<ContactInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAppList by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contacts = ContactManager.loadContacts()
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (contacts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "未找到联系人信息\n如果因操作不当导致手机无法使用，请下拉通知栏，使用系统提供的快捷按钮打开“设置”应用，卸载本软件")
        }
        return
    }

    val initialPage = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % contacts.size)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { Int.MAX_VALUE }
    )

    val currentContactIndex = pagerState.currentPage % contacts.size
    val currentContact = contacts[currentContactIndex]

    // 使用 Surface 作为底层，确保颜色填满整个窗口（包括系统栏下方）
    // 使用 Surface 作为底层，确保颜色填满整个窗口
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. 顶部栏
                TopActionBar(
                    onShowAppList = { showAppList = true },
                    onOpenSettings = {
                        context.startActivity(Intent(context, SettingsMain::class.java))
                    }
                )

                // 2. 中央/底部内容区
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        // 关键：navigationBarsPadding 确保内容不被底部虚拟按键遮挡，
                        // 但 Box 本身的背景会延伸到最底部
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp) // 距离导航栏的安全间距
                    ) {
                        // 联系人图片轮播 (保持 300dp 保证在大屏小屏都居中)
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentPadding = PaddingValues(horizontal = 80.dp),
                            pageSpacing = 16.dp
                        ) { page ->
                            val contact = contacts[page % contacts.size]
                            val imagePath = ContactManager.getContactImagePath(contact.image)

                            Card(
                                modifier = Modifier
                                    .size(240.dp)
                                    .graphicsLayer {
                                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                                        alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                        val scale = lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                        scaleX = scale
                                        scaleY = scale
                                    },
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                AsyncImage(
                                    model = imagePath?.let { File(it) },
                                    contentDescription = contact.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = currentContact.name,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 操作按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { makeDirectCall(context, currentContact.phone) },
                                modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Default.Call, "拨号", tint = Color.White, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.width(48.dp))
                            IconButton(
                                onClick = { Toast.makeText(context, "未完工", Toast.LENGTH_SHORT).show() },//TODO：实装视频通话功能
                                modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFF2196F3))
                            ) {
                                Icon(Icons.Default.PlayArrow, "视频", tint = Color.White, modifier = Modifier.size(36.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 导航按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "前一张", modifier = Modifier.size(48.dp))
                            }
                            Spacer(modifier = Modifier.width(100.dp))
                            IconButton(
                                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "后一张", modifier = Modifier.size(48.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAppList) {
        AppListDialog(onDismiss = { showAppList = false })
    }
}

@Composable
fun TopActionBar(onShowAppList: () -> Unit, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var currentDayOfWeek by remember { mutableStateOf("") }
    var lunarText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        var lastDateStr = ""
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
            if (todayStr != lastDateStr) {
                currentDate = SimpleDateFormat("M月d日", Locale.getDefault()).format(now)
                currentDayOfWeek = SimpleDateFormat("EEEE", Locale.CHINA).format(now)
                val lunarInfo = LunarDateManager.getCurrentLunarDate(context)
                lunarText = LunarDateManager.formatLunarDisplay(lunarInfo)
                lastDateStr = todayStr
            }
            delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShowAppList, modifier = Modifier.size(40.dp)) {
            Icon(
                // 使用 Menu 代替 Apps
                imageVector = Icons.Default.Menu,
                contentDescription = "应用列表",
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    scope.launch {
                        val apptvPackage = JsonConfigUtil.getApptv()
                        if (apptvPackage.isNotEmpty()) {
                            openAppByPackageName(context, apptvPackage)
                        } else {
                            Toast.makeText(context, "未配置快捷应用(Apptv)", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = currentTime,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "$currentDate $currentDayOfWeek", fontSize = 16.sp)
                if (lunarText.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = lunarText,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        IconButton(onClick = onOpenSettings, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun AppListDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var appPackages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val appsString = JsonConfigUtil.getApps()
        appPackages = appsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        isLoading = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "快捷应用",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (appPackages.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("请在设置中配置包名列表", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(appPackages) { pkg ->
                            AppItem(pkg, onClick = {
                                openAppByPackageName(context, pkg)
                                onDismiss()
                            })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
fun AppItem(packageName: String, onClick: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    
    val appInfo = remember(packageName) {
        try {
            val info = pm.getApplicationInfo(packageName, 0)
            val name = pm.getApplicationLabel(info).toString()
            val icon = pm.getApplicationIcon(info)
            Pair(name, icon)
        } catch (e: Exception) {
            Pair(packageName, null)
        }
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appInfo.second != null) {
                Image(
                    bitmap = appInfo.second!!.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Icon(
                    // 使用 Face 代替 Android
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = appInfo.first,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun openAppByPackageName(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "未找到应用: $packageName", Toast.LENGTH_SHORT).show()
    }
}

private fun makeDirectCall(context: Context, phoneNumber: String) {
    if (phoneNumber.isBlank()) return
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
        == PackageManager.PERMISSION_GRANTED) {
        context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber")))
    } else {
        ActivityCompat.requestPermissions(context as ComponentActivity, arrayOf(Manifest.permission.CALL_PHONE), 1001)
    }
}
