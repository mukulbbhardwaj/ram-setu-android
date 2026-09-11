package com.vanarsena.ramsetu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var hapticManager: HapticManager
    private lateinit var audioEngine: AudioEngine
    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(this)
        hapticManager = HapticManager(this).apply {
            intensity = preferencesManager.hapticIntensity
        }
        audioEngine = AudioEngine(this).apply {
            isSoundEnabled = preferencesManager.isSoundEnabled
            isMusicEnabled = preferencesManager.isMusicEnabled
        }
        gameEngine = GameEngine(hapticManager, audioEngine, preferencesManager)

        setContent {
            RamSetuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = OceanDeep
                ) {
                    RamSetuApp(
                        gameEngine = gameEngine,
                        preferencesManager = preferencesManager,
                        hapticManager = hapticManager
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gameEngine.pauseGame()
        audioEngine.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
    }
}

@Composable
fun RamSetuApp(
    gameEngine: GameEngine,
    preferencesManager: PreferencesManager,
    hapticManager: HapticManager
) {
    if (gameEngine.status == GameStatus.MENU) {
        MainMenuScreen(
            preferencesManager = preferencesManager,
            hapticManager = hapticManager,
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
