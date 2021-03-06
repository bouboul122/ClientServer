import java.io.IOException;

/**
 * Interface used for the different layer of the
 * model for the protocol
 */
public interface Layer {

    void sendToLowerLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException;
    void getFromLowerLayer(byte[] buffer, byte[] ipSource, int sourcePort) throws IOException;
    void sendToHigherLayer() throws IOException;
    void getFromHigherLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException;
    void listen(boolean setTimer) throws IOException;

}
