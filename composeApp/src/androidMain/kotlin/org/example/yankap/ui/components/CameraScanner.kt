package org.example.yankap.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.shape.CircleShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScanner(
    onNumberScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var isManualScanTriggered by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees
                                        )
                                        textRecognizer.process(image)
                                            .addOnSuccessListener { visionText ->
                                                for (block in visionText.textBlocks) {
                                                    val blockText = block.text
                                                    
                                                    // Cameroon specific: 9 digits starting with 6
                                                    // Matches 6 then 8 more digits, allowing for spaces/dashes
                                                    val cameroonPattern = "6([\\s-]?\\d){8}".toRegex()
                                                    val match = cameroonPattern.find(blockText)
                                                    
                                                    if (match != null || isManualScanTriggered) {
                                                        val rawText = match?.value ?: blockText
                                                        val cleanNumber = rawText.replace("[^\\d]".toRegex(), "")
                                                        
                                                        // Validate it's a likely Cameroon mobile number (starts with 6, length 9)
                                                        if (cleanNumber.length == 9 && cleanNumber.startsWith("6")) {
                                                            Log.d("CameraScanner", "Found Cameroon number: $cleanNumber")
                                                            
                                                            // Vibrate
                                                            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                                            } else {
                                                                vibrator.vibrate(100)
                                                            }
                                                            
                                                            onNumberScanned(cleanNumber)
                                                            isManualScanTriggered = false
                                                            break
                                                        } else if (isManualScanTriggered && cleanNumber.length >= 6) {
                                                            // Fallback for manual scan if it's not exactly 9 digits but looks like a number
                                                            onNumberScanned(cleanNumber)
                                                            isManualScanTriggered = false
                                                            break
                                                        }
                                                    }
                                                }
                                                if (isManualScanTriggered) {
                                                    isManualScanTriggered = false // Reset if nothing found
                                                }
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        imageProxy.close()
                                    }
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            Log.e("CameraScanner", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay UI
            Box(modifier = Modifier.fillMaxSize()) {
                // Focus Frame
                // Simple box in the middle to guide the user
                Surface(
                    modifier = Modifier
                        .size(280.dp, 100.dp)
                        .align(Alignment.Center)
                        .padding(2.dp),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {}

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Position the number inside the box",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f)).padding(8.dp)
                    )

                    // Shutter/Capture Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            onClick = { 
                                isManualScanTriggered = true
                                Log.d("CameraScanner", "Manual scan triggered")
                            },
                            modifier = Modifier.size(70.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.8f),
                            border = androidx.compose.foundation.BorderStroke(4.dp, Color.White),
                            content = {}
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("TAP TO SCAN", color = Color.White, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .align(Alignment.End)
                            .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission is required", color = Color.White)
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            textRecognizer.close()
        }
    }
}
