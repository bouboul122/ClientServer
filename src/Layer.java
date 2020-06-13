import java.io.IOException;

public interface Layer {

    void sendToLowerLayer(byte[] buffer) throws IOException;
    void getFromLowerLayer();
    void sendToHigherLayer();
    void getFromHigherLayer(byte[] buffer);

}
