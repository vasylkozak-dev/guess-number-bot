import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.SendMessage
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

// Game state on chat
data class GameState(var target: Int, var attempts: Int = 0)

fun main() {
    val token = "TELEGRAM_BOT_TOKEN" // Insert your bot token here
    val bot = TelegramBot(token)
    println("Bot is started!")

    val games = ConcurrentHashMap<Long, GameState>()

    bot.setUpdatesListener({ updates ->
        updates.forEach { update -> handleUpdate(bot, update, games) }
        UpdatesListener.CONFIRMED_UPDATES_ALL
    }, { e -> e.printStackTrace() })

    CountDownLatch(1).await()
}

// Neutral response to nonsense
val defaultReplies = listOf(
    "Boom, there it is!"
    "I’ve got nothing to say to that"
    "What do you want?"
    "I’m just a bot"
    "Hit /play and try again!"
    "Let’s play. Your move!"
)


fun handleUpdate(
    bot: TelegramBot,
    update: Update,
    games: ConcurrentHashMap<Long, GameState>
) {
    val msg = update.message() ?: return
    val chatId = msg.chat().id()
    val text = msg.text() ?: return
    println("chatId: ${msg.chat().id()} | username: ${msg.from().username()}")


    when {
        text == "/start" -> {
            bot.execute(SendMessage(chatId,
                "Hi! I’m a crazy bot with a mini-game. Type /play — let’s play a guessing game (from 1 to 10)"))
        }

        text == "/play" -> {
            games[chatId] = GameState(target = Random.nextInt(1, 11))
            bot.execute(SendMessage(chatId, "I’ve picked a number between 1 and 10. Enter your number!"))
        }

        text == "/stop" -> {
            if (games.remove(chatId) != null)
                bot.execute(SendMessage(chatId, "Okay, the game’s stopped. Type /play to start again"))
            else
                bot.execute(SendMessage(chatId, "The game isn’t running right now. Type /play"))
        }

        // If the game is active — handle the move
        games.containsKey(chatId) -> {
            val guess = text.toIntOrNull()
            if (guess == null) {
                bot.execute(SendMessage(chatId, "You need to pick a number between 1 and 10. Try again!"))
                return
            }

            val state = games[chatId]!!
            state.attempts++

            when {
                guess < state.target -> bot.execute(SendMessage(chatId, "Higher ⬆️"))
                guess > state.target -> bot.execute(SendMessage(chatId, "Lower ⬇️"))
                else -> {
                    bot.execute(SendMessage(chatId, "You got it! It is ${state.target}. Tries: ${state.attempts}."))
                    games.remove(chatId) // stopping the game
                }
            }
        }

        // Otherwise — random reply
        else -> {
            val randomReply = defaultReplies.random()
            bot.execute(SendMessage(chatId, randomReply))
        }

    }
}
