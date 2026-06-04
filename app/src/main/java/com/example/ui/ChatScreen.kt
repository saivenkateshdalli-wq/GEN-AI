package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChatMessage
import com.example.data.ChatSession
import com.example.ui.components.MarkdownText
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Custom Color Palette for VENKY (Indian Royal Theme: Orange/Amber & Royal Indigo)
val DeepIndigo = Color(0xFF0F172A)
val SoftIndigoBg = Color(0xFF1E293B)
val SaffronOrange = Color(0xFFF97316)
val SunnyYellow = Color(0xFFFBBF24)
val WarmBeige = Color(0xFFFFFBEB)
val LightSlate = Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessionsFlow.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showOnboarding by viewModel.showOnboarding.collectAsState()
    val showCelebration by viewModel.showCelebration.collectAsState()
    val isAiVoiceModeEnabled by viewModel.isAiVoiceModeEnabled.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DeepIndigo,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Logo and Subtitle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_venky_mascot),
                            contentDescription = "VENKY Mascot Logo",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SaffronOrange.copy(alpha = 0.2f))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "VENKY AI",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "University Buddy & Tutor",
                                color = SunnyYellow,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Start Consultation Button
                    Button(
                        onClick = {
                            viewModel.resetToOnboarding()
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_new_learning_drawer_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New Consultation", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Consultation", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "PAST DISCUSSIONS",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Sessions List
                    if (sessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No learning records yet.\nSet your profile to begin!",
                                color = Color.LightGray.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sessions) { s ->
                                val isSelected = s.id == activeSessionId
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectSession(s.id)
                                            coroutineScope.launch { drawerState.close() }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) SoftIndigoBg else Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = s.topicDisplayName,
                                                color = if (isSelected) SunnyYellow else Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Student: ${s.studentName}",
                                                color = Color.LightGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteSession(s) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Discussion",
                                                tint = Color.Red.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = "Made for University Students 🎓",
                        color = Color.LightGray.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        content = {
            if (showOnboarding) {
                OnboardingScreen(
                    onStartSession = { name, gender, subject, topic, understanding ->
                        viewModel.createNewSession(name, gender, subject, topic, understanding)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                activeSession?.let { session ->
                    MainChatWorkspace(
                        session = session,
                        messages = messages,
                        inputText = inputText,
                        isLoading = isLoading,
                        showCelebration = showCelebration,
                        isAiVoiceModeEnabled = isAiVoiceModeEnabled,
                        onSendMessage = { viewModel.sendMessage() },
                        onInputChanged = { viewModel.updateInputText(it) },
                        onToggleMode = { viewModel.toggleMode() },
                        onToggleAiVoiceMode = { viewModel.toggleAiVoiceMode() },
                        onQuickCommand = { viewModel.executeQuickCommand(it) },
                        onAddTag = { isUnderstood, tag -> viewModel.addNewTopicTag(isUnderstood, tag) },
                        onRemoveTag = { isUnderstood, tag -> viewModel.removeTopicTag(isUnderstood, tag) },
                        onMenuClicked = { coroutineScope.launch { drawerState.open() } },
                        onSpeak = { text -> viewModel.speakText(text, force = true) },
                        onStopSpeak = { viewModel.stopTts() },
                        modifier = modifier
                    )
                } ?: run {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SaffronOrange)
                    }
                }
            }
        }
    )
}

// ---------------- BACKGROUND BANNER & ONBOARDING SETUP SCREEN ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onStartSession: (String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var studentName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("BOY") } // BOY, GIRL, OTHER
    var selectedSubject by remember { mutableStateOf("Computer Science") }
    var topicTitle by remember { mutableStateOf("") }
    var understandingText by remember { mutableStateOf("") }

    val subjectsList = listOf(
        "Computer Science 💻",
        "Mathematics 📐",
        "Physics ⚛️",
        "Chemistry 🧪",
        "Economics 📈",
        "English 📚",
        "History 🏛️",
        "Motivation & Study Tips 🌟"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Live Rotating Space Background
        RotatingSpaceBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "🎓 Gen AI",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DeepIndigo.copy(alpha = 0.5f)
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                // Elegant Background Accent Glows
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-40).dp)
                    .background(SaffronOrange.copy(alpha = 0.15f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-60).dp, y = 60.dp)
                    .background(SunnyYellow.copy(alpha = 0.15f), CircleShape)
            )

            // Dynamic setup Card content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    // Profile branding block
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_venky_mascot),
                            contentDescription = "VENKY Greeting Avatar",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(SaffronOrange.copy(alpha = 0.15f))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "I am VENKY",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Your India-style University Socratic AI Companion. Let's get to know you to customize our tone and lessons!",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                item {
                    // Card wrapper for the form to give visual structure
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftIndigoBg.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Section: Profile Header
                            Text("1. TELL ME ABOUT YOURSELF", color = SunnyYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                            // Name field
                            OutlinedTextField(
                                value = studentName,
                                onValueChange = { studentName = it },
                                label = { Text("What is your name?", color = Color.LightGray) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronOrange,
                                    unfocusedBorderColor = Color.Gray,
                                    cursorColor = SaffronOrange
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_name_input")
                            )

                            // Gender Chips for customized interactive persona
                            Text("Your Gender:", color = Color.White, fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val genders = listOf("BOY" to "👦 Boy", "GIRL" to "👧 Girl", "OTHER" to "🧑 Other")
                                genders.forEach { (key, label) ->
                                    val isSelected = selectedGender == key
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) SaffronOrange else Color.DarkGray)
                                            .clickable { selectedGender = key }
                                            .wrapContentSize(Alignment.Center)
                                    ) {
                                        Text(
                                            text = label,
                                            color = Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            // Section: Subject consultation
                            Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                            Text("2. WHAT ARE WE STUDYING TODAY?", color = SunnyYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                            // Quick Subject Picker list
                            Text("Subject Choice:", color = Color.White, fontSize = 13.sp)
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(subjectsList) { sub ->
                                    val bareSubject = sub.substringBefore(" ").trim()
                                    val isSelected = selectedSubject == bareSubject ||
                                            (sub.contains("Motivation") && selectedSubject.contains("Motivation")) ||
                                            sub.startsWith(selectedSubject)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isSelected) SaffronOrange.copy(alpha = 0.25f) else Color.DarkGray.copy(alpha = 0.5f))
                                            .clickable { selectedSubject = bareSubject }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = sub,
                                            color = if (isSelected) SunnyYellow else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // Specific Topic Input
                            OutlinedTextField(
                                value = topicTitle,
                                onValueChange = { topicTitle = it },
                                label = { Text("Which specific topic? (e.g. Limits or Data Structures)", color = Color.LightGray) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronOrange,
                                    unfocusedBorderColor = Color.Gray,
                                    cursorColor = SaffronOrange
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_topic_input")
                            )

                            // Initial understanding input
                            OutlinedTextField(
                                value = understandingText,
                                onValueChange = { understandingText = it },
                                label = { Text("What do you already understand about this point?", color = Color.LightGray) },
                                shape = RoundedCornerShape(10.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronOrange,
                                    unfocusedBorderColor = Color.Gray,
                                    cursorColor = SaffronOrange
                                ),
                                maxLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_understanding_input")
                            )
                        }
                    }
                }

                item {
                    // Start Button
                    val isFormValid = studentName.isNotBlank() && topicTitle.isNotBlank()
                    Button(
                        onClick = {
                            if (isFormValid) {
                                onStartSession(
                                    studentName.trim(),
                                    selectedGender,
                                    selectedSubject,
                                    topicTitle.trim(),
                                    understandingText.trim()
                                )
                            }
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaffronOrange,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_tutor_button")
                    ) {
                        Text(
                            text = "Start Learning with VENKY! 🔥",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isFormValid) Color.White else Color.LightGray
                        )
                    }
                }
            }
        }
    }
    }
}


// ---------------- MAIN WORKSPACE ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatWorkspace(
    session: ChatSession,
    messages: List<ChatMessage>,
    inputText: String,
    isLoading: Boolean,
    showCelebration: Boolean,
    isAiVoiceModeEnabled: Boolean,
    onSendMessage: () -> Unit,
    onInputChanged: (String) -> Unit,
    onToggleMode: () -> Unit,
    onToggleAiVoiceMode: () -> Unit,
    onQuickCommand: (String) -> Unit,
    onAddTag: (Boolean, String) -> Unit,
    onRemoveTag: (Boolean, String) -> Unit,
    onMenuClicked: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isTrackerExpanded by remember { mutableStateOf(false) }

    // Keyboard action helper
    val keyboardController = LocalSoftwareKeyboardController.current

    // Automatically scroll to bottom when messages load
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        RotatingSpaceBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_venky_mascot),
                            contentDescription = "VENKY Mascot Avatar Mini",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "VENKY",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (session.activeMode == "EDUCATION") "Education (Socratic Tutor) 🎓" else "Companion (Bhai / Di) 💬",
                                fontSize = 11.sp,
                                color = SunnyYellow
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClicked) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Open History", tint = Color.White)
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAiVoiceModeEnabled) SaffronOrange.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f))
                            .clickable { onToggleAiVoiceMode() }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAiVoiceModeEnabled) "🗣️ Read: ON" else "🗣️ Read: OFF",
                            color = if (isAiVoiceModeEnabled) SunnyYellow else Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    // Mode Switch Button Chip with generous visual feedback
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (session.activeMode == "EDUCATION") SaffronOrange else SunnyYellow)
                            .clickable { onToggleMode() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (session.activeMode == "EDUCATION") "📚 Tutor Mode" else "💬 Companion",
                            color = if (session.activeMode == "EDUCATION") Color.White else DeepIndigo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepIndigo.copy(alpha = 0.85f)
                )
            )
        },
        bottomBar = {
            // Typing container
            Column(
                modifier = Modifier
                    .background(DeepIndigo.copy(alpha = 0.82f))
                    .navigationBarsPadding()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick commands scrollable container
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val commands = listOf(
                        "quiz me" to "📝 Quiz me",
                        "hint" to "💡 Hint",
                        "explain again" to "🔄 Explain alternative",
                        "summarize" to "📋 Summarize progress",
                        "confused" to "❓ Im Confused"
                    )
                    items(commands) { (key, label) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(SoftIndigoBg)
                                .clickable { onQuickCommand(key) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Chat Input and send button configuration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChanged,
                        placeholder = { Text("Push your doubts here...", color = Color.LightGray) },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaffronOrange,
                                unfocusedBorderColor = Color.Gray,
                                focusedContainerColor = SoftIndigoBg.copy(alpha = 0.85f),
                                unfocusedContainerColor = SoftIndigoBg.copy(alpha = 0.85f),
                                cursorColor = SaffronOrange
                            ),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    onSendMessage()
                                    keyboardController?.hide()
                                }
                            }
                        )
                    )

                    // Touch target of 48dp met
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage()
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(SaffronOrange, CircleShape)
                            .testTag("send_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(DeepIndigo.copy(alpha = 0.45f))
        ) {
            // Collapsible Profile tracker for Understood / Confused targets
            CollapsibleTrackerPanel(
                session = session,
                isExpanded = isTrackerExpanded,
                onToggleExpand = { isTrackerExpanded = !isTrackerExpanded },
                onAddTag = onAddTag,
                onRemoveTag = onRemoveTag
            )

            // Conversation message log viewport
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageItem(
                            message = message,
                            onSpeak = onSpeak,
                            onStopSpeak = onStopSpeak
                        )
                    }

                    if (isLoading) {
                        item {
                            BrewingChaiThinkingItem()
                        }
                    }
                }
            }
        }
    }
        if (showCelebration) {
            ConfettiCelebrationOverlay()
        }
    }
}

// ---------------- COLLAPSIBLE TRACKING PANEL ----------------
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CollapsibleTrackerPanel(
    session: ChatSession,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddTag: (Boolean, String) -> Unit,
    onRemoveTag: (Boolean, String) -> Unit
) {
    var newTagInput by remember { mutableStateOf("") }
    var tagTargetUnderstood by remember { mutableStateOf(true) } // Understood or Confused

    val understoodList = session.understoodTopics.split(";").filter { it.isNotBlank() }
    val confusedList = session.confusedTopics.split(";").filter { it.isNotBlank() }

    Surface(
        color = SoftIndigoBg,
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header trigger strip meeting Touch target requirements
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "STUDY PROFILE: ${session.studentName}'s Progress",
                        color = SunnyYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${session.subject} • ${session.topic}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Understood vs Confused Count Badges
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .background(Color.Green.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("✅ ${understoodList.size}", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("❌ ${confusedList.size}", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Study Profile",
                        tint = Color.LightGray
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Concept tags lists
                    Text("✅ Understood Concepts (Tracked):", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (understoodList.isEmpty()) {
                        Text("No concepts marked understood yet.", color = Color.Gray, fontSize = 11.sp)
                    } else {
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            understoodList.forEach { tag ->
                                TagChip(tag = tag, isUnderstood = true, onRemove = { onRemoveTag(true, tag) })
                            }
                        }
                    }

                    Text("❌ Still Confused / Needs Practice:", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (confusedList.isEmpty()) {
                        Text("No issues tracked! Good work.", color = Color.Gray, fontSize = 11.sp)
                    } else {
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            confusedList.forEach { tag ->
                                TagChip(tag = tag, isUnderstood = false, onRemove = { onRemoveTag(false, tag) })
                            }
                        }
                    }

                    // Section to add a tag inline dynamically
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTagInput,
                            onValueChange = { newTagInput = it },
                            placeholder = { Text("E.g. Integration, Recursion", fontSize = 12.sp, color = Color.LightGray) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaffronOrange,
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        // Toggle classification of tag
                        Button(
                            onClick = {
                                tagTargetUnderstood = !tagTargetUnderstood
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tagTargetUnderstood) Color.Green.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(if (tagTargetUnderstood) "Mark ✅" else "Mark ❌", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (newTagInput.isNotBlank()) {
                                    onAddTag(tagTargetUnderstood, newTagInput.trim())
                                    newTagInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(tag: String, isUnderstood: Boolean, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .background(
                if (isUnderstood) Color.Green.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tag,
            color = if (isUnderstood) Color.Green else Color.Red,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove tag",
            tint = if (isUnderstood) Color.Green else Color.Red,
            modifier = Modifier
                .size(14.dp)
                .clickable { onRemove() }
        )
    }
}


data class ExtractedLink(val label: String, val url: String)

fun extractLinks(text: String): List<ExtractedLink> {
    val links = mutableListOf<ExtractedLink>()
    
    // Pattern 1: Markdown style [Label](http://url)
    val markdownRegex = Regex("\\[([^\\]]+)\\]\\((https?://[^\\s\\)]+)\\)")
    markdownRegex.findAll(text).forEach { matchResult ->
        val label = matchResult.groupValues[1]
        val url = matchResult.groupValues[2]
        links.add(ExtractedLink(label, url))
    }
    
    // Pattern 2: Raw URLs that are not part of a markdown link
    val urlRegex = Regex("(https?://[^\\s\\)\\]\\*]+)")
    urlRegex.findAll(text).forEach { matchResult ->
        val url = matchResult.value
        if (links.none { it.url == url }) {
            val label = if (url.contains("youtube.com") || url.contains("youtu.be")) {
                val queryVal = url.substringAfter("search_query=", "").replace("+", " ").trim()
                if (queryVal.isNotEmpty()) "Search YouTube: $queryVal" else "Related YouTube Video"
            } else {
                "Learn More"
            }
            links.add(ExtractedLink(label, url))
        }
    }
    return links
}

// ---------------- CHAT MESSAGE ROW RENDERING ----------------
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onSpeak: (String) -> Unit = {},
    onStopSpeak: () -> Unit = {}
) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    when (message.sender) {
        "SYSTEM" -> {
            // Elegant centered status notice for modes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SunnyYellow.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = message.content,
                        color = SunnyYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        "USER" -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .wrapContentWidth(Alignment.End)
                        .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                        .background(SaffronOrange)
                        .padding(12.dp)
                ) {
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(
                            text = message.content,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        else -> { // VENKY
            val extractedLinks = remember(message.content) { extractLinks(message.content) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                // Circle Mascot portrait
                Image(
                    painter = painterResource(id = R.drawable.ic_venky_mascot),
                    contentDescription = "VENKY Profile",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SaffronOrange.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp))
                        .background(SoftIndigoBg)
                        .padding(12.dp)
                ) {
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        MarkdownText(
                            text = message.content,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    // Render extracted links (e.g., YouTube related topics) beautifully as individual buttons
                    if (extractedLinks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color.LightGray.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Visual Learning Resources:",
                            color = SunnyYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Render each link as a high-fidelity interactive button
                        extractedLinks.forEach { link ->
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Red.copy(alpha = 0.15f))
                                    .clickable {
                                        try {
                                            uriHandler.openUri(link.url)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📺 ",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = link.label,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "➔",
                                    color = SaffronOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SunnyYellow.copy(alpha = 0.15f))
                                .clickable { onSpeak(message.content) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🗣️ Speak Out",
                                color = SunnyYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { onStopSpeak() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏹️ Stop",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- LOADING CHAI BREWING STATE ----------------
@Composable
fun BrewingChaiThinkingItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_venky_mascot),
            contentDescription = "VENKY Profile Mini",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SaffronOrange.copy(alpha = 0.15f))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp))
                .background(SoftIndigoBg)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Venky is brewing Irani Chai & your answer... ☕️",
                    color = SunnyYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    color = SaffronOrange,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

// ---------------- DYNAMIC LIVE ROTATING SPACE BACKGROUND ----------------
@Composable
fun RotatingSpaceBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpaceAnim")

    val galaxyRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180000, easing = LinearEasing)
        ),
        label = "GalaxyRotation"
    )
    
    val nebulaPulsation by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "NebulaPulsation"
    )

    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarTwinkle"
    )

    val backgroundStars = remember {
        val rand = java.util.Random(42)
        List(200) {
            Triple(rand.nextFloat(), rand.nextFloat(), 0.3f + rand.nextFloat() * 1.5f)
        }
    }
    
    val galaxyDustDrops = remember {
        val rand = java.util.Random(123)
        List(350) {
            val angle = rand.nextFloat() * 2 * Math.PI.toFloat()
            // Make spiral distribution with exponential falloff so core is dense
            val radiusDist = rand.nextFloat()
            val radius = radiusDist * radiusDist
            Pair(angle, radius to (rand.nextFloat() * 2.5f + 0.5f))
        }
    }

    Canvas(modifier = modifier.fillMaxSize().background(Color(0xFF01030B))) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val maxDim = size.maxDimension
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF4C1D95).copy(alpha = 0.08f), Color.Transparent),
                center = Offset(width * 0.2f, height * 0.3f),
                radius = width * 0.6f
            ),
            radius = width * 0.6f,
            center = Offset(width * 0.2f, height * 0.3f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.06f), Color.Transparent),
                center = Offset(width * 0.8f, height * 0.7f),
                radius = width * 0.5f
            ),
            radius = width * 0.5f,
            center = Offset(width * 0.8f, height * 0.7f)
        )

        // 1. Draw Deep Space Background Stars
        backgroundStars.forEach { (xRatio, yRatio, scale) ->
            val phase = (xRatio * 20f) + (yRatio * 20f)
            val twinkleAlpha = ((sin((starTwinkle * 2 * Math.PI.toFloat()) + phase) + 1f) / 2f) * 0.7f + 0.3f
            
            drawCircle(
                color = if (scale > 1.2f) Color(0xFFBAE6FD).copy(alpha = twinkleAlpha) else Color.White.copy(alpha = twinkleAlpha * 0.7f),
                radius = 1.0f * scale,
                center = Offset(xRatio * width, yRatio * height)
            )
        }

        rotate(galaxyRotation, center) {
            // 2. Draw Supermassive Glow (Galactic Core)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF), 
                        Color(0xFFFDE047).copy(alpha = 0.6f), 
                        Color(0xFFF97316).copy(alpha = 0.3f),
                        Color(0xFFC084FC).copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = width * 0.7f * nebulaPulsation
                ),
                radius = width * 0.7f * nebulaPulsation,
                center = center
            )
            
            // 3. Draw Inner Nebula Clouds
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.2f),
                        Color(0xFF818CF8).copy(alpha = 0.15f),
                        Color(0xFFC084FC).copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = width * 0.9f * (2f - nebulaPulsation)
                ),
                radius = width * 0.9f * (2f - nebulaPulsation),
                center = center
            )
            
            // 4. Draw Spiral Arms (Simulated by drawing large angled oval gradients)
            rotate(45f, center) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD8B4FE).copy(alpha = 0.18f), Color.Transparent),
                        center = center,
                        radius = width * 0.7f
                    ),
                    topLeft = Offset(center.x - width * 0.9f, center.y - width * 0.25f),
                    size = Size(width * 1.8f, width * 0.5f)
                )
            }
            
            rotate(-45f, center) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.15f), Color.Transparent),
                        center = center,
                        radius = width * 0.7f
                    ),
                    topLeft = Offset(center.x - width * 0.8f, center.y - width * 0.2f),
                    size = Size(width * 1.6f, width * 0.4f)
                )
            }

            // 5. Draw Swirling Galaxy Stars & Dust
            val maxRadius = maxDim * 0.8f
            galaxyDustDrops.forEachIndexed { index, (angle, data) ->
                val (radiusRatio, scale) = data
                val radius = radiusRatio * maxRadius
                
                // Add spiral curve depending on radius
                val spiralAngle = angle + (radius / maxRadius) * 4.5f 
                
                val x = center.x + radius * cos(spiralAngle)
                val y = center.y + radius * sin(spiralAngle) * 0.6f // flatten it slightly for 3D look
                
                val distFromCenter = radius / maxRadius
                val alpha = (1f - distFromCenter).coerceIn(0.1f, 1f)
                
                val color = if (index % 4 == 0) Color(0xFFC084FC) 
                            else if (index % 3 == 0) Color(0xFF38BDF8)
                            else if (index % 5 == 0) Color(0xFFFDE047)
                            else Color.White
                
                drawCircle(
                    color = color.copy(alpha = alpha * 0.9f),
                    radius = 1.5f * scale,
                    center = Offset(x.toFloat(), y.toFloat())
                )
                
                // Draw glow for bigger dust
                if (scale > 1.8f) {
                    drawCircle(
                        color = color.copy(alpha = alpha * 0.2f),
                        radius = 6f * scale,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }
        
        // 6. Subtle foreground flares/nebula 
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1D4ED8).copy(alpha = 0.1f), Color.Transparent),
                center = Offset(width * 0.1f, height * 0.9f),
                radius = width * 0.5f
            ),
            radius = width * 0.5f,
            center = Offset(width * 0.1f, height * 0.9f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF7E22CE).copy(alpha = 0.08f), Color.Transparent),
                center = Offset(width * 0.9f, height * 0.1f),
                radius = width * 0.4f
            ),
            radius = width * 0.4f,
            center = Offset(width * 0.9f, height * 0.1f)
        )
    }
}

data class StaticParticle(
    val startX: Float,
    val startY: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color,
    val size: Float,
    val startRotation: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiCelebrationOverlay() {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan, SaffronOrange, SunnyYellow)
    
    val particles = remember {
        val rand = java.util.Random(456)
        List(120) {
            StaticParticle(
                startX = rand.nextFloat(),
                startY = -rand.nextFloat() * 0.3f,
                speedX = rand.nextFloat() * 1.5f - 0.75f,
                speedY = rand.nextFloat() * 0.5f + 0.4f,
                color = colors[rand.nextInt(colors.size)],
                size = rand.nextFloat() * 16f + 8f,
                startRotation = rand.nextFloat() * 360f,
                rotationSpeed = rand.nextFloat() * 360f - 180f
            )
        }
    }

    var elapsedTime by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (true) {
            val current = System.currentTimeMillis()
            elapsedTime = (current - startTime) / 1000f
            kotlinx.coroutines.delay(16)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            for (p in particles) {
                val gravityFactor = 0.12f
                val py = (p.startY + p.speedY * elapsedTime + 0.5f * gravityFactor * elapsedTime * elapsedTime) * canvasHeight
                val px = (p.startX + p.speedX * elapsedTime) * canvasWidth
                val rotation = p.startRotation + p.rotationSpeed * elapsedTime

                if (py > canvasHeight + p.size) continue

                drawContext.canvas.save()
                drawContext.canvas.translate(px, py)
                drawContext.canvas.rotate(rotation)
                drawRect(
                    color = p.color,
                    topLeft = Offset(-p.size / 2, -p.size / 2),
                    size = Size(p.size, p.size)
                )
                drawContext.canvas.restore()
            }
        }
        
        // Text scale heartbeat animation
        val infiniteTransition = rememberInfiniteTransition(label = "ConfettiPulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Text(
            text = "Concept Mastered! 🎓",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SunnyYellow,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .background(DeepIndigo.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        )
    }
}