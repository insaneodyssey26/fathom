package com.masum.fathom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.masum.fathom.ui.theme.FathomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            FathomTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = FathomTheme.colors.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(FathomTheme.colors.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Fathom",
                            style = FathomTheme.typography.commentaryTitle,
                            color = FathomTheme.colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    FathomTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FathomTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Fathom",
                style = FathomTheme.typography.commentaryTitle,
                color = FathomTheme.colors.textPrimary
            )
        }
    }
}