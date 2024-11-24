package vn.edu.usth.naturevoice_socket;
import java.util.ArrayList;
import java.util.List;

public class ChatHistory {
    private List<String> userMessages; // Danh sách tin nhắn của user
    private List<String> botMessages;  // Danh sách tin nhắn của bot

    public ChatHistory() {
        this.userMessages = new ArrayList<>();
        this.botMessages = new ArrayList<>();
    }

    // Thêm tin nhắn của user
    public void addUserMessage(String message) {
        userMessages.add(message);
    }

    // Thêm tin nhắn của bot
    public void addBotMessage(String message) {
        botMessages.add(message);
    }

    // Lấy danh sách tin nhắn của user
    public List<String> getUserMessages() {
        return userMessages;
    }

    // Lấy danh sách tin nhắn của bot
    public List<String> getBotMessages() {
        return botMessages;
    }
}
