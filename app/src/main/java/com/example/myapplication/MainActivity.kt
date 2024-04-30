package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

private val showDialog = mutableStateOf(false)
private val passCount = mutableIntStateOf(0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {

                    Column {
                        if (showDialog.value) {
                            FilledButtonExample("Hide Message") { showDialog.value = !showDialog.value }
                        } else {
                            FilledButtonExample("Show Message") { showDialog.value = !showDialog.value }
                        }
                        Greeting(name = "")
                        FilledButtonExample("Increment") { passCount.intValue++ }
                        PassengerCount()
                        FilledButtonExample("Decrement") { passCount.intValue-- }
                        FilledButtonExample("Reset") { passCount.intValue = 0 }
                    }

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    if (showDialog.value) {
        Text(
            text = "😐 Hello $name!",
            modifier = modifier,
            fontSize = 120.sp,
            color = Color.Red,
            textAlign = TextAlign.Center,
            lineHeight = 120.sp
        )
    }
}

@Composable
fun PassengerCount(modifier: Modifier = Modifier) {
    if (passCount.intValue < 0) {
        passCount.intValue = 0
    }
    Text(
        text = "${passCount.intValue}",
        modifier = modifier,
        fontSize = 120.sp,
        color = Color.Red,
        textAlign = TextAlign.Center,
        lineHeight = 120.sp
    )
}

@Composable
fun FilledButtonExample(text: String, onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("")
    }
}