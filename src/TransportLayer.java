import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TransportLayer implements Layer{

    Layer higherLayer;
    Layer lowerLayer;
    ArrayList<byte[]> dataPackets;
    byte[] destinationIp;
    byte[] fileName;

    public TransportLayer(Layer lowerLayer){
        this.lowerLayer = lowerLayer;
        this.dataPackets = new ArrayList<>();
    }
    @Override
    public void sendToLowerLayer(byte[] buffer) throws IOException {

    }

    @Override
    public void getFromLowerLayer() {

    }

    @Override
    public void sendToHigherLayer() {

    }

    @Override
    public void getFromHigherLayer(byte[] buffer) {
        this.destinationIp = Arrays.copyOfRange(buffer, 0, 4);
        byte fileLength = buffer[5];
        this.fileName = Arrays.copyOfRange(buffer, 6, 6 + Byte.valueOf(fileLength).intValue());
    }
}
