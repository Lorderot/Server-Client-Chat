package protocols;

public class NicknameTransmissionProtocol {
    private String nickNameRequest;
    private String restriction;
    private boolean serverResponse;

    public String getNickNameRequest() {
        return nickNameRequest;
    }

    public void setNickNameRequest(String nickNameRequest) {
        this.nickNameRequest = nickNameRequest;
    }

    public boolean getServerResponse() {
        return serverResponse;
    }

    public void setServerResponse(boolean serverResponse) {
        this.serverResponse = serverResponse;
    }

    public String getRestriction() {
        return restriction;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }
}
