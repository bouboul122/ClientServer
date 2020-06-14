import java.io.IOException;

public interface Layer {

    void sendToLowerLayer(byte[] buffer) throws IOException;
    void getFromLowerLayer(byte[] buffer) throws IOException;
    void sendToHigherLayer();
    void getFromHigherLayer(byte[] buffer) throws IOException;

}
