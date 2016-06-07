package protocol;

public class ServerMessage {
    private String nickname;
    private ClientStatus status;
    private String time;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }
}
