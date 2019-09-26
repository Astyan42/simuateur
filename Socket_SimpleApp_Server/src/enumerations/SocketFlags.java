package enumerations;

public enum SocketFlags {
    YOUR_TURN("YOUR_TURN"),
    WIN("WIN"),
    LOOSE("LOOSE");

    private String key;

    SocketFlags(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
