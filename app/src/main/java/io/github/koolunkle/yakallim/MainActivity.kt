package io.github.koolunkle.yakallim

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import io.github.koolunkle.yakallim.ui.ocr.OcrExtraSpec
import io.github.koolunkle.yakallim.ui.ocr.OcrScreen
import io.github.koolunkle.yakallim.ui.ocr.OcrViewModel
import io.github.koolunkle.yakallim.ui.theme.YakAllimTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: OcrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.onNotificationOpened(OcrExtraSpec.readJobId(intent))
        setContent {
            YakAllimTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OcrScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        super.onNewIntent(intent)
        viewModel.onNotificationOpened(OcrExtraSpec.readJobId(intent))
    }
}
