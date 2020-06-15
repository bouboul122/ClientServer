import java.io.IOException;

public interface Layer {

    void sendToLowerLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException;
    void getFromLowerLayer(byte[] buffer) throws IOException;
    void sendToHigherLayer() throws IOException;
    void getFromHigherLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException;
    void listen() throws IOException;

}
