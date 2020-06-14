import java.io.IOException;

public interface Layer {

    void sendToLowerLayer(byte[] buffer, String ipDestination, int port) throws IOException;
    void getFromLowerLayer();
    void sendToHigherLayer();
    void getFromHigherLayer(byte[] buffer, String ipDestination, int port) throws IOException;

}
