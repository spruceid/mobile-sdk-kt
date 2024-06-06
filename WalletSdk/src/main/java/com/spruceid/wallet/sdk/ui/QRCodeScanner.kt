package com.spruceid.wallet.sdk.ui

import android.content.res.Resources
import android.util.Range
import android.view.Surface
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat


@Composable
fun QRCodeScanner(
    title: String = "Scan QR Code",
    subtitle: String = "Please align within the guides",
    cancelButtonLabel: String = "Cancel",
    onRead: (content: String) -> Unit,
    isMatch: (content: String) -> Boolean = {_ -> true},
    onCancel: () -> Unit,
    fontFamily: FontFamily = FontFamily.Default,
    guidesColor: Color = Color.White,
    readerColor: Color = Color.White,
    textColor: Color = Color.White,
    backgroundOpacity: Float = 0.5f,
) {
    var code by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val cameraProviderFuture =
        remember {
            ProcessCameraProvider.getInstance(context)
        }
    val lifecycleOwner = LocalLifecycleOwner.current

    var canvasSize by remember {
        mutableStateOf(Size(0f, 0f))
    }
    val infiniteTransition = rememberInfiniteTransition("Infinite QR code line transition remember")
    val offsetTop by infiniteTransition.animateFloat(
        initialValue = canvasSize.height * .35f,
        targetValue = canvasSize.height * .35f + canvasSize.width * .6f,
        animationSpec =
        infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        "QR code line animation",
    )


    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
            Box(
                Modifier.fillMaxSize(),
            ) {
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context)
                        val preview =
                            Preview.Builder()
                                .setTargetFrameRate(Range(20, 45))
                                .setTargetRotation(Surface.ROTATION_0)
                                .build()
                        val selector =
                            CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        val imageAnalysis =
                            ImageAnalysis.Builder()
                                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                        imageAnalysis.setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            QrCodeAnalyzer(
                                isMatch = isMatch,
                                onQrCodeScanned = { result ->
                                    onRead(result)
                                    code = result
                                }),
                        )
                        var cameraControl: CameraControl? = null
                        try {
                            cameraControl = cameraProviderFuture
                                .get()
                                .bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    imageAnalysis,
                                ).cameraControl
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            cameraControl?.setZoomRatio(2f)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        previewView
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = backgroundOpacity))
                        .drawWithContent {
                            canvasSize = size
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val width = canvasWidth * .6f

                            val left = (canvasWidth - width) / 2
                            val top = canvasHeight * .35f
                            val right = left + width
                            val bottom = top + width
                            val cornerLength = 40f
                            val cornerRadius = 40f
                            drawContent()
                            drawRect(Color(0x99000000))
                            drawRoundRect(
                                topLeft = Offset(left, top),
                                size = Size(width, width),
                                color = Color.Transparent,
                                blendMode = BlendMode.SrcIn,
                                cornerRadius = CornerRadius(cornerRadius - 10f),
                            )
                            drawRect(
                                topLeft = Offset(left, offsetTop),
                                size = Size(width, 2f),
                                color = readerColor,
                                style = Stroke(2.dp.toPx()),
                            )
                            val path = Path()

                            // top left
                            path.moveTo(left, (top + cornerRadius))
                            path.arcTo(
                                Rect(
                                    left = left,
                                    top = top,
                                    right = left + cornerRadius,
                                    bottom = top + cornerRadius,
                                ),
                                180f,
                                90f,
                                true,
                            )
                            path.moveTo(left + (cornerRadius / 2f), top)
                            path.lineTo(left + (cornerRadius / 2f) + cornerLength, top)
                            path.moveTo(left, top + (cornerRadius / 2f))
                            path.lineTo(left, top + (cornerRadius / 2f) + cornerLength)

                            // top right
                            path.moveTo(right - cornerRadius, top)
                            path.arcTo(
                                Rect(
                                    left = right - cornerRadius,
                                    top = top,
                                    right = right,
                                    bottom = top + cornerRadius,
                                ),
                                270f,
                                90f,
                                true,
                            )
                            path.moveTo(right - (cornerRadius / 2f), top)
                            path.lineTo(right - (cornerRadius / 2f) - cornerLength, top)
                            path.moveTo(right, top + (cornerRadius / 2f))
                            path.lineTo(right, top + (cornerRadius / 2f) + cornerLength)

                            // bottom left
                            path.moveTo(left, bottom - cornerRadius)
                            path.arcTo(
                                Rect(
                                    left = left,
                                    top = bottom - cornerRadius,
                                    right = left + cornerRadius,
                                    bottom = bottom,
                                ),
                                90f,
                                90f,
                                true,
                            )
                            path.moveTo(left + (cornerRadius / 2f), bottom)
                            path.lineTo(left + (cornerRadius / 2f) + cornerLength, bottom)
                            path.moveTo(left, bottom - (cornerRadius / 2f))
                            path.lineTo(left, bottom - (cornerRadius / 2f) - cornerLength)

                            // bottom right
                            path.moveTo(left, bottom - cornerRadius)
                            path.arcTo(
                                Rect(
                                    left = right - cornerRadius,
                                    top = bottom - cornerRadius,
                                    right = right,
                                    bottom = bottom,
                                ),
                                0f,
                                90f,
                                true,
                            )
                            path.moveTo(right - (cornerRadius / 2f), bottom)
                            path.lineTo(right - (cornerRadius / 2f) - cornerLength, bottom)
                            path.moveTo(right, bottom - (cornerRadius / 2f))
                            path.lineTo(right, bottom - (cornerRadius / 2f) - cornerLength)

                            drawPath(
                                path,
                                color = guidesColor,
                                style = Stroke(width = 15f),
                            )
                        },
                )
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp)
                            .padding(horizontal = 30.dp),
                    ) {
                        Text(
                            text = title,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = textColor,
                        )
                        Text(
                            text = subtitle,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor,
                        )
                    }

                    Column(
                        Modifier.fillMaxWidth(),
                    ) {
                        Button(
                            onClick = onCancel,
                            modifier =
                            Modifier
                                .padding(bottom = 50.dp)
                                .padding(horizontal = 30.dp),
                            colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Transparent,
                            ),
                        ) {
                            Text(
                                text = cancelButtonLabel,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = textColor,
                            )
                        }
                    }
                }
            }
    }
}
