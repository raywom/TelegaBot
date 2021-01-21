import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hibernate.query.Query
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.io.File
import kotlin.random.Random

fun getDataPlayer(_path : String) : MutableList<Player>
{
    val mapper = jacksonObjectMapper()
    val file = File(_path)
    if (file.exists())
    {
        return mapper.readValue(file.readText())
    }
    return mutableListOf()
}

fun addPlayer(_players: MutableList<Player>, _userId: Int) : Boolean
{
    for(player in _players)
    {
        if(player.userId == _userId)
            return false
    }
    return true
}
fun saveHunterData(_player: Player, _players: MutableList<Player>){
    val mapper = jacksonObjectMapper()
    val file = File("dataPlayers.txt")
    ++_player.countOfWinsAsHunter
    for(count in _players)
    {
        ++count.countOfGames
    }
    file.writeText(mapper.writeValueAsString(_players))
}
fun saveDataPlayers(_path: String, _players: MutableList<Player>)
{
    val mapper = jacksonObjectMapper()
    val file = File(_path)
    file.writeText(mapper.writeValueAsString(_players))

}

fun createGame(_games : MutableList<Game>, _chatId : Long) : Boolean
{
    for(game in _games){
     if(game.chatId == _chatId)
         return false
    }
    _games.add(Game(_chatId))
    return true
}

fun startGame(_games: MutableList<Game>, _chatId: Long) : Boolean
{
    for(game in _games){
        if(game.chatId == _chatId){
            if (game.phase == GamePhase.WAITING){
                game.map.startPoint = createMap()
                game.phase = GamePhase.STARTED
                distributionPlayers(game)
                return true
            }
            return false // игра уже идет
        }
    }
    return false // игра не создана
}

//unfinished
fun getActionButtons() : InlineKeyboardMarkup
{
    val keyboardButtonsRowFirst : MutableList<InlineKeyboardButton> = mutableListOf()
    val keyboardButtonsRowSecond : MutableList<InlineKeyboardButton> = mutableListOf()

    val rowList: MutableList<List<InlineKeyboardButton>> = mutableListOf()
    keyboardButtonsRowFirst.add(InlineKeyboardButton().setText("Перемещение").setCallbackData("move_menu"))
    keyboardButtonsRowFirst.add(InlineKeyboardButton().setText("Взаимодействия").setCallbackData("interactions_menu"))
    keyboardButtonsRowSecond.add(InlineKeyboardButton().setText("Схемы карт").setCallbackData("show_maps"))
    keyboardButtonsRowSecond.add(InlineKeyboardButton().setText("Пропустить ход").setCallbackData("stand_still"))

    rowList.add(keyboardButtonsRowFirst)
    rowList.add(keyboardButtonsRowSecond)
    return InlineKeyboardMarkup().setKeyboard(rowList)
}

fun getActionButtonInHitch() : InlineKeyboardMarkup
{
    val keyboardButtonsRowFirst : MutableList<InlineKeyboardButton> = mutableListOf()
    val keyboardButtonsRowSecond : MutableList<InlineKeyboardButton> = mutableListOf()

    val rowList: MutableList<List<InlineKeyboardButton>> = mutableListOf()
    keyboardButtonsRowFirst.add(InlineKeyboardButton().setText("Выйти").setCallbackData("leave_hitch"))
    keyboardButtonsRowFirst.add(InlineKeyboardButton().setText("Обыскать").setCallbackData("loot"))
    keyboardButtonsRowSecond.add(InlineKeyboardButton().setText("Пропустить ход").setCallbackData("stand_still"))

    rowList.add(keyboardButtonsRowFirst)
    rowList.add(keyboardButtonsRowSecond)
    return InlineKeyboardMarkup().setKeyboard(rowList)
}

fun getCatchMenu(_game: Game) : InlineKeyboardMarkup
{
    val rowList: MutableList<List<InlineKeyboardButton>> = mutableListOf()
    for(index in 0 until _game.map.hunterLocation.players.count()){
        println(_game.map.hunterLocation.players[index].name)
        if(_game.map.hunterLocation.players[index] != _game.hunter) {
            val keyboardButtonsRow: MutableList<InlineKeyboardButton> = mutableListOf()
            keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.hunterLocation.players[index].name).setCallbackData("caught(${_game.map.hunterLocation.players[index].userId})"))
            rowList.add(keyboardButtonsRow)
        }
    }
    val keyboardButtonsRow : MutableList<InlineKeyboardButton> = mutableListOf()
    keyboardButtonsRow.add(InlineKeyboardButton().setText("Назад к выбору действий").setCallbackData("action_menu"))
    rowList.add(keyboardButtonsRow)
    return InlineKeyboardMarkup().setKeyboard(rowList)
}

fun tryCatchPlayer(_game: Game, _userId: Int){
    _game.hunterHistory.setHistoryCatch(_userId)
}

//unfinished
fun getActionButtonsForHunter() : InlineKeyboardMarkup
{
    val keyboardButtonsRowFirst : MutableList<InlineKeyboardButton> = mutableListOf()
    val keyboardButtonsRowSecond : MutableList<InlineKeyboardButton> = mutableListOf()
    val keyboardButtonsRowThird : MutableList<InlineKeyboardButton> = mutableListOf()

    val rowList: MutableList<List<InlineKeyboardButton>> = mutableListOf()
    keyboardButtonsRowFirst.add(InlineKeyboardButton().setText("Перемещение").setCallbackData("move_menu"))
    keyboardButtonsRowFirst.add(InlineKeyboardButton().setText("Взаимодействия").setCallbackData("interactions_menu"))
    keyboardButtonsRowSecond.add(InlineKeyboardButton().setText("Поймать").setCallbackData("catch_menu"))
    keyboardButtonsRowSecond.add(InlineKeyboardButton().setText("Схемы карт").setCallbackData("show_maps"))
    keyboardButtonsRowThird.add(InlineKeyboardButton().setText("Пропустить ход").setCallbackData("stand_still"))

    rowList.add(keyboardButtonsRowFirst)
    rowList.add(keyboardButtonsRowSecond)
    rowList.add(keyboardButtonsRowThird)
    return InlineKeyboardMarkup().setKeyboard(rowList)
}

fun getDirectionsButtons(_role : String, _index : Int, _game: Game) : InlineKeyboardMarkup
{
    val rowList: MutableList<List<InlineKeyboardButton>> = mutableListOf()
    if (_role == "hunter"){
        for (index in 0 until _game.map.hunterLocation.directions.count() step 2){
            val keyboardButtonsRow : MutableList<InlineKeyboardButton> = mutableListOf()
            keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.hunterLocation.directions[index].name).setCallbackData("move_to(${_game.map.hunterLocation.directions[index].name})"))
            if (index + 1 < _game.map.hunterLocation.directions.count())
                keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.hunterLocation.directions[index+1].name).setCallbackData("move_to(${_game.map.hunterLocation.directions[index+1].name})"))
            rowList.add(keyboardButtonsRow)
        }
    }
    else if(_role == "runner"){
        for (index in 0 until _game.map.playerLocation[_index].directions.count() step 2){
            val keyboardButtonsRow : MutableList<InlineKeyboardButton> = mutableListOf()
            keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.playerLocation[_index].directions[index].name).setCallbackData("move_to(${_game.map.playerLocation[_index].directions[index].name})"))
            if (index + 1 < _game.map.playerLocation[_index].directions.count())
                keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.playerLocation[_index].directions[index+1].name).setCallbackData("move_to(${_game.map.playerLocation[_index].directions[index+1].name})"))
            rowList.add(keyboardButtonsRow)
        }
    }
    val keyboardButtonsRow : MutableList<InlineKeyboardButton> = mutableListOf()
    keyboardButtonsRow.add(InlineKeyboardButton().setText("Назад к выбору действий").setCallbackData("action_menu"))
    rowList.add(keyboardButtonsRow)
    return InlineKeyboardMarkup().setKeyboard(rowList)
}

fun getInteractionButtonsMenu(_role: String, _index: Int,_game: Game):InlineKeyboardMarkup{
    val rowList: MutableList<List<InlineKeyboardButton>> = mutableListOf()
    println("getInteractionButtonsMenu")
    if(_role == "hunter"){
        for (index in 0 until _game.map.hunterLocation.objects.count() step 2){
            val keyboardButtonsRow: MutableList<InlineKeyboardButton> = mutableListOf()
            keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.hunterLocation.objects[index].name).setCallbackData("interact_with(${index})"))
            if (index + 1 < _game.map.hunterLocation.objects.count()) {
                keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.hunterLocation.objects[index + 1].name).setCallbackData("interact_with(${index + 1})"))
            }
            rowList.add(keyboardButtonsRow)
        }
    }
    else if(_role == "runner"){
        for (index in 0 until _game.map.playerLocation[_index].objects.count() step 2){
            println(_game.map.playerLocation[_index].objects[index].name)
            val keyboardButtonsRow : MutableList<InlineKeyboardButton> = mutableListOf()
            keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.playerLocation[_index].objects[index].name).setCallbackData("interact_with(${index})"))
            println("${index + 1}     ${_game.map.playerLocation[_index].objects.count()}")
            if (index + 1 < _game.map.playerLocation[_index].objects.count())
                keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.playerLocation[_index].objects[index+1].name).setCallbackData("interact_with(${index + 1})"))
            rowList.add(keyboardButtonsRow)
        }
    }
    val keyboardButtonsRow : MutableList<InlineKeyboardButton> = mutableListOf()
    keyboardButtonsRow.add(InlineKeyboardButton().setText("Назад к выбору действий").setCallbackData("action_menu"))
    rowList.add(keyboardButtonsRow)
    return InlineKeyboardMarkup().setKeyboard(rowList)
}

fun getActionForItemButtons(_role: String, _index: Int,_game: Game, _indexOfItem : Int) : InlineKeyboardMarkup
{
    val rowList: MutableList<List<InlineKeyboardButton>> = mutableListOf()
    if(_role == "hunter"){
        for (index in 0 until _game.map.hunterLocation.objects[_indexOfItem].actionButtonsForHunter.count() step 2){
            val keyboardButtonsRow: MutableList<InlineKeyboardButton> = mutableListOf()
            keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.hunterLocation.objects[_indexOfItem].actionButtonsForHunter[index]).setCallbackData("interact_via($_indexOfItem)button($index)"))
            if (index + 1 < _game.map.hunterLocation.objects[_indexOfItem].actionButtonsForHunter.count()) {
                keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.hunterLocation.objects[_indexOfItem].actionButtonsForHunter[index + 1]).setCallbackData("interact_via($_indexOfItem)button($index)"))
            }
            rowList.add(keyboardButtonsRow)
        }
    }
    else if(_role == "runner"){
        for (index in 0 until _game.map.playerLocation[_index].objects[_indexOfItem].actionButtonsForPlayers.count() step 2){
            val keyboardButtonsRow : MutableList<InlineKeyboardButton> = mutableListOf()
            keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.playerLocation[_index].objects[_indexOfItem].actionButtonsForPlayers[index]).setCallbackData("interact_via($_indexOfItem)button($index)"))
            if (index + 1 < _game.map.playerLocation[_index].objects[_indexOfItem].actionButtonsForPlayers.count())
                keyboardButtonsRow.add(InlineKeyboardButton().setText(_game.map.playerLocation[_index].objects[_indexOfItem].actionButtonsForPlayers[index + 1]).setCallbackData("interact_via($_indexOfItem)button($index)"))
            rowList.add(keyboardButtonsRow)
        }
    }
    val keyboardButtonsRow : MutableList<InlineKeyboardButton> = mutableListOf()
    keyboardButtonsRow.add(InlineKeyboardButton().setText("Назад к выбору действий").setCallbackData("action_menu"))
    rowList.add(keyboardButtonsRow)
    return InlineKeyboardMarkup().setKeyboard(rowList)
}

fun standStill(_role: String, _index: Int, _game: Game)
{
    if (_role == "hunter"){
        if(!_game.passingList[_game.players.count()]) {
            _game.hunterHistory.setHistoryStand(_game.map.hunterLocation)
            _game.passingList[_game.players.count()] = true
        }
    }
    else if(_role == "runner"){
        if(!_game.passingList[_index]) {
            _game.playersHistory[_index].setHistoryStand(_game.map.playerLocation[_index])
            _game.passingList[_index] = true
        }
    }
}

fun toInteract(_role: String,_index: Int,_game: Game, _indexes : String)
{
    var indexOfObject = _indexes[0].toString().toInt()
    var indexOfButton = _indexes[1].toString().toInt()
    if(_role == "runner")
        _game.map.playerLocation[_index].objects[indexOfObject].actions[indexOfButton].interact(_role,_index,_game,_game.map.playerLocation[_index].objects[indexOfObject])
    else
        _game.map.hunterLocation.objects[indexOfObject].actions[_game.map.hunterLocation.objects[indexOfObject].actionButtonsForPlayers.count() + indexOfButton].interact(_role,_index,_game,_game.map.hunterLocation.objects[indexOfObject])
}

fun moveToPlace(_role: String, _index: Int, _game: Game, _namePlace : String)
{
    if (_role == "hunter"){
        if(!_game.passingList[_game.players.count()]) {
            for (direction in _game.map.hunterLocation.directions) {
                if (direction.name == _namePlace) {
                    _game.hunterHistory.setHistoryMove(direction, _game.map.hunterLocation)
                    _game.passingList[_game.players.count()] = true
                }
            }
        }
    }
    else if(_role == "runner"){
        if(!_game.passingList[_index]) {
            if (!_game.players[_index].isHidden) {
                for (direction in _game.map.playerLocation[_index].directions) {
                    if (direction.name == _namePlace) {
                        _game.playersHistory[_index].setHistoryMove(direction, _game.map.playerLocation[_index])
                        _game.passingList[_index] = true
                    }
                }
            }
        }
    }
}

fun getMutableListUserId(_game: Game) : MutableList<Long>
{
    var listChatId = mutableListOf<Long>()
    for(player in _game.players){
        listChatId.add(player.userId.toLong())
    }
    if(_game.hunter.userId != 0)
        listChatId.add(_game.hunter.userId.toLong())
    return listChatId
}

fun findGame(_games: MutableList<Game>, _chatId: Long) : Game
{
    for (game in _games)
        if (game.chatId == _chatId)
            return game
    return Game(0)
}

//unfinished
fun distributionPlayers(_game : Game)
{
    var index = Random.nextInt(0, _game.players.count())
    _game.hunter = _game.players[index]
    _game.players.removeAt(index)
    _game.countOfPlayers = _game.players.count().toByte()
    _game.playersHistory.removeAt(index)
    _game.map.hunterLocation = _game.map.startPoint
    _game.map.startPoint.players.add(_game.hunter)
    _game.passingList[_game.players.count()] = true
    for (player in _game.players){
        _game.map.playerLocation.add(_game.map.startPoint)
        _game.map.playerHitch.add(Hitch())
        _game.map.startPoint.players.add(player)
    }
}

fun joinGame(_games: MutableList<Game>, _players : MutableList<Player>, _chatId: Long, _userId : Int) : Player
{
    for(game in _games){
        if(game.chatId == _chatId && game.phase == GamePhase.WAITING){
            for (player in _players){
                if(player.userId == _userId){
                    if(!findPlayerInGame(game, player.userId)){
                        game.players.add(player)
                        game.passingList.add(false)
                        game.playersHistory.add(History())
                    }
                    return player
                }
            }
        }
    }
    return Player()
}

fun leaveFromHitch(_game : Game, _index : Int){
    _game.playersHistory[_index].setHistoryLeaveHide()
    _game.passingList[_index] = true
}

fun findPlayerInGame(_game: Game, _userId: Int) : Boolean
{
    for (player in _game.players){
        if(player.userId == _userId)
            return true
    }
    return false
}

fun findPlayerById(_userId: Int, _players: MutableList<Player>) : Player
{
    for (player in _players){
        if(player.userId == _userId)
            return player
    }
    return Player()
}