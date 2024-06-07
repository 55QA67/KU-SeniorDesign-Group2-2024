package com.ce491.safe_ride

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.RingtoneManager
import android.os.Bundle
import android.view.OrientationEventListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ce491.safe_ride.ui.theme.MyApplicationTheme
import kotlin.math.abs


private const val debug = false

private val showDialog = mutableStateOf(false)
val inWay = mutableStateOf(true)
val passCount = mutableIntStateOf(0)
private val rot = mutableStateOf(true)
private val showCamera = mutableStateOf(true)
private val linearAcceleration = mutableStateOf(floatArrayOf(0F, 0F, 0F))
private val accuracyMain = mutableIntStateOf(0)
private const val floatError = 5F


class MainActivity : ComponentActivity(), SensorEventListener {

    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        val mOrientationListener: OrientationEventListener = object : OrientationEventListener(
            applicationContext
        ) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == 0 || orientation == 180) {
                    rot.value = true
                } else if (orientation == 90 || orientation == 270) {
                    rot.value = false
                }
            }
        }

        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable()
        }

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> { }
            else -> {
                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
            }
        }

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                        color = (if (inWay.value) { Color.Green } else { Color.Red })
                ) {
                    if (inWay.value) {
                        try {
                            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            val r = RingtoneManager.getRingtone(applicationContext, notification)
                            r.play()
                        } catch (e: Exception) {
                            print(e.stackTrace)
                        }
                    }

                    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)

                    if (debug) {
                        Column {
                            Text(text = "Linear Acceleration: ${linearAcceleration.value[0]}, " +
                                    "${linearAcceleration.value[1]}, " +
                                    "${linearAcceleration.value[2]}")
                            Text(text = "Accuracy: ${accuracyMain.intValue}")
                        }
                    }

                    if (rot.value) {
                        PortraitMode()
                    }
                    else {
                        LandscapeMode()
                    }
                }
            }
        }
    }

    // TODO: This might work, but the camera might freak out
    // TODO: maybe change the floatError
    override fun onSensorChanged(event: SensorEvent) {

        linearAcceleration.value = event.values

        showCamera.value = abs(event.values[0]) < floatError &&
                abs(event.values[1]) < floatError &&
                abs(event.values[2]) < floatError
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        accuracyMain.intValue = accuracy
    }
}

@Composable
fun PortraitMode() {
    // A column that helps with positioning things on the screen
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        PassengerCount()

        // This is the show the camera in the app
        Box(contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .padding(top = 95.dp)
                .fillMaxSize()) {
            if (showCamera.value) {
                CameraPreviewScreen(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun LandscapeMode() {
    Row(verticalAlignment = Alignment.CenterVertically) {

        PassengerCount()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // This is the show the camera in the app
            Box(contentAlignment = Alignment.BottomCenter) {
                if (showCamera.value) {
                    CameraPreviewScreen(Modifier.fillMaxSize())
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PassengerCount() {
    // A column that helps with positioning things on the screen
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // This is to test the background color change
//        FilledButton("Check") { inWay.value = !inWay.value }

        // This is to test incrementing the passenger counter
//        FilledButton("Increment") { passCount.intValue++ }

        // This is the passenger counter
        PassengerCountText(modifier = Modifier.combinedClickable(
            onClick = { passCount.intValue++ },
            onDoubleClick = { passCount.intValue-- },
            onLongClick = { showDialog.value = true }
        ))

        // This is to test decrementing the passenger counter
//        FilledButton("Decrement") { passCount.intValue-- }

        // This is to reset the passenger counter
//        FilledButton("Reset") { showDialog.value = true }
        if (showDialog.value) {
            ResetAlertDialog(
                onDismissRequest = { showDialog.value = false },
                onConfirmation = {
                    showDialog.value = false
                    passCount.intValue = 0
                },
                dialogTitle = "🛑⚠️--WARNING--⚠️🛑",
                dialogText = "Are you sure you want to reset the passenger counter?",
                checkBoxText = "Reset Count?",
                icon = Icons.Default.Warning
            )
        }
    }
}

// This is way shows the passenger count
@Composable
fun PassengerCountText(modifier: Modifier = Modifier) {
    if (passCount.intValue < 0) {
        passCount.intValue = 0
    }
    Text(
        text = "${passCount.intValue}",
        modifier = modifier,
        fontSize = 120.sp,
        color = Color.Black,
        textAlign = TextAlign.Center,
        lineHeight = 120.sp
    )
}

// This is an alert dialog
// Used for:
//  - Reset Warning
@Composable
fun ResetAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    checkBoxText: String,
    icon: ImageVector,
) {
    val showConfirm = remember { mutableStateOf(false) }
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Warning Icon")
        },
        title = {
            Text(
                text = dialogTitle,
                fontSize = 24.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    text = dialogText,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Row {
                    Checkbox(
                        checked = showConfirm.value,
                        onCheckedChange = { showConfirm.value = !showConfirm.value }
                    )
                    Text(
                        text = checkBoxText,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                }
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                enabled = showConfirm.value,
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}

// This is what shows the buttons
// Used for:
//  - Check
//  - Increment
//  - Decrement
//  - Reset
@Composable
fun FilledButton(text: String, onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text(text)
    }
}