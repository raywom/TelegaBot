import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale


class TelegramBotApp

fun main(){
    ApiContextInitializer.init()
    val bot=Bot()
    TelegramBotsApi().registerBot(bot)
}

class Bot: TelegramLongPollingBot() {
    override fun getBotUsername() = "how_to_install_bot"
    override fun getBotToken() = token()
    var games: MutableList<Game> = mutableListOf()
    var players: MutableList<Player> = getDataPlayer("dataPlayers.txt")
    var playersInGame = mutableMapOf<Int,Game>() // Int - userId

    override fun onUpdateReceived(update: Update) {
        fun resetAction(){
            if(playersInGame[update.callbackQuery.from.id]!!.readinessCheck()) {
                playersInGame[update.callbackQuery.from.id]!!.resetTurn()
                var index = 0
                //Global
                if(playersInGame[update.callbackQuery.from.id]!!.global.isGlobalEvent){
                    playersInGame[update.callbackQuery.from.id]!!.global.isGlobalEvent = false
                    execute(SendMessage().setChatId(playersInGame[update.callbackQuery.from.id]!!.chatId).setText(playersInGame[update.callbackQuery.from.id]!!.global.result))
                    if (playersInGame[update.callbackQuery.from.id]!!.global.typeEvent == TypeEvent.CAUGHT)
                        execute(SendMessage().setChatId(playersInGame[update.callbackQuery.from.id]!!.global.caughtUserId.toLong()).setText("Вас поймали!!!"))
                    else if (playersInGame[update.callbackQuery.from.id]!!.global.typeEvent == TypeEvent.MASCAUGHT){
                        for(index in 0 until playersInGame[update.callbackQuery.from.id]!!.global.listCaughtUserId.count()){
                            execute(SendMessage().setChatId(playersInGame[update.callbackQuery.from.id]!!.global.listCaughtUserId[index].toLong()).setText("Вас поймали!!!"))
                        }
                    }
                    else if (playersInGame[update.callbackQuery.from.id]!!.global.typeEvent == TypeEvent.ESCAPED){
                        for(index in 0 until playersInGame[update.callbackQuery.from.id]!!.global.listFugitiveUserId.count()){
                            execute(SendMessage().setChatId(playersInGame[update.callbackQuery.from.id]!!.global.listFugitiveUserId[index].toLong()).setText("Вы сбежали!!!"))
                        }
                    }
                    if (playersInGame[update.callbackQuery.from.id]!!.endGameCheck()) {
                        execute(SendMessage().setChatId(playersInGame[update.callbackQuery.from.id]!!.chatId).setText("Игра завершена\n" +
                                "Пойманных игроков: ${playersInGame[update.callbackQuery.from.id]!!.countOfCaught}/${playersInGame[update.callbackQuery.from.id]!!.countOfPlayers}"))
                        games.remove(findGame(games, playersInGame[update.callbackQuery.from.id]!!.chatId))
                        playersInGame[update.callbackQuery.from.id]!!.hunter.isPlaying = false
                        saveHunterData(playersInGame[update.callbackQuery.from.id]!!.hunter, players)
                        playersInGame.remove(playersInGame[update.callbackQuery.from.id]!!.hunter.userId)
                    }
                    playersInGame.remove(playersInGame[update.callbackQuery.from.id]!!.global.caughtUserId)
                }
                //Global

                for (chatId in playersInGame[update.callbackQuery.from.id]?.let { getMutableListUserId(it) }!!) {
                    if (playersInGame[update.callbackQuery.from.id]!!.hunter.userId.toLong() != chatId) {
                        if(!playersInGame[update.callbackQuery.from.id]!!.passingList[index]) {
                            execute(SendMessage().setChatId(chatId).setText(playersInGame[update.callbackQuery.from.id]!!.getResultPlayers(index)))
                            if (!playersInGame[update.callbackQuery.from.id]!!.getPlayerByChatId(chatId.toInt()).isHidden) {
                                execute(SendMessage().setChatId(chatId).setText("Ваши действия").setReplyMarkup(getActionButtons()))
                            }
                            else if (playersInGame[update.callbackQuery.from.id]!!.getPlayerByChatId(chatId.toInt()).isHidden){
                                execute(SendMessage().setChatId(chatId).setText("Ваши действия").setReplyMarkup(getActionButtonInHitch()))
                            }
                            playersInGame[update.callbackQuery.from.id]!!.playersHistory[index].result = ""
                        }
                        index++
                    }
                    else {
                        if (playersInGame[update.callbackQuery.from.id]!!.numberMoves >= 3) {
                            execute(SendMessage().setChatId(chatId).setText(playersInGame[update.callbackQuery.from.id]!!.getResultHunter()))
                            execute(SendMessage().setChatId(chatId).setText("Ваши действия").setReplyMarkup(getActionButtonsForHunter()))
                        }
                        else {
                            execute(SendMessage().setChatId(chatId).setText("Осталось ждать ${3 - playersInGame[update.callbackQuery.from.id]!!.numberMoves}"))
                            playersInGame[update.callbackQuery.from.id]?.let { standStill("hunter", 0, it) }
                        }
                        playersInGame[update.callbackQuery.from.id]!!.hunterHistory.result = ""
                    }
                }
                playersInGame[update.callbackQuery.from.id]!!.clearResult()
            }
        }

        fun lobbyTimer() : Boolean
        {
            Thread.sleep(60000L)
            if (findGame(games, update.message.chatId).phase != GamePhase.WAITING)
                return true
            execute(SendMessage().setChatId(update.message.chatId).setText("Осталось одна минута до закрытия лоби"))
            Thread.sleep(30000L)
            if (findGame(games, update.message.chatId).phase != GamePhase.WAITING)
                return true
            execute(SendMessage().setChatId(update.message.chatId).setText("Осталось 30 секунд до закрытия лоби"))
            Thread.sleep(30000L)
            if (findGame(games, update.message.chatId).phase == GamePhase.WAITING) {
                execute(SendMessage().setChatId(update.message.chatId).setText("Игра отменена"))
                games.remove(findGame(games, update.message.chatId))
            }
            return false
        }

        if (update.hasMessage()) {
            println("ChatID" + update.message.from + "\nTime: " + getCurrentTime() + "\nMsgText: " + update.message.text + "\n")

            //command start
            if (update.message.text == "/start") {
                Thread {
                    if (addPlayer(players, update.message.from.id)) {
                        players.add(Player(update.message.from.id, update.message.from.firstName))
                        saveDataPlayers("dataPlayers.txt", players)
                        //addToDatabase(update.message.from.id, update.message.from.userName)
                    }
                }.start()
            }
            //command game
            else if (update.message.text == "/game" || update.message.text == "/game@$botUsername") {
                if (createGame(games, update.message.chatId)) {
                    execute(SendMessage().setChatId(update.message.chatId).setText("Начался набор игры в прятки\nНажмите /join@$botUsername"))
                    Thread{
                        lobbyTimer()
                    }.start()
                }
            }
            //command join
            else if (update.message.text == "/join" || update.message.text == "/join@$botUsername") {
                var result = joinGame(games, players, update.message.chatId, update.message.from.id)
                if (result.userId != 0) {
                    if (result.isPlaying)
                        execute(SendMessage().setChatId(result.userId.toLong()).setText("ты заебал, ты понимаешь что ты уже в игре нахуй???"))
                    else {
                        execute(SendMessage().setChatId(result.userId.toLong()).setText("Нахуй пишешь?"))
                        result.isPlaying = true
                    }
                }
            }
            //command start game
            else if (update.message.text == "/start_game" || update.message.text == "/start_game@$botUsername") {
                val findGame = findGame(games, update.message.chatId)
                if(findGame.players.count() > 1) {
                    if (startGame(games, update.message.chatId)) {
                        val game = findGame(games, update.message.chatId)
                        execute(SendMessage().setChatId(update.message.chatId).setText("Игра началась\nу бегунов есть 3 хода чтоб отбежать от старта!\nЛовец - ${game.hunter.name}"))
                        for (chatId in getMutableListUserId(game)) {
                            playersInGame[chatId.toInt()] = game
                            execute(SendMessage().setChatId(chatId).setText("Локация:\nГримридж\nДом на берегу моря"))
                            if (game.hunter.userId.toLong() != chatId)
                                execute(SendMessage().setChatId(chatId).setText("Ваши действия").setReplyMarkup(getActionButtons()))
                            else
                                execute(SendMessage().setChatId(chatId).setText("Вы ловец\nваша задача выловить всех сучар"))
                        }
                    }
                }
                else if(findGame.players.count() == 1 && findGame.hunter.userId == 0)
                    execute(SendMessage().setChatId(update.message.chatId).setText("Недостаточно игроков"))
            }
            //test
            else if (update.message.text == "/test") {
            }
        }
        else if(update.hasCallbackQuery()){
            println("ChatID" + update.callbackQuery.from + "\nTime: " + getCurrentTime() + "\nMsgText: " + update.callbackQuery.data + "\n")

            if(update.callbackQuery.data == "show_maps"){
                execute(SendMessage().setChatId(update.callbackQuery.from.id.toLong()).setText("Cхемы карт"))
                execute(SendPhoto().setChatId(update.callbackQuery.from.id.toLong()).setPhoto(listMap()[0]))
                execute(SendPhoto().setChatId(update.callbackQuery.from.id.toLong()).setPhoto(listMap()[1]))
                execute(SendPhoto().setChatId(update.callbackQuery.from.id.toLong()).setPhoto(listMap()[2]))
            }
            else if (update.callbackQuery.data == "move_menu"){
                execute(DeleteMessage().setChatId(update.callbackQuery.from.id.toLong()).setMessageId(update.callbackQuery.message.messageId))
                if(update.callbackQuery.from.id in playersInGame) {
                    val (index, role) = playersInGame[update.callbackQuery.from.id]!!.getNumberPlayerAndRole(update.callbackQuery.from.id)
                    execute(SendMessage().setChatId(update.callbackQuery.from.id.toLong()).setText("доступные локации").setReplyMarkup(playersInGame[update.callbackQuery.from.id]?.let { getDirectionsButtons(role, index, it) }))
                }
            }
            else if(update.callbackQuery.data == "action_menu"){
                execute(DeleteMessage().setChatId(update.callbackQuery.from.id.toLong()).setMessageId(update.callbackQuery.message.messageId))
                if(update.callbackQuery.from.id in playersInGame) {
                    if(playersInGame[update.callbackQuery.from.id]!!.hunter.userId != update.callbackQuery.from.id)
                        execute(SendMessage().setChatId(update.callbackQuery.from.id.toLong()).setText("Ваши действия").setReplyMarkup(getActionButtons()))
                    else
                        execute(SendMessage().setChatId(update.callbackQuery.from.id.toLong()).setText("Ваши действия").setReplyMarkup(getActionButtonsForHunter()))
                }
            }
            else if ("move_to" in update.callbackQuery.data){
                var namePlace = update.callbackQuery.data.replace(Regex("""[move_to\(\)]"""),"")
                val (index, role) = playersInGame[update.callbackQuery.from.id]!!.getNumberPlayerAndRole(update.callbackQuery.from.id)
                playersInGame[update.callbackQuery.from.id]?.let { moveToPlace(role,index, it,namePlace) }
                execute(DeleteMessage().setChatId(update.callbackQuery.from.id.toLong()).setMessageId(update.callbackQuery.message.messageId))

                resetAction()
            }
            else if(update.callbackQuery.data == "catch_menu") {
                execute(DeleteMessage().setChatId(update.callbackQuery.from.id.toLong()).setMessageId(update.callbackQuery.message.messageId))
                if (update.callbackQuery.from.id in playersInGame) {
                    if (playersInGame[update.callbackQuery.from.id]!!.hunter.userId == update.callbackQuery.from.id) {
                        execute(SendMessage().setChatId(update.callbackQuery.from.id.toLong()).setText("Поймать").setReplyMarkup(playersInGame[update.callbackQuery.from.id]?.let { getCatchMenu(it) }))
                    }
                }
            }
            else if ("caught" in update.callbackQuery.data){
                var idPlayer = update.callbackQuery.data.replace(Regex("""[caught\(\)]"""),"")
                playersInGame[update.callbackQuery.from.id]?.let { tryCatchPlayer(it,idPlayer.toInt()) }
                playersInGame[update.callbackQuery.from.id]!!.passingList[playersInGame[update.callbackQuery.from.id]!!.players.count()] = true
                execute(DeleteMessage().setChatId(update.callbackQuery.from.id.toLong()).setMessageId(update.callbackQuery.message.messageId))

                resetAction()
            }
            else if (update.callbackQuery.data == "stand_still")
            {
                val (index, role) = playersInGame[update.callbackQuery.from.id]!!.getNumberPlayerAndRole(update.callbackQuery.from.id)
                playersInGame[update.callbackQuery.from.id]?.let { standStill(role,index, it) }
                execute(DeleteMessage().setChatId(update.callbackQuery.from.id.toLong()).setMessageId(update.callbackQuery.message.messageId))

                resetAction()
            }
            else if (update.callbackQuery.data == "interactions_menu")
            {
                execute(DeleteMessage().setChatId(update.callbackQuery.from.id.toLong()).setMessageId(update.callbackQuery.message.messageId))
                if(update.callbackQuery.from.id in playersInGame) {
                    val (index, role) = playersInGame[update.callbackQuery.from.id]!!.getNumberPlayerAndRole(update.callbackQuery.from.id)
                    execute(SendMessage().setChatId(update.callbackQuery.from.id.toLong()).setText("Выберите предмет").setReplyMarkup(playersInGame[update.callbackQuery.from.id]?.let{ getInteractionButtonsMenu(role, index, it)}))
                }
            }
            else if ("interact_with" in update.callbackQuery.data){
                execute(DeleteMessage().setChatId(update.callbackQuery.from.id.toLong()).setMessageId(update.callbackQuery.message.messageId))
                var indexOfItem = update.callbackQuery.data.replace(Regex("""[interact_with\(\)]"""),"")
                if (update.callbackQuery.from.id in playersInGame) {
                    val (index, role) = playersInGame[update.callbackQuery.from.id]!!.getNumberPlayerAndRole(update.callbackQuery.from.id)
                    execute(SendMessage().setChatId(update.callbackQuery.from.id.toLong()).setText("Действие").setReplyMarkup(playersInGame[update.callbackQuery.from.id]?.let { getActionForItemButtons(role, index, it, indexOfItem.toInt()) }))
                }
            }
            else if ("interact_via" in update.callbackQuery.data){
                execute(DeleteMessage().setChatId(update.callbackQuery.from.id.toLong()).setMessageId(update.callbackQuery.message.messageId))
                var indexes = update.callbackQuery.data.replace(Regex("""[interact_via\(\)button\(\)]"""),"")
                if (update.callbackQuery.from.id in playersInGame) {
                    val (index, role) = playersInGame[update.callbackQuery.from.id]!!.getNumberPlayerAndRole(update.callbackQuery.from.id)
                    playersInGame[update.callbackQuery.from.id]?.let { toInteract(role, index, it, indexes) }
                }

                resetAction()
            }
            else if ("leave_hitch" == update.callbackQuery.data){
                execute(DeleteMessage().setChatId(update.callbackQuery.from.id.toLong()).setMessageId(update.callbackQuery.message.messageId))
                if (update.callbackQuery.from.id in playersInGame) {
                    val (index, role) = playersInGame[update.callbackQuery.from.id]!!.getNumberPlayerAndRole(update.callbackQuery.from.id)
                    playersInGame[update.callbackQuery.from.id]?.let { leaveFromHitch(it, index) }
                }

                resetAction()
            }
        }
    }
}
private fun getCurrentTime(): String? {
    val currentDate = Date()
    val dateFormat: DateFormat = SimpleDateFormat("EEE, d MMM yyyy, HH:mm", Locale.getDefault())
    return dateFormat.format(currentDate)
}
