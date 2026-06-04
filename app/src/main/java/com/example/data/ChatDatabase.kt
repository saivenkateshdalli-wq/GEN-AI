package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String = "",
    val gender: String = "OTHER", // "BOY", "GIRL", "OTHER"
    val subject: String = "",
    val topic: String = "",
    val initialUnderstanding: String = "",
    val activeMode: String = "EDUCATION", // "EDUCATION", "INTERACTIVE"
    val understoodTopics: String = "", // semicolon-separated
    val confusedTopics: String = "", // semicolon-separated
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val topicDisplayName: String
        get() = if (topic.isNotEmpty()) "$subject - $topic" else "New Consultation"
}

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val sender: String, // "USER", "VENKY"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY lastUpdated DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE id = :id")
    suspend fun getSessionById(id: Int): ChatSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession): Long

    @Update
    suspend fun updateSession(session: ChatSession)

    @Delete
    suspend fun deleteSession(session: ChatSession)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Int)
}

@Database(entities = [ChatSession::class, ChatMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}

object DatabaseProvider {
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            val inst = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "venky_tutor_db"
            ).fallbackToDestructiveMigration()
                .build()
            instance = inst
            inst
        }
    }
}

class ChatRepository(private val chatDao: ChatDao) {
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    suspend fun getSessionById(id: Int): ChatSession? = chatDao.getSessionById(id)

    suspend fun insertSession(session: ChatSession): Long = chatDao.insertSession(session)

    suspend fun updateSession(session: ChatSession) = chatDao.updateSession(session)

    suspend fun deleteSession(session: ChatSession) = chatDao.deleteSession(session)

    fun getMessages(sessionId: Int): Flow<List<ChatMessage>> = chatDao.getMessagesForSession(sessionId)

    suspend fun insertMessage(message: ChatMessage): Long = chatDao.insertMessage(message)
}
