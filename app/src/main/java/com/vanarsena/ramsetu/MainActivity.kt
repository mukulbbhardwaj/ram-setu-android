package com.vanarsena.ramsetu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vanarsena.ramsetu.ui.ProvideAppLanguage
import com.vanarsena.ramsetu.audio.AudioEngine
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.data.PreferencesManager
import com.vanarsena.ramsetu.engine.GameEngine
import com.vanarsena.ramsetu.engine.GameStatus
import com.vanarsena.ramsetu.ui.screens.GameScreen
import com.vanarsena.ramsetu.ui.screens.MainMenuScreen
import com.vanarsena.ramsetu.ui.theme.OceanDeep
import com.vanarsena.ramsetu.ui.theme.RamSetuTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels { GameViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProvideAppLanguage(viewModel.language) {
                RamSetuTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = OceanDeep
                    ) {
                        RamSetuApp(
                            gameEngine = viewModel.gameEngine,
                            preferencesManager = viewModel.preferencesManager,
                            hapticManager = viewModel.hapticManager,
                            audioEngine = viewModel.audioEngine,
                            language = viewModel.language,
                            onLanguageChange = viewModel::setAppLanguage
                        )
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.gameEngine.pauseGame()
        viewModel.audioEngine.pauseMusic()
        viewModel.gameEngine.persistInterruptedRun()
    }

}

@Composable
fun RamSetuApp(
    gameEngine: GameEngine,
    preferencesManager: PreferencesManager,
    hapticManager: HapticManager,
    audioEngine: AudioEngine,
    language: String,
    onLanguageChange: (String) -> Unit
) {
    if (gameEngine.status == GameStatus.MENU) {
        MainMenuScreen(
            preferencesManager = preferencesManager,
            hapticManager = hapticManager,
            audioEngine = audioEngine,
            language = language,
            onLanguageChange = onLanguageChange,
            onStartGame = { gameEngine.startGame() }
        )
    } else {
        GameScreen(
            gameEngine = gameEngine,
            preferencesManager = preferencesManager,
            hapticManager = hapticManager
        )
    }
}
