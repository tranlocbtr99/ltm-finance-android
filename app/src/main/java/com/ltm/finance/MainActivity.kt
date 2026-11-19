package com.ltm.finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.ltm.finance.ui.MainScreen
import com.ltm.finance.ui.MainViewModel
import com.ltm.finance.ui.theme.LTMFinanceTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge for immersive iOS liquid glass style
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LTMFinanceTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
