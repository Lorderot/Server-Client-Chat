package protocols;

public class NicknameTransmissionProtocol {
    private String nickNameRequest;
    private String restriction;
    private boolean serverRespond;

    public String getNickNameRequest() {
        return nickNameRequest;
    }

    public void setNickNameRequest(String nickNameRequest) {
        this.nickNameRequest = nickNameRequest;
    }

    public boolean getServerRespond() {
        return serverRespond;
    }

    public void setServerRespond(boolean serverRespond) {
        this.serverRespond = serverRespond;
    }

    public String getRestriction() {
        return restriction;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }
}
