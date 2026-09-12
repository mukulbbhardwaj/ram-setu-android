package com.vanarsena.ramsetu

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vanarsena.ramsetu.audio.AudioEngine
import com.vanarsena.ramsetu.audio.HapticManager
import com.vanarsena.ramsetu.data.PreferencesManager
import com.vanarsena.ramsetu.engine.GameEngine

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesManager = PreferencesManager(application)

    val hapticManager = HapticManager(application).apply {
        intensity = preferencesManager.hapticIntensity
    }

    val audioEngine = AudioEngine(application).apply {
        isSoundEnabled = preferencesManager.isSoundEnabled
        isMusicEnabled = preferencesManager.isMusicEnabled
    }

    val gameEngine = GameEngine(hapticManager, audioEngine, preferencesManager)

    var language by mutableStateOf(preferencesManager.language)
        private set

    init {
        audioEngine.onFocusLost = { gameEngine.pauseGame() }
        gameEngine.restoreInterruptedRunIfNeeded()
    }

    fun setAppLanguage(tag: String) {
        preferencesManager.language = tag
        language = tag
    }

    override fun onCleared() {
        audioEngine.onFocusLost = null
        audioEngine.release()
        super.onCleared()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as Application
                GameViewModel(app)
            }
        }
    }
}
