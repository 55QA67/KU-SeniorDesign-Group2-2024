package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.example.myapplication.ui.theme.MyApplicationTheme

private val showDialog = mutableStateOf(false)
private val inWay = mutableStateOf(true)
private val passCount = mutableIntStateOf(0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(), color = (if (inWay.value) { Color.Green } else { Color.Red })) {

                    // A column that helps with positioning things on the screen
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // This is to test the background color change
                        FilledButtonExample("Check") { inWay.value = !inWay.value }

                        // This is to test incrementing the passenger counter
                        FilledButtonExample("Increment") { passCount.intValue++ }

                        // This is the passenger counter
                        PassengerCount()

                        // This is to test decrementing the passenger counter
                        FilledButtonExample("Decrement") { passCount.intValue-- }

                        // This is to reset the passenger counter
                        FilledButtonExample("Reset") { showDialog.value = true }
                        if (showDialog.value) {
                            AlertDialogExample(
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
            }
        }
    }
}

// This is way shows the passenger count
@Composable
fun PassengerCount(modifier: Modifier = Modifier) {
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
fun AlertDialogExample(
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
fun FilledButtonExample(text: String, onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text(text)
    }
}