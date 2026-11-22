package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.story.EndingType
import com.example.ui.screens.EndingScreen
import com.example.ui.screens.GameHudScreen
import com.example.ui.screens.TitleScreen
import com.example.ui.theme.House12Theme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        setContent {
            House12Theme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    when (uiState.screen) {
                        AppScreen.TITLE -> {
                            TitleScreen(
                                settings = uiState.settings,
                                onStartGame = viewModel::startNewGame,
                                onUpdateSensitivity = viewModel::updateSensitivity,
                                onUpdatePsxJitter = viewModel::updatePsxJitter,
                                onUpdateCrtScanlines = viewModel::updateCrtScanlines,
                                onUpdateVolume = viewModel::updateVolume
                            )
                        }

                        AppScreen.PLAYING -> {
                            GameHudScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }

                        AppScreen.ENDING -> {
                            BackHandler { viewModel.returnToTitle() }
                            EndingScreen(
                                ending = uiState.currentEnding ?: EndingType.VIGILANT_SURVIVAL,
                                unlockedCount = uiState.settings.unlockedEndings.size,
                                crtEnabled = uiState.settings.crtScanlinesEnabled,
                                onReplay = viewModel::startNewGame,
                                onBackToTitle = viewModel::returnToTitle
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startAudio()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopAudio()
    }
}
