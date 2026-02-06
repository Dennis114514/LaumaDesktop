package com.dennis114514.laumadesktop

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dennis114514.laumadesktop.ui.theme.LaumaDesktopTheme
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

private const val REQUEST_CODE_PERMISSIONS = 100

private val REQUIRED_PERMISSIONS = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
    //区分Android 13及以上的权限获取
    arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
} else {
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                ImageGalleryScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun ImageGalleryScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 检查权限
    val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
    
    if (!hasPermission) {
        ActivityCompat.requestPermissions(
            context as ComponentActivity,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }
    
    val imageList = remember { mutableStateListOf<String>() }
    
    // 加载Download/LaumaDesktop/images目录下的图片
    if (hasPermission && imageList.isEmpty()) {
        val laumaDesktopDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LaumaDesktop")
        val imagesDir = File(laumaDesktopDir, "images")
        val imageFiles = if (imagesDir.exists()) {         //TODO：按照用户指定的顺序进行排序，按顺序展示
            imagesDir.listFiles { _, name ->
                name.lowercase().endsWith(".png") || name.lowercase().endsWith(".jpg") || name.lowercase().endsWith(".jpeg")
            }
        } else {
            null
        }
        
        imageFiles?.forEach { file ->
            imageList.add(file.absolutePath)
        }
    }
    
    val pagerState = rememberPagerState(
        pageCount = { imageList.size }
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height((context.resources.displayMetrics.heightPixels * 0.25).roundToInt().dp) // 屏幕高度的1/4
        ) { index ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageList[index])
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}