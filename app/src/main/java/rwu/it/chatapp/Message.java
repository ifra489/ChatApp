package rwu.it.chatapp;

public class Message {

    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;
    private boolean seen;
    private boolean delivered;
    public Message() {
        // Required for Firebase
    }

    public Message(String senderId, String receiverId, String message, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isSeen() { return seen; }
    public boolean isDelivered() { return delivered; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public void setMessage(String message) { this.message = message; }
    public void setSeen(boolean seen) { this.seen = seen; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
