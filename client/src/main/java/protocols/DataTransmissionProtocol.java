package protocols;

public class DataTransmissionProtocol {
    private ProtocolType type;
    private String protocol;
    private String secureWord;

    public ProtocolType getType() {
        return type;
    }

    public void setType(ProtocolType type) {
        this.type = type;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSecureWord() {
        return secureWord;
    }

    public void setSecureWord(String secureWord) {
        this.secureWord = secureWord;
    }
}
