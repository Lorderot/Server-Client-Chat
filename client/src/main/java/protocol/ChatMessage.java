package protocol;

public class ChatMessage {
    private String message;
    private AccessType type;
    private String receiver;
    private String sender;
    private String time;

    public ChatMessage() {
    }

    public ChatMessage(String message, AccessType type, String receiver,
                       String sender, String time) {
        this.message = message;
        this.type = type;
        this.receiver = receiver;
        this.sender = sender;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AccessType getType() {
        return type;
    }

    public void setType(AccessType type) {
        this.type = type;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
