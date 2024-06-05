package com.ce491.safe_ride

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.ce491.safe_ride.ml.DetectWithMetadata
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import java.util.concurrent.Executors

@Composable
fun CameraPreviewScreen(modifier: Modifier) {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val boundingBoxOverlay = remember { OverlayView(context) }
    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    val currentImage = remember { mutableStateOf<Bitmap?>(null) } // Add this line


    LaunchedEffect(lensFacing) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        Executors.newSingleThreadExecutor()
                    ) { imageProxy ->
                        imageProxy.use { proxy ->
                            processImageProxy(proxy, context, boundingBoxOverlay, previewView, currentImage)
                        }
                    }
                }

            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalyzer
            )
        }, ContextCompat.getMainExecutor(context))

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(modifier = modifier) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
//        currentImage.value?.let { bitmap ->
//            Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Current Image", modifier = Modifier.fillMaxSize())
//        }
        AndroidView({ boundingBoxOverlay }, modifier = Modifier.fillMaxSize())
    }
}

private fun processImageProxy(
    imageProxy: ImageProxy,
    context: Context,
    overlayView: OverlayView,
    previewView: PreviewView,
    currentImage: MutableState<Bitmap?> // Add this parameter
) {
    val bitmap = imageProxy.toBitmap()

    // Calculate the PreviewView aspect ratio
    val previewAspectRatio = previewView.width.toFloat() / previewView.height

    // Create a new matrix for the rotation transformation
    val matrix = Matrix().apply {
        postRotate(90f) // Rotate 90 degrees
    }

    // Apply the rotation transformation to the bitmap
    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    // Scale the bitmap to match the PreviewView aspect ratio
    val scaledBitmap = Bitmap.createScaledBitmap(
        rotatedBitmap,
        (rotatedBitmap.height * previewAspectRatio).toInt(),
        rotatedBitmap.height,
        true
    )

    currentImage.value = scaledBitmap // Add this line

    // Run the model inference on a background thread
    Executors.newSingleThreadExecutor().execute {
        val options = Model.Options.Builder()
//            .setDevice(Model.Device.GPU)
            .setNumThreads(4)
            .build()

        val model = DetectWithMetadata.newInstance(context, options)

        // Creates inputs for reference.
        val image = TensorImage.fromBitmap(scaledBitmap)

        // Runs model inference and gets result.
        val outputs = model.process(image)
        val detectionResult = outputs.detectionResultList

        // Update the overlay view with the detection results
        overlayView.post {
            overlayView.updateResults(detectionResult, scaledBitmap.width, scaledBitmap.height)
        }

        // Releases model resources if no longer used.
        model.close()
    }
}