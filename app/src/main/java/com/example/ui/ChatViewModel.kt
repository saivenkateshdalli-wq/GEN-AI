package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.ChatMessage
import com.example.data.ChatSession
import com.example.data.DatabaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import android.speech.tts.TextToSpeech
import java.util.Locale

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val chatDao = database.chatDao()

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isAiVoiceModeEnabled = MutableStateFlow(false)
    val isAiVoiceModeEnabled: StateFlow<Boolean> = _isAiVoiceModeEnabled.asStateFlow()

    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "IN")
                isTtsInitialized = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    fun toggleAiVoiceMode() {
        _isAiVoiceModeEnabled.value = !_isAiVoiceModeEnabled.value
        if (!_isAiVoiceModeEnabled.value) {
            tts?.stop()
        }
    }

    fun speakText(text: String, force: Boolean = false) {
        if (isTtsInitialized && (_isAiVoiceModeEnabled.value || force)) {
            // Remove markdown symbols and emojis for cleaner speech
            val cleanText = text.replace(Regex("[*#~_`]|🎓|🔥|✅|❌|🔄|📝|💡|📋|❓"), "")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
        }
    }

    fun stopTts() {
        tts?.stop()
    }

    // All available sessions for history
    val sessionsFlow: StateFlow<List<ChatSession>> = chatDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active session ID
    private val _activeSessionId = MutableStateFlow<Int?>(null)
    val activeSessionId: StateFlow<Int?> = _activeSessionId.asStateFlow()

    // Active session details
    private val _activeSession = MutableStateFlow<ChatSession?>(null)
    val activeSession: StateFlow<ChatSession?> = _activeSession.asStateFlow()

    // Messages of the active session
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // UI Input field
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Loading indicator for VENKY's response
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Onboarding setup state
    private val _showOnboarding = MutableStateFlow(true)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    init {
        // Collect messages whenever active session id changes
        viewModelScope.launch {
            var messagesJob: kotlinx.coroutines.Job? = null
            _activeSessionId.collect { id ->
                messagesJob?.cancel()
                if (id != null) {
                    val session = chatDao.getSessionById(id)
                    _activeSession.value = session
                    _showOnboarding.value = false
                    messagesJob = launch {
                        chatDao.getMessagesForSession(id).collect { msgs ->
                            _messages.value = msgs
                        }
                    }
                } else {
                    _activeSession.value = null
                    _messages.value = emptyList()
                    _showOnboarding.value = true
                }
            }
        }
    }

    fun selectSession(id: Int) {
        _activeSessionId.value = id
    }

    fun deleteSession(session: ChatSession) {
        viewModelScope.launch {
            chatDao.deleteSession(session)
            if (_activeSessionId.value == session.id) {
                _activeSessionId.value = null
            }
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun toggleMode() {
        val current = _activeSession.value ?: return
        val newMode = if (current.activeMode == "EDUCATION") "INTERACTIVE" else "EDUCATION"
        viewModelScope.launch {
            val updated = current.copy(activeMode = newMode, lastUpdated = System.currentTimeMillis())
            chatDao.updateSession(updated)
            _activeSession.value = updated

            // Insert standard Venky guidance notice in chat
            val noticeContent = if (newMode == "INTERACTIVE") {
                "🔄 *Now chatting in Interactive Mode! Let's handle college stress, talk study goals, or just grab visual tips!*"
            } else {
                "🔄 *Now tutoring in Education Socratic Mode! Let's tackle concepts step-by-step with real-world Indian examples!*"
            }
            chatDao.insertMessage(ChatMessage(sessionId = current.id, sender = "SYSTEM", content = noticeContent))
        }
    }

    fun addNewTopicTag(isUnderstood: Boolean, tag: String) {
        if (tag.isBlank()) return
        val session = _activeSession.value ?: return
        viewModelScope.launch {
            val list = if (isUnderstood) {
                session.understoodTopics.split(";").filter { it.isNotBlank() }.toMutableList()
            } else {
                session.confusedTopics.split(";").filter { it.isNotBlank() }.toMutableList()
            }
            if (!list.contains(tag)) {
                list.add(tag)
                if (isUnderstood) {
                    _showCelebration.value = true
                    launch {
                        kotlinx.coroutines.delay(3000)
                        _showCelebration.value = false
                    }
                }
            }
            val contentStr = list.joinToString(";")
            val updated = if (isUnderstood) {
                session.copy(understoodTopics = contentStr, lastUpdated = System.currentTimeMillis())
            } else {
                session.copy(confusedTopics = contentStr, lastUpdated = System.currentTimeMillis())
            }
            chatDao.updateSession(updated)
            _activeSession.value = updated
        }
    }

    fun removeTopicTag(isUnderstood: Boolean, tag: String) {
        val session = _activeSession.value ?: return
        viewModelScope.launch {
            val list = if (isUnderstood) {
                session.understoodTopics.split(";").filter { it.isNotBlank() }.toMutableList()
            } else {
                session.confusedTopics.split(";").filter { it.isNotBlank() }.toMutableList()
            }
            list.remove(tag)
            val contentStr = list.joinToString(";")
            val updated = if (isUnderstood) {
                session.copy(understoodTopics = contentStr, lastUpdated = System.currentTimeMillis())
            } else {
                session.copy(confusedTopics = contentStr, lastUpdated = System.currentTimeMillis())
            }
            chatDao.updateSession(updated)
            _activeSession.value = updated
        }
    }

    fun createNewSession(
        name: String,
        gender: String,
        subject: String,
        topic: String,
        understanding: String
    ) {
        viewModelScope.launch {
            val newSession = ChatSession(
                studentName = name,
                gender = gender,
                subject = subject,
                topic = topic,
                initialUnderstanding = understanding,
                activeMode = "EDUCATION",
                lastUpdated = System.currentTimeMillis()
            )
            val id = chatDao.insertSession(newSession)
            _activeSessionId.value = id.toInt()

            // Trigger Venky's starting introduction message!
            _isLoading.value = true

            val tempMessageList = listOf(
                ChatMessage(
                    sessionId = id.toInt(),
                    sender = "USER",
                    content = "Hey VENKY! Introduce yourself based on my new profile and ask me your first guided Socratic question about $topic."
                )
            )
            val venkyIntro = GeminiClient.generateResponse(tempMessageList, newSession.copy(id = id.toInt()))

            chatDao.insertMessage(ChatMessage(
                sessionId = id.toInt(),
                sender = "VENKY",
                content = venkyIntro
            ))
            speakText(venkyIntro)
            _isLoading.value = false
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        val sessionId = _activeSessionId.value ?: return
        val session = _activeSession.value ?: return

        _inputText.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            val userMsg = ChatMessage(sessionId = sessionId, sender = "USER", content = text)
            chatDao.insertMessage(userMsg)

            // Update the session's timestamp
            val updatedSession = session.copy(lastUpdated = System.currentTimeMillis())
            chatDao.updateSession(updatedSession)

            // Collect all current messages from DB
            val allMsgs = _messages.value + userMsg

            val response = GeminiClient.generateResponse(allMsgs, updatedSession)

            chatDao.insertMessage(ChatMessage(sessionId = sessionId, sender = "VENKY", content = response))
            speakText(response)
            _isLoading.value = false
        }
    }

    // Quick Command Trigger
    fun executeQuickCommand(command: String) {
        val sessionId = _activeSessionId.value ?: return
        val session = _activeSession.value ?: return

        _isLoading.value = true
        viewModelScope.launch {
            // UI indicator of executing a command
            val userPrompt = when (command) {
                "quiz me" -> "📝 Quiz me, Venky!"
                "hint" -> "💡 Give me a hint"
                "explain again" -> "🔄 Explain this differently"
                "summarize" -> "📋 Summarize our session progress"
                else -> "❓ I still don't understand, simplify it!"
            }

            val userMsg = ChatMessage(sessionId = sessionId, sender = "USER", content = userPrompt)
            chatDao.insertMessage(userMsg)

            val updatedSession = session.copy(lastUpdated = System.currentTimeMillis())
            chatDao.updateSession(updatedSession)

            // Build payload for Gemini with command directive
            val requestPrompt = when (command) {
                "quiz me" -> "[Command: quiz me on the current topic]"
                "hint" -> "[Command: hint for current problem]"
                "explain again" -> "[Command: explain again using a completely different analogy]"
                "summarize" -> "[Command: summarize session progress so far]"
                else -> "[Command: I don't understand, break down to absolute basics]"
            }

            val commandMsg = ChatMessage(sessionId = sessionId, sender = "USER", content = requestPrompt)
            val allMsgs = _messages.value + commandMsg

            val response = GeminiClient.generateResponse(allMsgs, updatedSession)

            chatDao.insertMessage(ChatMessage(sessionId = sessionId, sender = "VENKY", content = response))
            speakText(response)
            _isLoading.value = false
        }
    }

    fun resetToOnboarding() {
        _activeSessionId.value = null
        _showOnboarding.value = true
    }
}
