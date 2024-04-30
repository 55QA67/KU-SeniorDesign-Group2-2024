package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
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
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(), color = (if (inWay.value) { Color.Green } else { Color.Red })) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FilledButtonExample("Check") { inWay.value = !inWay.value }
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

@Composable
fun FilledButtonExample(text: String, onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text(text)
    }
}