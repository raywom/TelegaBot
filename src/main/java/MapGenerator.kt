import javax.swing.Action

fun createMap() : Place
{
    var hitches = createHitches()
    var usefulItem = createUsefulItem()
    var place1 = Place("Коридор№1.1", "В этой части коридора есть развилка, налево и направ, также есть проход на кухню(слево), гостиную(справа) и задний двор(прямо)")
    var place2 = Place("Кухня", "Это тупик, проход есть только обратно в коридор")
    var place3 = Place("Гостиная", "В гостиной есть два прохода, коридор(откуда ты зашел) и другая часть коридора")
    var place4 = Place("Коридор№1.2", "В этой части коридора есть развилка, направо или прямо по коридору, также проход в гостиную, комноту для секса, лестница на второй этаж, и между развилкой есть проход задний двор")
    var place5 = Place("Коридор№1.3", "В этой части коридора есть развилка, налево или прямо по коридору, также есть лифт, и межу развилкой проход на задний двор")
    var place7 = Place("Лифт", "В лифте есть кнопки:\n1)1 Этаж \n и нахуй он только нужен")
    var place8 = Place("Комната", "Это тупик, проход есть только обратно в коридор")
    var place9 = Place("Двор", "Во дворе есть нихуевый большой бассейн, также есть лестница в подвал и проход в дом")
    var place10 = Place("лестница", "Вы стоите на лестнице между первым и вторым этажем")
    var place11 = Place("Пустота", "Тут ничего нет, даже прохода назад(По ходу вы прошли дальше чем нужно было)")


    place1.directions.add(place2)
    place1.directions.add(place3)
    place1.directions.add(place4)
    place1.directions.add(place5)
    place1.directions.add(place9)
    place1.objects.add(hitches[1])
    place1.objects.add(usefulItem[6])

    place2.directions.add(place1)
    place2.objects.add(hitches[4])
    place2.objects.add(hitches[7])
    place2.objects.add(usefulItem[0])
    place2.objects.add(usefulItem[1])

    place3.directions.add(place1)
    place3.directions.add(place4)
    place3.objects.add(hitches[8])
    place3.objects.add(hitches[6])
    place3.objects.add(hitches[5])
    place3.objects.add(usefulItem[2])
    place3.objects.add(usefulItem[3])

    place4.directions.add(place1)
    place4.objects.add(hitches[9])
    place4.directions.add(place5)
    place4.directions.add(place9)
    place4.directions.add(place3)
    place4.directions.add(place8)
    place4.directions.add(place10)

    place5.directions.add(place1)
    place5.directions.add(place4)
    place5.directions.add(place9)
    place5.objects.add(hitches[10])
    place5.directions.add(place7)

    place7.directions.add(place5)

    place8.directions.add(place4)
    place8.objects.add(hitches[0])
    place8.objects.add(hitches[3])
    place8.objects.add(usefulItem[4])
    place8.objects.add(usefulItem[5])

    place9.directions.add(place1)
    place9.directions.add(place4)
    place9.directions.add(place5)
    place9.directions.add(place11)
    place9.objects.add(hitches[11])
    place9.objects.add(hitches[12])

    place10.directions.add(place11)
    place10.directions.add(place4)
    place10.objects.add(hitches[2])

    return place1
}
fun createHitches() : MutableList<Hitch>{

    var listOfHitches : MutableList<Hitch> = mutableListOf()
    var hitch1 = Hitch("Шкаф Гримриджа младшего", "Чьи-то обчеркашенные трусы..")
    var hitch2 = Hitch("Деревянный шкаф в прихожей", "Уютненькое местечко")
    var hitch3 = Hitch("Кладовка под лестницей", "Прям как в Гарри Поттере...")
    var hitch4 = Hitch("Высокая кровать Гейбоевых", "Уютненькое местечко")
    var hitch5 = Hitch("Стол с плотной тканью поверх", "Уютненькое местечко")
    var hitch6 = Hitch("Ветхая люстра", "Уютненькое местечко")
    var hitch7 = Hitch("Димаут шторы", "Уютненькое местечко")
    var hitch8 = Hitch("Холодильник", "Уютненькое местечко")
    var hitch9 = Hitch("Большой черный кожаный диван", "Уютненькое местечко")
    var hitch10 = Hitch("Комод из добротного дерева", "Уютненькое местечко")
    var hitch11 = Hitch("Кладовка", "Неплохо живут эти Гримриджи, даже кладовка с люстрой за пару десятков зеленых...")
    var hitch12 = Hitch("Мусорный бак", "Черт, здесь недопитый Jameson...")
    var hitch13 = Hitch("Бассейн", "Надолго меня под водой не хватит...")

    listOfHitches.add(0,hitch1)
    listOfHitches.add(1,hitch2)
    listOfHitches.add(2,hitch3)
    listOfHitches.add(3,hitch4)
    listOfHitches.add(4,hitch5)
    listOfHitches.add(5,hitch6)
    listOfHitches.add(6,hitch7)
    listOfHitches.add(7,hitch8)
    listOfHitches.add(8,hitch9)
    listOfHitches.add(9,hitch10)
    listOfHitches.add(10,hitch11)
    listOfHitches.add(11,hitch12)
    listOfHitches.add(12,hitch13)

    var actionStringForPlayer1 = "Спрятаться"
    var actionStringForPlayer2 = "Залутать"
    var actionStringForHunter1 = "Обыскать"
    for (hitch in listOfHitches){
        hitch.actionButtonsForPlayers.add(actionStringForPlayer1)
        hitch.actions.add(HideAway())
        hitch.actionButtonsForPlayers.add(actionStringForPlayer2)
        hitch.actions.add(Loot())
        hitch.actionButtonsForHunter.add(actionStringForHunter1)
        hitch.actions.add(ToSearch())
    }
    return listOfHitches
}

fun createUsefulItem() : MutableList<UsefulItem>
{
    var listOfUsefulItem = mutableListOf<UsefulItem>()
    //кухня 0 - 1
    var usefulItem1 = UsefulItem("Навесные полки", "")
    var usefulItem2 = UsefulItem("Полки", "")
    //гостиная 2 - 3
    var usefulItem3 = UsefulItem("Тумбочка", "")
    var usefulItem4 = UsefulItem("Маленький столик", "")
    //комната хозяйна 4 - 5
    var usefulItem5 = UsefulItem("Шкафчик", "")
    var usefulItem6 = UsefulItem("Письменный стол", "")
    //коридор1.1 6
    var usefulItem7 = UsefulItem("Дверь наружу", "")

    listOfUsefulItem.add(usefulItem1)
    listOfUsefulItem.add(usefulItem2)
    listOfUsefulItem.add(usefulItem3)
    listOfUsefulItem.add(usefulItem4)
    listOfUsefulItem.add(usefulItem5)
    listOfUsefulItem.add(usefulItem6)

    usefulItem3.items.add(Item("Key"))

    var actionStringForPlayer1 = "Залутать"
    for (usefulItem in listOfUsefulItem){
        usefulItem.actionButtonsForPlayers.add(actionStringForPlayer1)
        usefulItem.actions.add(Loot())
    }

    listOfUsefulItem.add(usefulItem7)
    usefulItem7.actionButtonsForPlayers.add("Открыть дверь")
    usefulItem7.interactionItem.name = "Key"
    usefulItem7.actions.add(ToOpen())

    return listOfUsefulItem
}