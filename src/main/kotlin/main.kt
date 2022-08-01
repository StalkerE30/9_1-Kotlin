import java.time.LocalDateTime
private var messages = emptyArray<Messages>()
private var lastIdMess: Int = 0
private var chats = emptyArray<Chats>()
private var lastIdChat: Int = 0


fun main() {

}

data class Chats(
    val id: Int = 0, //идентификатор чата
    val ownerId: Int = 0, //идентификатор инициализатора чата
    val idOpponent: Int = 0, // идентификатор того, кому сообщение
    val date: LocalDateTime? = LocalDateTime.now(), //дата создания чата
    var isDeleted: Boolean = false, //признак удаления чата
    var countMessages: Int = 0 //количество сообщений

)

data class Messages(
    val id: Int = 0, //идентификтор сообщения
    val idUser: Int = 0, //идентификатор пользователя, оставившего сообщение
    val idChat: Int = 0, // идентификтор чата
    val text: String = "", // текст сообщения
    val date: LocalDateTime? = LocalDateTime.now(), // дата сообщения
    var isDeleted: Boolean = false, //признак удаления сообщения
    var isRead: Boolean = false //признак прочитанного сообщения. Читать сообщение может только Opponent
)

class ChatService(){
    //private var chats = emptyArray<Chats>()
    //private var lastIdChat: Int = 0

    fun getUnreadChatsCount(userId: Int): Int { //получить число не прочитанных чатов для пользователя с userId
        val chatsList = chats.filter { (it.ownerId == userId || it.idOpponent == userId) && it.countMessages>0 }
        var chatCount = 0
        for((index, chat) in chatsList.withIndex()) {
           if (MessagesService().getUnreadMessagesCount(chat.id,userId) > 0) {
               chatCount +=1
           }
        }
        return chatCount
    }

    fun getChats(userId: Int): List<Chats>{ // получить список чатов с сообщениями
        return chats.filter { (it.idOpponent == userId || it.ownerId == userId) && it.countMessages > 0 && !it.isDeleted }
    }

    fun getChat(idChat: Int): Chats { // получить чат по id
        val find: Chats? = chats.find { it.id == idChat }
        return find ?: throw EntityNotFoundException("Не найден чат с id: $idChat")
    }

    fun addChat(ownerId:Int, idOpponent: Int):Int{ //добавить чат
        lastIdChat += 1
        chats += Chats(id=lastIdChat,ownerId = ownerId, idOpponent = idOpponent)
        return lastIdChat
    }

    fun addMessageInChat(idChat:Int){ //добавить сообщение в чат
        val currChat:Chats? = chats.find { it.id == idChat}
        if (currChat != null) {
            currChat.countMessages +=1
        }
    }

    fun deleteMessageInChat(idChat:Int){ // удалить сообщение в чате
        val currChat:Chats? = chats.find { it.id == idChat}
        if (currChat != null) {
            currChat.countMessages -=1
        }

    }

    fun deleteChat(idChat: Int, ownerId: Int){ //удалить чат
        val currChat = getChat(idChat)
        if (currChat.ownerId == ownerId) {
            if (!currChat.isDeleted) {
                currChat.isDeleted = true
                val mess = MessagesService().getMessagesOnChat(idChat)
                    for((index,message) in (mess.withIndex())){
                        message.isDeleted = true
                    }
            } else {
                println("Чат с id $idChat уже удален")
            }
        } else {
            println("Нельзя удалять чужие чаты!")
        }
    }

}

class MessagesService(){
    //private var messages = emptyArray<Messages>()
    //private var lastIdMess: Int = 0
    //private var chatService = ChatService()

    fun getUnreadMessagesCount(idChat: Int, userId: Int): Int { //получить число непрочитанных сообщений внутри чата для ползователя c userId
        return messages.filter { it.idUser!=userId && it.idChat == idChat && !it.isRead && !it.isDeleted}.size
    }

    fun readMessage(idMessage: Int, userId: Int){ //чтение сообщения пользователем с userId
        //читать будем только чужие сообщения, но в своих чатах
        val currMessage: Messages? = messages.find {it.idUser!=userId && it.id == idMessage && !it.isRead && !it.isDeleted}
        if (currMessage != null) {
            val chat: Chats = ChatService().getChat(idChat = currMessage.idChat)
            //if (chat != null) {
                if (userId == chat.ownerId || userId == chat.idOpponent) {
                    currMessage.isRead = true
                } else {
                    println("Не хорошо читать сообщения из чужих чатов!")
                }
            //}
        } else {
            println("Сообщение с id $idMessage не найдено")
        }
    }

    fun getListMessages(idChat: Int,userId: Int, fromIdMessage: Int, countMessages: Int, readerId: Int): List<Messages> { //получить и прочитать список сообщений
        var count = countMessages
        if (fromIdMessage + countMessages > lastIdMess) {
            count = fromIdMessage + countMessages - lastIdMess
        }
            val listMessages =
                messages.filter { it.idChat == idChat && it.id >= fromIdMessage && it.id <= fromIdMessage + count - 1 }
            for ((index, message) in listMessages.withIndex()) {
                readMessage(message.id,userId) //с точки зрения скорости работы наверное проще было тут помечать прочитанность
            }
            return listMessages

    }

    fun addMessage(idChat: Int,senderId:Int, idOpponent: Int, text: String):Int {//добавить сообщение
        val currMessage:Messages
        if (idChat == 0) {//чат еще не создан
            val chatId = ChatService().addChat(ownerId = senderId, idOpponent = idOpponent)
            lastIdMess += 1
            currMessage = Messages(idUser = senderId, id = lastIdMess, idChat = chatId, text = text)
            messages += currMessage
            ChatService().addMessageInChat(chatId)
            return lastIdMess
        } else { //нужно проверить куда поставить ownerId и idOpponent
            val currChat = ChatService().getChat(idChat)
            lastIdMess += 1
            currMessage = when (senderId) {
                currChat.ownerId -> Messages(idUser = senderId, id = lastIdMess, idChat = idChat, text = text)
                else -> Messages(idUser = idOpponent, id = lastIdMess, idChat = idChat, text = text)
                }
            messages += currMessage
            ChatService().addMessageInChat(idChat)
            return lastIdMess
        }
    }
    fun getMessagesOnChat(idChat: Int): List<Messages> { // получить сообщения в чате
        return messages.filter { it.idChat == idChat }
    }

    fun deleteMassage(userId: Int, idMessage: Int){ //удалять сообщения можно только из тех чатов, которые сам создавал
        val currMessage: Messages? = messages.find {it.idUser ==userId && it.id == idMessage && !it.isDeleted}
        if (currMessage != null) {
            val chat: Chats = ChatService().getChat(currMessage.idChat)
            //if (chat != null) {
                if (userId == chat.ownerId || userId == chat.idOpponent) {
                    currMessage.isDeleted = true
                    ChatService().deleteMessageInChat(currMessage.idChat)
                } else {
                    println("Удалять сообщения из чужих чатов нельзя!")
                }
            //}
        } else {
            println("Сообщение с id $idMessage не найдено")
        }

    }
    fun getMessage(idMess:Int):Messages{
        val find: Messages? = messages.find { it.id == idMess }
        return find ?: throw EntityNotFoundException("Не найдено сообщение с id: $idMess")

    }
}

class EntityNotFoundException(massage:String): RuntimeException(massage)