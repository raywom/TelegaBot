import kotlin.random.Random

enum class GamePhase
{
    WAITING,
    STARTED
}

enum class TypeEvent
{
    CAUGHT,
    MASCAUGHT,
    ESCAPED
}

class GlobalEvent() {
    var result: String = ""
    var isGlobalEvent: Boolean = false
    var typeEvent = TypeEvent.CAUGHT
    var caughtUserId = 0
    var listCaughtUserId = mutableListOf<Int>()
    var listFugitiveUserId = mutableListOf<Int>()
}


class Game(_chatId : Long)
{
    var numberMoves = 0
    val chatId : Long = _chatId
    var players : MutableList<Player> = mutableListOf()
    var hunter : Player = Player()
    var phase : GamePhase = GamePhase.WAITING
    var map = Map(Place("",""))
    var passingList = mutableListOf<Boolean>()
    var playersHistory : MutableList<History> = mutableListOf()
    var hunterHistory = History()
    var global = GlobalEvent()
    var countOfCaught : Int = 0
    var countOfPlayers : Byte = 0

    fun getPlayerByChatId(_chatId : Int) : Player
    {
        for (player in players){
            if (player.userId == _chatId)
                return player
        }
        return Player()
    }

    fun getNumberPlayerAndRole(_userId: Int) : Pair<Int, String>
    {
        if (hunter.userId == _userId){
            return Pair(0,"hunter")
        }
        else
            for (index in 0..players.count()){
                if (players[index].userId == _userId)
                    return Pair(index, "runner")
            }
        return Pair(-1, "none")
    }

    fun readinessCheck() : Boolean
    {
        for (index in 0 until passingList.count()){
            if (!passingList[index])
                return false
        }
        return true
    }

    private fun eventCheck()
    {
        //game result

        for(index in 0 until playersHistory.count()){
            if (playersHistory[index].action == "move"){
                map.playerLocation[index].players.remove(players[index])
                playersHistory[index].moveTo.players.add(players[index])
                map.playerLocation[index] = playersHistory[index].moveTo
            }
        }

        //for Players
        for (index in 0 until playersHistory.count()){
            var listPlayers = ""
            for (player in map.playerLocation[index].players){
                if(player.userId != players[index].userId)
                    listPlayers += "${player.name} "
            }
            if(listPlayers != "")
                playersHistory[index].result += "Вы видите $listPlayers\n\n"
            else
                playersHistory[index].result += ""
        }
        //for Players

        //for Hunter
        var listPlayers = ""
        for (player in map.hunterLocation.players){
            if(player.userId != hunter.userId)
                if(!player.isHidden)
                    listPlayers += "${player.name} "
        }
        if(listPlayers != "")
            hunterHistory.result += "Вы видите $listPlayers\n\n"
        else
            hunterHistory.result += ""
        var catchText = ""
        if (hunterHistory.action == "catch"){
            for (index in 0 until map.hunterLocation.players.count()){
                if (map.hunterLocation.players[index].userId == hunterHistory.victimUserId){
                    countOfCaught++
                    catchText += "Вы поймали ${map.hunterLocation.players[index].name}"
                    global.isGlobalEvent = true
                    global.result = "${map.hunterLocation.players[index].name} был пойман!!!"
                    global.typeEvent = TypeEvent.CAUGHT
                    global.caughtUserId = map.hunterLocation.players[index].userId
                    deletePlayerByIndex(getIndexPlayer(map.hunterLocation.players[index]))
                }
            }
            if (catchText != ""){
                hunterHistory.result += catchText
            }
            else {
                hunterHistory.result += "Вы не смогли никого поймать\n" +
                        "${players[getIndexPlayer(hunterHistory.victimUserId)].name} убегает в ${map.playerLocation[getIndexPlayer(hunterHistory.victimUserId)].name}"
            }
        }
        else if (hunterHistory.action == "masCatch"){
            for (index in 0 until players.count()){
                for (victimUserId in hunterHistory.victimsUserId) {
                    if (players[index].userId == victimUserId) {
                        countOfCaught++
                        global.isGlobalEvent = true
                        global.result += "${map.hunterLocation.players[index].name} был пойман!!!\n"
                        global.typeEvent = TypeEvent.MASCAUGHT
                        global.listCaughtUserId.add(players[index].userId)
                        deletePlayerByIndex(getIndexPlayer(map.hunterLocation.players[index]))
                    }
                }
            }
        }
        //for Hunter

        //ActionGame
        for(index in 0 until playersHistory.count()){
            if (playersHistory[index].action == "hide"){
                map.playerHitch[index].players.add(players[index])
            }
            else if (playersHistory[index].action == "leaveHitch"){
                map.playerLocation[index].players.add(players[index])
                players[index].isHidden = false
            }
            else if (playersHistory[index].action == "successfulEscape"){
                global.isGlobalEvent = true
                global.result += "Игрок ${players[index].name} сбежал\n"
                global.typeEvent = TypeEvent.ESCAPED
                global.listFugitiveUserId.add(players[index].userId)
                deletePlayerByIndex(index)
            }
        }
        if (hunterHistory.action == "move"){
            map.hunterLocation.players.remove(hunter)
            hunterHistory.moveTo.players.add(hunter)
            map.hunterLocation = hunterHistory.moveTo
            chanceToActivateTheSixthSense()
        }
    }

    private fun chanceToActivateTheSixthSense(){
        var sixthSenseWillActivate = true;
        for (index in 0 until players.count()){
            if(!players[index].isHidden && map.playerLocation[index] == map.hunterLocation)
                sixthSenseWillActivate = false
        }

        if(sixthSenseWillActivate) {
            for (index in 0 until players.count()) {
                if(players[index].isHidden && map.playerLocation[index] == map.hunterLocation)
                {
                    var chanceToFeel = 45;
                    var chance = Random.nextInt(1, 101);
                    if (chance <= chanceToFeel)
                        hunterHistory.result += "Вы чувствуете чье то присутствие\n";
                    else
                        break;
                }
            }
        }
    }

    fun resetTurn()
    {
        numberMoves++
        if (numberMoves >= 3) {
            passingList[players.count()] = false
            if (numberMoves % 2 == 1)
                for (index in 0 until players.count()) {
                    passingList[index] = false
            }
        }
        else {
            for (index in 0 until players.count()) {
                passingList[index] = false
            }
        }
        eventCheck()
        for (history in playersHistory)
        {
            history.action = "stand"
        }
        hunterHistory.action = "stand"
    }

    fun getResultPlayers(_index : Int) : String
    {
        println("in GetResult ${playersHistory[_index].result}")
        return "Вы находитесь в ${map.playerLocation[_index].name}\n${playersHistory[_index].result}"
    }

    fun getResultHunter() : String
    {
        return "Вы находитесь в ${map.hunterLocation.name}\n${hunterHistory.result}"
    }

    private fun getIndexPlayer(_player : Player) : Int
    {
        for (index in 0 until players.count()){
            if (players[index] == _player)
                return index
        }
        return -1
    }
    private fun getIndexPlayer(_userId: Int) : Int
    {
        for (index in 0 until players.count()){
            if (players[index].userId == _userId)
                return index
        }
        return -1
    }

    fun endGameCheck() : Boolean
    {
        if (players.count() > 0)
            return false
        return true
    }

    private fun deletePlayerByIndex(_index: Int)
    {
        players[_index].isPlaying = false
        players.removeAt(_index)
        map.playerLocation.removeAt(_index)
        map.playerHitch.removeAt(_index)
        passingList.removeAt(_index)
        playersHistory.removeAt(_index)
    }

    fun clearResult(){
        for(history in playersHistory){
            history.result = ""
        }
    }
}

class Player(_userId : Int = 0, _name : String = "unknown", _countOfWinsAsHunter : Int = 0, _countOfGames : Int = 0, _countOfWinsAsHider : Int = 0)
{
    var userId = _userId
    var name = _name
    var countOfGames= _countOfGames
    var countOfWinsAsHider = _countOfWinsAsHider
    var countOfWinsAsHunter = _countOfWinsAsHunter
    var items = mutableListOf<Item>()
    var isPlaying = false
    var isHidden = false

    fun returnItemsList() : MutableList<String>
    {
        var listItems = mutableListOf<String>()
        for (item in items){
            listItems.add(item.name)
        }
        return listItems
    }
}

class History()
{
    var action = ""
    var moveTo = Place("","")
    var moveFrom = Place("","")
    var victimUserId = 0
    var victimsUserId = mutableListOf<Int>()
    var result = ""
    var whereHide = Hitch()

    fun setHistoryLoot()
    {
        action = "loot"
    }

    fun setHistoryStand(_standPosition : Place)
    {
        action = "stand"
        moveTo = _standPosition
        moveFrom = _standPosition
    }

    fun setHistoryCatch(_victimUserId : Int)
    {
        action = "catch"
        victimUserId = _victimUserId
    }

    fun setHistoryMasCatch(_listUserId : MutableList<Int>)
    {
        action = "masCatch"
        victimsUserId = _listUserId
    }

    fun setHistoryHide(_gameObject: GameObject)
    {
        action = "hide"
        whereHide = _gameObject as Hitch
    }

    fun setHistoryMove(_moveTo : Place, _moveFrom : Place)
    {
        action = "move"
        moveTo = _moveTo
        moveFrom = _moveFrom
    }

    fun setHistoryLeaveHide(){
        action = "leaveHitch"
    }

    fun setHistoryTryToEscape(_isSuccess : Boolean){
        if (_isSuccess)
            action = "successfulEscape"
        else
            action = "badEscape"
    }
}

class Place(_name: String, _description : String)
{
    var name : String = _name
    var directions : MutableList<Place> = mutableListOf()
    var objects : MutableList<GameObject> = mutableListOf()
    var players : MutableList<Player> = mutableListOf()
    var description : String = _description
}

class Hitch(_name: String = "none", _description : String = "none", _maxNumberOfPlayers : Int = 2) : GameObject
{
    var directions = Place("","")
    var players : MutableList<Player> = mutableListOf()
    var items = mutableListOf<Item>()
    var maxNumberOfPlayers = _maxNumberOfPlayers
    override var actionButtonsForPlayers = mutableListOf<String>()
    override var actionButtonsForHunter = mutableListOf<String>()
    override var name = _name
    override var description : String = _description
    override val actions = mutableListOf<ActionButton>()
}

class UsefulItem(_name: String, _description: String) : GameObject
{
    var items = mutableListOf<Item>()
    var interactionItem = Item()
    override var actionButtonsForPlayers = mutableListOf<String>()
    override var actionButtonsForHunter = mutableListOf<String>()
    override var name = _name
    override var description = _description
    override val actions = mutableListOf<ActionButton>()
}

class Item(_name: String = "nothing"){
    var name = _name
}

class Map(_place : Place)
{
    var startPoint = _place
    var playerLocation = mutableListOf<Place>()
    var playerHitch = mutableListOf<Hitch>()
    var hunterLocation = Place("","")
}

interface GameObject
{
    val name : String
    val description : String
    val actionButtonsForPlayers : MutableList<String>
    val actionButtonsForHunter : MutableList<String>
    val actions : MutableList<ActionButton>
}

interface ActionButton
{
    fun interact(_role : String, _playerIndex : Int, _game : Game, _gameObject : GameObject)
}

class Loot : ActionButton
{
    override fun interact(_role: String, _indexOfPlayer: Int, _game: Game, _gameObject: GameObject) {
        if (_role == "runner") {
            _game.playersHistory[_indexOfPlayer].result = if ((_gameObject as UsefulItem).items.count() == 0)
                "В ${_gameObject.name} ничего нет.\n"
            else {
                var listItems = ""
                for (item in _gameObject.items) {
                    _game.players[_indexOfPlayer].items.add(item)
                    listItems += "${item.name} "
                }
                (_gameObject as UsefulItem).items.clear()
                "Вы взяли $listItems\n"
            }
            _game.playersHistory[_indexOfPlayer].setHistoryLoot()
            _game.passingList[_indexOfPlayer] = true
        }
        else{
            _game.hunterHistory.result = if ((_gameObject as UsefulItem).items.count() == 0)
                "В ${_gameObject.name} ничего нет.\n"
            else {
                var listItems = ""
                for (item in _gameObject.items) {
                    listItems += "${item.name} "
                }
                _gameObject.items.clear()
                "Вы взяли $listItems\n"
            }
            _game.hunterHistory.setHistoryLoot()
            _game.passingList[_game.players.count()] = true
        }
    }
}

class HideAway : ActionButton
{
    override fun interact(_role: String, _indexOfPlayer: Int, _game: Game, _gameObject: GameObject) {
        if (_role == "runner") {
            _game.playersHistory[_indexOfPlayer].result = if ((_gameObject as Hitch).players.count() == _gameObject.maxNumberOfPlayers) {
                var listPlayers = ""
                for (player in _gameObject.players){
                    listPlayers += "${player.name} "
                }
                "Вы пытались спрятатся, но ${_gameObject.name} был занят: $listPlayers\n"
            }
            else {
                _game.players[_indexOfPlayer].isHidden = true
                _game.map.playerHitch[_indexOfPlayer] = _gameObject
                _game.map.playerLocation[_indexOfPlayer].players.remove(_game.players[_indexOfPlayer])
                "Вы спрятались в ${_gameObject.name}\n"
            }
            _game.playersHistory[_indexOfPlayer].setHistoryHide(_gameObject)
            _game.passingList[_indexOfPlayer] = true
        }
    }
}

class ToSearch : ActionButton
{
    override fun interact(_role: String, _indexOfPlayer: Int, _game: Game, _gameObject: GameObject) {
        if (_role == "hunter") {
            _game.hunterHistory.result = if ((_gameObject as Hitch).players.count() == 0) {
                "Вы обыскали ${_gameObject.name}, но нокого не нашли\n"
            }
            else {
                var listPlayers = ""
                var listUserId = mutableListOf<Int>()
                for (player in _gameObject.players){
                    listPlayers += "${player.name} "
                    listUserId.add(player.userId)
                }
                _game.hunterHistory.setHistoryMasCatch(listUserId)
                "Вы нашли и поймали: $listPlayers\n"
            }
            _game.passingList[_game.players.count()] = true
        }
    }
}

class ToOpen : ActionButton
{
    override fun interact(_role: String, _indexOfPlayer: Int, _game: Game, _gameObject: GameObject) {
        if (_role == "runner") {
            _game.playersHistory[_indexOfPlayer].result = if ((_gameObject as UsefulItem).interactionItem.name in _game.players[_indexOfPlayer].returnItemsList()) {
                _game.playersHistory[_indexOfPlayer].setHistoryTryToEscape(true)
                for(index in 0 until _game.players[_indexOfPlayer].items.count()){
                    if (_game.players[_indexOfPlayer].items[index].name == _gameObject.interactionItem.name)
                        _game.players[_indexOfPlayer].items.removeAt(index)
                }
                "Вы открыли ${_gameObject.name}\n"
            }
            else {
                _game.playersHistory[_indexOfPlayer].setHistoryTryToEscape(false)
                "Вы пытались открыть ${_gameObject.name}, но безуспешно\nВам нужен ${_gameObject.interactionItem.name}\n"
            }
            _game.passingList[_indexOfPlayer] = true
        }
    }
}