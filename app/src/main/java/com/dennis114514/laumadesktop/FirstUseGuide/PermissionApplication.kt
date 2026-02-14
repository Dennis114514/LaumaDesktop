package com.dennis114514.laumadesktop.FirstUseGuide
//一般权限申请
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.util.Log
import com.dennis114514.laumadesktop.FirstUseGuide.ui.theme.LaumaDesktopTheme

class PermissionApplication : ComponentActivity() {
    
    // 权限列表 - 按重要性和依赖关系排序
    @SuppressLint("InlinedApi")
    val permissions = mutableListOf(
        PermissionInfo("存储权限", getStoragePermissions()), // 优先申请存储权限
        PermissionInfo("应用列表权限", getInstalledAppsPermissions()),
        PermissionInfo("电话权限", arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE)),
        PermissionInfo("短信权限", arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)),
        PermissionInfo("悬浮窗权限", arrayOf()) // 特殊处理
    )
    
    private var currentPermissionIndex = 0
    var grantedPermissionsCount = 0
        private set
    
    // 是否已获得所有权限
    private var isAllPermissionsGranted by mutableStateOf(false)
    
    // 对话框显示回调
    var showPermissionDeniedDialog: (String) -> Unit = {}
    
    // 权限申请回调
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        handlePermissionsResult(permissionsResult)
    }
    
    // 权限申请超时处理
    private var permissionTimeoutHandler: android.os.Handler? = null
    private val PERMISSION_TIMEOUT = 5000L // 5秒超时
    
    // 周期性检查处理器
    private var periodicCheckHandler: android.os.Handler? = null
    private val PERIODIC_CHECK_INTERVAL = 2000L // 2秒间隔
    
    // 悬浮窗权限回调
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(this)) {
            Log.d("PermissionCheck", "悬浮窗权限已授予")
            // 注意：这里不增加grantedPermissionsCount，因为checkAllPermissions会重新计算
            checkNextPermission()
        } else {
            Log.d("PermissionCheck", "悬浮窗权限被拒绝")
            showPermissionDeniedDialog("悬浮窗权限")
        }
    }
    
    // SAF文件访问回调
    private val safPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val treeUri = result.data?.data
            if (treeUri != null) {
                // 保存访问权限
                try {
                    contentResolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    Log.d("PermissionCheck", "SAF权限已保存: $treeUri")
                } catch (e: Exception) {
                    Log.e("PermissionCheck", "保存SAF权限失败: ${e.message}")
                }
                // 强制重新检查所有权限状态
                checkAllPermissions()
                checkNextPermission()
            }
        } else {
            Log.d("PermissionCheck", "SAF权限申请被拒绝或取消")
            showPermissionDeniedDialog("文件访问权限")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaumaDesktopTheme {
                PermissionApplicationScreen(
                    isAllPermissionsGranted = isAllPermissionsGranted,
                    onGetPermissionsClick = { requestNextPermission() },
                    onCheckPermissions = { checkAllPermissions() }
                )
            }
        }
        
        // 页面加载时检查权限状态
        checkAllPermissions()
        
        // 启动周期性检查，确保UI状态同步
        startPeriodicPermissionCheck()
    }
    
    /**
     * 启动周期性权限检查
     */
    private fun startPeriodicPermissionCheck() {
        periodicCheckHandler = android.os.Handler(mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                if (!isAllPermissionsGranted) {
                    Log.d("PermissionCheck", "执行周期性权限检查")
                    checkAllPermissions()
                    // 继续周期性检查
                    periodicCheckHandler?.postDelayed(this, PERIODIC_CHECK_INTERVAL)
                } else {
                    Log.d("PermissionCheck", "所有权限已获得，停止周期性检查")
                    stopPeriodicPermissionCheck()
                }
            }
        }
        periodicCheckHandler?.postDelayed(runnable, PERIODIC_CHECK_INTERVAL)
        Log.d("PermissionCheck", "启动周期性权限检查")
    }
    
    /**
     * 停止周期性权限检查
     */
    private fun stopPeriodicPermissionCheck() {
        periodicCheckHandler?.removeCallbacksAndMessages(null)
        periodicCheckHandler = null
        Log.d("PermissionCheck", "停止周期性权限检查")
    }
    
    /**
     * 检查是否需要申请存储权限
     * @return true表示需要申请，false表示已有足够权限
     */
    private fun needToRequestStoragePermission(): Boolean {
        val hasTraditionalPermission = checkTraditionalStoragePermission()
        val hasSAFPermission = checkSAFPermission()
        
        // 如果任一权限已获得，则不需要申请
        val result = !(hasTraditionalPermission || hasSAFPermission)
        Log.d("PermissionCheck", "是否需要申请存储权限: $result (传统: $hasTraditionalPermission, SAF: $hasSAFPermission)")
        
        return result
    }
    
    private fun requestSAFPermission() {
        // 显示详细提示信息
        Toast.makeText(this, "请在文件选择器中选择Download文件夹并点击'使用此文件夹'", Toast.LENGTH_LONG).show()
        
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            // 设置初始URI为Download目录
            val downloadUri = DocumentsContract.buildRootUri("com.android.externalstorage.documents", "primary:Download")
            // 注意：某些设备可能不支持这个参数
            // putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadUri)
        }
        safPermissionLauncher.launch(intent)
    }
    
    private fun requestNextPermission() {
        if (currentPermissionIndex < permissions.size) {
            val currentPermission = permissions[currentPermissionIndex]
            when (currentPermission.name) {
                "悬浮窗权限" -> requestOverlayPermission()
                "存储权限" -> {
                    // 智能选择存储权限申请方式
                    requestSmartStoragePermission()
                }
                else -> requestNormalPermission(currentPermission.permissions)
            }
        } else {
            // 所有权限申请完毕
            checkAllPermissions()
        }
    }
    
    /**
     * 智能申请存储权限 - 根据当前已有的权限情况选择最优申请方式
     */
    private fun requestSmartStoragePermission() {
        val hasTraditionalPermission = checkTraditionalStoragePermission()
        val hasSAFPermission = checkSAFPermission()
        
        Log.d("PermissionCheck", "智能存储权限申请 - 传统权限: $hasTraditionalPermission, SAF权限: $hasSAFPermission")
        
        when {
            hasTraditionalPermission || hasSAFPermission -> {
                // 任一权限已获得，标记存储权限为已授予并继续
                Log.d("PermissionCheck", "已有存储权限(${if (hasTraditionalPermission) "传统" else "SAF"})，标记为已授予")
                // 注意：这里不增加grantedPermissionsCount，因为checkAllPermissions会重新计算
                checkNextPermission()
            }
            else -> {
                // 两种权限都没有
                if (shouldUseSAF()) {
                    // 推荐使用SAF
                    Log.d("PermissionCheck", "无存储权限，申请SAF权限")
                    requestSAFPermission()
                } else {
                    // 使用传统权限
                    Log.d("PermissionCheck", "无存储权限，申请传统权限")
                    requestNormalPermission(getStoragePermissions())
                }
            }
        }
    }
    
    private fun requestNormalPermission(permissionArray: Array<String>) {
        if (permissionArray.isNotEmpty()) {
            // 启动超时保护
            startPermissionTimeout()
            requestPermissionLauncher.launch(permissionArray)
        } else {
            // 如果没有需要申请的权限，直接检查下一个
            checkNextPermission()
        }
    }
    
    /**
     * 启动权限申请超时保护
     */
    private fun startPermissionTimeout() {
        permissionTimeoutHandler = android.os.Handler(mainLooper)
        permissionTimeoutHandler?.postDelayed({
            // 超时处理：跳过当前权限申请
            Toast.makeText(this, "权限申请超时，跳过此项", Toast.LENGTH_SHORT).show()
            checkNextPermission()
        }, PERMISSION_TIMEOUT)
    }
    
    /**
     * 取消权限申请超时保护
     */
    private fun cancelPermissionTimeout() {
        permissionTimeoutHandler?.removeCallbacksAndMessages(null)
        permissionTimeoutHandler = null
    }
    
    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            overlayPermissionLauncher.launch(intent)
        } else {
            grantedPermissionsCount++
            checkNextPermission()
        }
    }
    
    private fun handlePermissionsResult(permissionsResult: Map<String, Boolean>) {
        // 取消超时保护
        cancelPermissionTimeout()
        
        var allGranted = true
        var deniedPermissionName = ""
        
        Log.d("PermissionCheck", "收到权限申请结果:")
        for ((permission, isGranted) in permissionsResult) {
            Log.d("PermissionCheck", "权限 $permission: ${if (isGranted) "已授予" else "被拒绝"}")
            if (!isGranted) {
                allGranted = false
                deniedPermissionName = getPermissionDisplayName(permission)
                break
            }
        }
        
        if (allGranted) {
            Log.d("PermissionCheck", "权限组申请成功")
            // 强制重新检查所有权限状态
            checkAllPermissions()
            checkNextPermission()
        } else {
            Log.d("PermissionCheck", "权限申请失败: $deniedPermissionName")
            showPermissionDeniedDialog(deniedPermissionName)
        }
    }
    
    private fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.QUERY_ALL_PACKAGES -> "应用列表权限"
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE -> "电话权限"
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS -> "短信权限"
            else -> "权限"
        }
    }
    
    private fun checkNextPermission() {
        currentPermissionIndex++
        if (currentPermissionIndex < permissions.size) {
            requestNextPermission()
        } else {
            // 所有权限申请完成，重新检查权限状态
            checkAllPermissions()
        }
    }
    
    /**
     * 检查所有权限是否已获得
     * 修改逻辑：只要获得所有必需的权限就显示下一步按钮
     */
    private fun checkAllPermissions() {
        var essentialPermissionsGranted = 0
        val totalEssentialPermissions = permissions.size
        
        // 检查每个必需权限
        for (permissionInfo in permissions) {
            val isGranted = when (permissionInfo.name) {
                "悬浮窗权限" -> {
                    val granted = Settings.canDrawOverlays(this)
                    Log.d("PermissionCheck", "悬浮窗权限: ${if (granted) "已授予" else "未授予"}")
                    granted
                }
                "存储权限" -> {
                    // 存储权限特殊处理 - 只要有一种存储权限就行
                    val granted = checkStoragePermission()
                    Log.d("PermissionCheck", "存储权限: ${if (granted) "已授予" else "未授予"}")
                    granted
                }
                else -> {
                    // 其他权限组检查
                    if (permissionInfo.permissions.isEmpty()) {
                        // 无具体权限要求的项目默认为已授予
                        Log.d("PermissionCheck", "${permissionInfo.name}: 无具体权限要求，视为已授予")
                        true
                    } else {
                        val allGranted = permissionInfo.permissions.all { permission ->
                            val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                Log.d("PermissionCheck", "权限 $permission: 已授予")
                            } else {
                                Log.d("PermissionCheck", "权限 $permission: 未授予")
                            }
                            granted
                        }
                        allGranted
                    }
                }
            }
            
            if (isGranted) {
                essentialPermissionsGranted++
            }
        }
        
        Log.d("PermissionCheck", "必需权限总数: $totalEssentialPermissions, 已获得: $essentialPermissionsGranted")
        grantedPermissionsCount = essentialPermissionsGranted
        isAllPermissionsGranted = essentialPermissionsGranted >= totalEssentialPermissions
        Log.d("PermissionCheck", "是否获得所有必需权限: $isAllPermissionsGranted")
    }
    
    /**
     * 检查存储权限
     * @return 是否具有存储权限（SAF或传统权限任一即可）
     */
    private fun checkStoragePermission(): Boolean {
        // 检查传统存储权限
        val hasTraditionalPermission = checkTraditionalStoragePermission()
        
        // 检查SAF权限
        val hasSAFPermission = checkSAFPermission()
        
        val result = hasTraditionalPermission || hasSAFPermission
        Log.d("PermissionCheck", "存储权限检查 - 传统权限: $hasTraditionalPermission, SAF权限: $hasSAFPermission, 最终结果: $result")
        
        return result
    }
    
    /**
     * 检查传统存储权限
     * @return 是否具有传统存储权限
     */
    private fun checkTraditionalStoragePermission(): Boolean {
        val storagePermissions = getStoragePermissions()
        return if (storagePermissions.isEmpty()) {
            // Android 5.x及以下无需检查
            true
        } else {
            storagePermissions.all { permission ->
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
    
    fun goToNextPage() {
        // 停止所有后台检查
        stopPeriodicPermissionCheck()
        cancelPermissionTimeout()
        
        val intent = Intent(this, InitializeProvider::class.java)
        startActivity(intent)
        finish()
    }
    
    fun exitApp() {
        // 停止所有后台检查
        stopPeriodicPermissionCheck()
        cancelPermissionTimeout()
        
        finishAffinity()
    }
    
    /**
     * 根据Android版本获取适当的存储权限
     * @return 权限数组
     */
    private fun getStoragePermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11及以上，使用READ_EXTERNAL_STORAGE
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Android 6.0-10，使用传统存储权限
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            else -> {
                // Android 5.x及以下，通常不需要运行时权限
                arrayOf()
            }
        }
    }
    
    /**
     * 判断是否应该使用SAF（Storage Access Framework）
     * @return true表示使用SAF，false表示使用传统权限
     */
    private fun shouldUseSAF(): Boolean {
        // Android 10 (API 29) 及以上推荐使用SAF
        // Android 8.0-9.0 (API 26-28) 可以使用传统权限
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
    
    /**
     * 根据Android版本获取适当的应用列表权限
     * @return 权限数组
     */
    private fun getInstalledAppsPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上，使用QUERY_ALL_PACKAGES
            arrayOf(Manifest.permission.QUERY_ALL_PACKAGES)
        } else {
            // 旧版本Android通常不需要特殊权限
            arrayOf()
        }
    }
    
    /**
     * 检查系统Download目录访问权限
     * @return 是否具有Download目录访问权限
     */
    private fun checkDownloadDirectoryAccess(): Boolean {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null && downloadDir.exists()) {
                // 根据Android版本采用不同的检查方式
                if (shouldUseSAF()) {
                    // Android 10及以上，检查是否有SAF权限
                    checkSAFPermission()
                } else {
                    // Android 9及以下，检查传统文件访问权限
                    downloadDir.listFiles()?.isNotEmpty() ?: false
                }
            } else {
                false
            }
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查SAF权限
     * @return 是否具有SAF权限
     */
    private fun checkSAFPermission(): Boolean {
        return try {
            // 方法1：检查特定Download URI权限
            val downloadUri = DocumentsContract.buildTreeDocumentUri(
                "com.android.externalstorage.documents",
                "primary:Download"
            )
            
            val persistedPermissions = contentResolver.persistedUriPermissions
            Log.d("PermissionCheck", "持久化URI权限数量: ${persistedPermissions.size}")
            
            // 打印所有持久化权限（用于调试）
            persistedPermissions.forEachIndexed { index, uriPermission ->
                Log.d("PermissionCheck", "权限[$index]: ${uriPermission.uri} read:${uriPermission.isReadPermission} write:${uriPermission.isWritePermission}")
            }
            
            val hasSpecificPermission = persistedPermissions.any { uriPermission ->
                uriPermission.uri == downloadUri && 
                uriPermission.isReadPermission && 
                uriPermission.isWritePermission
            }
            
            // 方法2：尝试访问Download目录作为备用检查
            val canAccessDownload = try {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadDir?.exists() == true && downloadDir.listFiles()?.isNotEmpty() ?: false
            } catch (e: Exception) {
                false
            }
            
            val result = hasSpecificPermission || canAccessDownload
            Log.d("PermissionCheck", "SAF权限检查 - 特定URI: $hasSpecificPermission, 目录访问: $canAccessDownload, 最终结果: $result")
            result
        } catch (e: Exception) {
            Log.e("PermissionCheck", "SAF权限检查异常: ${e.message}")
            // 回退到传统文件访问检查
            try {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadDir?.exists() == true && downloadDir.listFiles()?.isNotEmpty() ?: false
            } catch (ex: Exception) {
                Log.e("PermissionCheck", "回退检查也失败: ${ex.message}")
                false
            }
        }
    }
    
    /**
     * 获取存储权限的用户友好描述
     * @return 权限描述文本
     */
    fun getStoragePermissionDescription(): String {
        return "存储权限：访问Download目录（支持传统权限或文件选择器两种方式）"
    }
    
    data class PermissionInfo(
        val name: String,
        val permissions: Array<String>
    )
    // Activity生命周期管理
    override fun onDestroy() {
        super.onDestroy()
        // 停止所有后台检查
        stopPeriodicPermissionCheck()
        cancelPermissionTimeout()
        Log.d("PermissionCheck", "PermissionApplication已销毁")
    }
    
    override fun onPause() {
        super.onPause()
        // 页面不可见时暂停周期性检查以节省性能
        periodicCheckHandler?.removeCallbacksAndMessages(null)
        Log.d("PermissionCheck", "页面暂停，暂停周期性检查")
    }
    
    override fun onResume() {
        super.onResume()
        // 页面恢复可见时重新启动检查（如果还需要的话）
        if (!isAllPermissionsGranted && periodicCheckHandler == null) {
            startPeriodicPermissionCheck()
            Log.d("PermissionCheck", "页面恢复，重新启动周期性检查")
        }
    }
}

@Composable
fun PermissionApplicationScreen(
    isAllPermissionsGranted: Boolean,
    onGetPermissionsClick: () -> Unit,
    onCheckPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current as Activity
    val activity = context as PermissionApplication
    
    var showDialog by remember { mutableStateOf(false) }
    var deniedPermissionName by remember { mutableStateOf("") }
    
    // 动态生成权限说明文本
    val permissionDescription = buildString {
        append("为了应用正常运行，需要您授予以下权限：\n\n")
        
        // 存储权限描述
        append("• ")
        append(activity.getStoragePermissionDescription())
        append("\n")
        
        // 其他权限
        append("• 应用列表权限：获取已安装应用信息\n")
        append("• 电话权限：拨打电话\n")
        append("• 短信权限：读取短信\n")
        append("• 悬浮窗权限：在其他应用上层显示内容")
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 标题文本
        Text(
            text = "权限申请",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // 说明文本
        Text(
            text = permissionDescription,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // 获取权限按钮
        Button(
            onClick = {
                if (isAllPermissionsGranted) {
                    activity.goToNextPage()
                } else {
                    onGetPermissionsClick()
                }
            },
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = if (isAllPermissionsGranted) "下一步" else "获取权限",
                fontSize = 16.sp
            )
        }
        
        // 添加重新检查权限按钮
        if (isAllPermissionsGranted) {
            Button(
                onClick = {
                    onCheckPermissions()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "重新检查权限",
                    fontSize = 14.sp
                )
            }
        }
    }
    
    // 权限被拒绝对话框
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("权限被拒绝") },
            text = { Text("$deniedPermissionName 被拒绝") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onGetPermissionsClick()
                    }
                ) {
                    Text("重试")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                        activity.exitApp()
                    }
                ) {
                    Text("退出应用")
                }
            }
        )
    }
    
    // 暴露对话框显示方法给Activity
    activity.showPermissionDeniedDialog = { permissionName ->
        deniedPermissionName = permissionName
        showDialog = true
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionApplicationPreview() {
    LaumaDesktopTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 标题文本
            Text(
                text = "权限申请",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // 说明文本
            Text(//TODO：替换为实际内容
                text = "为了应用正常运行，需要您授予以下权限：\n\n" +
                      "• 文件访问权限：通过系统文件选择器授权访问\n" +
                      "• 应用列表权限：获取已安装应用信息\n" +
                      "• 电话权限：拨打电话\n" +
                      "• 短信权限：读取短信\n" +
                      "• 悬浮窗权限：在其他应用上层显示内容",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // 获取权限按钮
            Button(
                onClick = { /* Preview中不执行实际操作 */ },
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = "获取权限",
                    fontSize = 16.sp
                )
            }
        }
    }
}