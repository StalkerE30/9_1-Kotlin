import org.junit.Test

import org.junit.Assert.*

class ChatServiceTest {

    @Test
    fun getUnreadChatsCount() {
        val chatService = ChatService()
        val messagesService =MessagesService()
        val idMess1 = messagesService.addMessage(0,1,2,"Привет! Как дела?")
        val idChat = messagesService.getMessage(idMess1).idChat
        val idMess2 = messagesService.addMessage(idChat,1,2,"Ау!!!")
        val idMess3 = messagesService.addMessage(0,3,2,"Привет! Как дела?")
        val result = chatService.getUnreadChatsCount(2)
        assertEquals(2, result)
    }

    @Test
    fun getChats() {
    }

    @Test
    fun getChat() {
    }

    @Test
    fun addChat() {
    }

    @Test
    fun addMessageInChat() {
    }

    @Test
    fun deleteMessageInChat() {
    }

    @Test
    fun deleteChat() {
    }
}