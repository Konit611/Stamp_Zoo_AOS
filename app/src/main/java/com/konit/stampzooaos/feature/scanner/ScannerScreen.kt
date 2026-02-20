package com.konit.stampzooaos.feature.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.konit.stampzooaos.BuildConfig
import com.konit.stampzooaos.R
import com.konit.stampzooaos.ui.theme.ZooNavyBlue
import com.konit.stampzooaos.ui.theme.ZooWhite
import androidx.compose.ui.res.stringResource
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(onResult: (String) -> Unit, onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )
    
    var lastResultAt by remember { mutableStateOf(0L) }
    var isScanning by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    
    // 카메라 권한 확인
    LaunchedEffect(Unit) {
        if (cameraPermissionState.allPermissionsGranted) {
            isScanning = true
            if (BuildConfig.DEBUG) Log.d("ScannerScreen", "Camera permission granted, starting scan")
        } else {
            if (BuildConfig.DEBUG) Log.d("ScannerScreen", "Requesting camera permission")
            cameraPermissionState.launchMultiplePermissionRequest()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.qr_background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 메인 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            // 뒤로가기 버튼
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 카메라 프리뷰 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally)
            ) {
                when {
                    !cameraPermissionState.allPermissionsGranted -> {
                        // 권한 요청 필요
                        PermissionRequestView(
                            modifier = Modifier.fillMaxSize(),
                            onRequestPermission = { 
                                cameraPermissionState.launchMultiplePermissionRequest()
                            }
                        )
                    }
                    cameraError != null -> {
                        // 에러 상태
                        ErrorView(
                            message = cameraError ?: "Unknown error",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    isScanning -> {
                        // 카메라 프리뷰
                        CameraPreviewView(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp)),
                            lifecycleOwner = lifecycleOwner,
                            onScanResult = { result ->
                                val now = System.currentTimeMillis()
                                if (now - lastResultAt > 1500L) {
                                    lastResultAt = now
                                    // 햅틱 피드백 (에러 발생 시 무시)
                                    try {
                                        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            context.getSystemService(Vibrator::class.java)
                                        } else {
                                            @Suppress("DEPRECATION")
                                            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            vib.vibrate(
                                                VibrationEffect.createOneShot(
                                                    50,
                                                    VibrationEffect.DEFAULT_AMPLITUDE
                                                )
                                            )
                                        } else {
                                            @Suppress("DEPRECATION")
                                            vib.vibrate(50)
                                        }
                                    } catch (e: Exception) {
                                        // 진동 권한이 없어도 계속 진행
                                        if (BuildConfig.DEBUG) Log.w("ScannerScreen", "Vibration failed: ${e.message}")
                                    }
                                    onResult(result)
                                }
                            },
                            onError = { error ->
                                cameraError = error
                                Log.e("ScannerScreen", "Camera error: $error")
                            }
                        )
                    }
                    else -> {
                        // 스캔 중지 시 검은색 박스
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black)
                        )
                    }
                }
                
                // 테두리
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            color = if (isScanning) Color.Green else Color.White,
                            shape = RoundedCornerShape(20.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 스캔 버튼
            Button(
                onClick = { 
                    if (cameraPermissionState.allPermissionsGranted && cameraError == null) {
                        isScanning = !isScanning
                    }
                },
                enabled = cameraPermissionState.allPermissionsGranted && cameraError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) Color.Red else ZooNavyBlue,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = if (isScanning) stringResource(id = R.string.scanner_scan_stop) else stringResource(id = R.string.scanner_scan_start),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CameraPreviewView(
    modifier: Modifier = Modifier,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onScanResult: (String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val scanner = BarcodeScanning.getClient()
    val executor = remember { Executors.newSingleThreadExecutor() }
    
    // 카메라 정리
    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
            executor.shutdown()
        }
    }
    
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    val resolutionSelector = ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(
                                android.util.Size(1280, 720),
                                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                            )
                        )
                        .build()
                    
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    
                    imageAnalyzer.setAnalyzer(executor) { imageProxy: ImageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val raw = barcodes.firstOrNull()?.rawValue
                                    if (!raw.isNullOrBlank()) {
                                        onScanResult(raw)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    if (BuildConfig.DEBUG) Log.e("ScannerScreen", "Barcode scanning error", e)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                    
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                    if (BuildConfig.DEBUG) Log.d("ScannerScreen", "Camera bound successfully")
                } catch (e: Exception) {
                    onError("Camera init error: ${e.message}")
                    if (BuildConfig.DEBUG) Log.e("ScannerScreen", "Camera initialization error", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        }
    )
}

@Composable
private fun PermissionRequestView(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.scanner_permission_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermission) {
                Text(stringResource(id = R.string.scanner_permission_button))
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.scanner_error_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}
