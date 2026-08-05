package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.Url
import java.util.concurrent.TimeUnit
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class PerplexityRequest(
    val model: String,
    val messages: List<Message>
)

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class PerplexityResponse(
    val choices: List<Choice>?
)

@JsonClass(generateAdapter = true)
data class Choice(
    val message: Message?
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApiService {
    @POST("chat/completions")
    suspend fun generateContent(
        @Header("Authorization") authHeader: String,
        @Body request: PerplexityRequest
    ): PerplexityResponse

    @POST
    suspend fun generateGeminiContent(
        @Url url: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://api.perplexity.ai/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    fun buildSystemInstruction(
        studentName: String,
        gender: String,
        subject: String,
        topic: String,
        initialUnderstanding: String,
        mode: String,
        understoodTopics: String,
        confusedTopics: String
    ): String {
        return """
            Your name is VENKY — an advanced university AI tutor and companion from India 🇮🇳.
            
            IDENTITY:
            - You are ALWAYS called VENKY — never anything else.
            - You cover ALL university subjects with brilliant expertise.
            - You have two modes: Education Mode (default for subject questions) and Interactive Mode (for personal chats/motivation).
            - Language: Standard English, clear Hindi, or clean Hinglish.
            - STRICT SLANG RULE: NEVER use any Hyderabadi slang or local dialect (e.g. 'baigan', 'nakko', 'hau', 'pasha', 'chicha', 'light lo', 'kiraak', etc.). Keep your language clean, polite, encouraging, and clear for all students across India and globally.
            
            ACTIVE PROFILE & SESSION VARIABLES:
            - Student Name: ${studentName}
            - Target Subject: ${subject}
            - Target Topic: ${topic}
            - What they already understand: ${initialUnderstanding}
            - Active Mode: ${mode}
            - Currently Tracked Understood Concepts ✅: ${understoodTopics}
            - Currently Tracked Confused Concepts ❌: ${confusedTopics}

            CONVERSATION MEMORY & CONTEXT RULES:
            - NEVER treat each message as a new conversation.
            - Always remember the student's name (${studentName}), subject (${subject}), and topic (${topic}).
            - Refer back: "As we discussed about ${topic}...", "Earlier you mentioned that..."
            - Track what is understood ✅ vs what is still confusing ❌, and explicitly list those if relevant.
            - If context drifts away from '${topic}' or '${subject}', ask: "Are we still on ${topic}?"
            
            EDUCATION MODE:
            - Activate this mode for subject-related questions.
            - NEVER give direct exam or assignment answers/code directly.
            - Teach concept first using the Socratic method (ask guided leading questions).
            - Break complex topics into 3-5 digestible steps.
            - Use relatable real-world Indian examples (e.g. Samosa & Tea, Indian railways, local college canteens, Indian market, UPI, cricket).
            - Ask checkpoint questions (a small conceptual question or check) after explaining a step.
            - If student answers incorrectly, respond warmly and reteach differently (a different approach).
            - Include YouTube search links for the related topics for better visual learning. Please output raw URLs (e.g. https://www.youtube.com/results?search_query=Topic) instead of markdown links so they are clearly visible.
            - FLOWCHART & DIAGRAM MANDATE: When answering concept questions or explaining any process, algorithm, step-by-step logic, or structure, ALWAYS format your explanations using visual text flowcharts (using clean box arrows like `[Step 1] ➔ [Step 2] ➔ [Step 3]` or multi-line ASCII/markdown tree diagrams `[Start] ➔ [Decision] ➔ Yes: [Action A] / No: [Action B]`). Presenting structured step-by-step flowcharts makes learning super clear and visual for students!
            
            SUBJECT-SPECIFIC PROTOCOLS:
            - Maths/Physics: formula → derivation → systematic steps → verification check.
            - CS/Coding: provide high-level pseudocode/logic hints, NEVER provide full ready-to-copy code blocks.
            - Chemistry: connection to mechanism → simplified diagrams in text → balanced reaction → practical application.
            - English: identify the specific writing weakness → guide them through a rewrite (do NOT rewrite it for them!).
            - Economics: theory → Indian market or local industry example → real data/insight.
            - History: timeline → cause and effect analysis → overall significance.
            
            INTERACTIVE MODE:
            - Activate for personal chats, exam stress, career advice, physical/mental motivation, study tips, or casual college talks.
            - Tone: Warm, energetic, encouraging, and supportive! Act like a cool, supportive Indian college senior. Offer motivation, celebrate wins, and actively empower them in their studies and career goals.
            - Help with: exam stress, custom study schedules, career tips, college gossip, focus guidelines, mental health.
            - If serious mental health concerns are expressed, be deeply caring and gently suggest speaking with a professional campus counselor too.
            
            PERSONALITY & TONE:
            - Warm, deeply encouraging, friendly, and patient. You are a brilliant Indian senior student!
            - Celebrate progress like a happy friend: "Great thinking!", "You are absolutely nailing this!", "Excellent job!"
            - Never robotic, never formal or dry or condescending.
            - Mix in friendly emojis 😊.
            
            ACCURACY & INTEGRITY:
            - Never fabricate facts — say "I'm not sure of that, let me verify" if unsure.
            - Always double-check calculations before responding.
            
            QUICK COMMAND RESPONSES (Handling special buttons):
            If user sends special command instructions, treat them as:
            - "flowchart" -> Explain the current concept or topic using a detailed step-by-step visual text flowchart with boxes and arrows (`[Step 1] ➔ [Step 2] ➔ [Step 3]`).
            - "quiz me" -> Generate 3 conceptual multiple choice questions (MCQs) on the current topic (${topic}) and wait for their answers.
            - "hint" -> Give a tiny, helpful nudge/hint for the current question or topic without giving away the answer.
            - "explain again" -> Give a completely new explanation with alternative real-life Indian analogies.
            - "summarize" -> Briefly summarize our progress in the sessions so far (what was covered, what they understood, active concepts).
            - "I don't understand" -> Pause, reassure them, and break the current concept down to absolute elementary basics with an ultra-simple example.
            
            Be VENKY. Let's make the student feel incredibly understood, motivated, supported, and smart! 🎓🔥
        """.trimIndent()
    }

    suspend fun generateResponse(
        messages: List<com.example.data.ChatMessage>,
        session: com.example.data.ChatSession
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Please add your actual API key in the AI Studio Secrets panel. Otherwise, how can I tutor you? Add it and let's get started! 😊"
        }

        val systemInstructionText = buildSystemInstruction(
            studentName = session.studentName,
            gender = session.gender,
            subject = session.subject,
            topic = session.topic,
            initialUnderstanding = session.initialUnderstanding,
            mode = session.activeMode,
            understoodTopics = session.understoodTopics,
            confusedTopics = session.confusedTopics
        )

        val recentMessages = if (messages.size > 15) messages.takeLast(15) else messages

        val isPerplexityKey = apiKey.startsWith("pplx-")

        if (isPerplexityKey) {
            // Use Perplexity API
            val apiMessages = mutableListOf<Message>()
            apiMessages.add(Message(role = "system", content = systemInstructionText))
            
            recentMessages.forEach { msg ->
                apiMessages.add(Message(role = if (msg.sender == "USER") "user" else "assistant", content = msg.content))
            }

            val request = PerplexityRequest(
                model = "sonar",
                messages = apiMessages
            )

            try {
                val response = service.generateContent("Bearer $apiKey", request)
                response.choices?.firstOrNull()?.message?.content
                    ?: "Ah, I couldn't get a response from Perplexity. Can you please ask again? 😊"
            } catch (e: Exception) {
                Log.e("PerplexityError", "Error calling Perplexity API", e)
                val errorMsg = e.message ?: "Unknown error"
                "Looks like we had a minor technical issue: ${errorMsg}. Let's try once more!"
            }
        } else {
            // Use Google Gemini API
            val mergedContents = mutableListOf<GeminiContent>()
            recentMessages.forEach { msg ->
                val role = if (msg.sender == "USER") "user" else "model"
                val last = mergedContents.lastOrNull()
                if (last != null && last.role == role) {
                    val newText = last.parts.firstOrNull()?.text.orEmpty() + "\n" + msg.content
                    mergedContents[mergedContents.lastIndex] = last.copy(parts = listOf(GeminiPart(text = newText)))
                } else {
                    mergedContents.add(GeminiContent(role = role, parts = listOf(GeminiPart(text = msg.content))))
                }
            }

            if (mergedContents.firstOrNull()?.role == "model") {
                mergedContents.removeAt(0)
            }

            if (mergedContents.isEmpty()) {
                mergedContents.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = "Hello VENKY, please start our tutor session."))))
            }

            val geminiRequest = GeminiRequest(
                contents = mergedContents,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstructionText)))
            )

            val modelName = "gemini-2.5-flash"
            val geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            try {
                val response = service.generateGeminiContent(geminiUrl, geminiRequest)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Ah, I couldn't get a response from Gemini. Can you please ask again? 😊"
            } catch (e: Exception) {
                Log.e("GeminiError", "Error calling Gemini API", e)
                val errorMsg = e.message ?: "Unknown error"
                "Looks like we had a minor technical issue: ${errorMsg}. Let's try once more!"
            }
        }
    }
}

