import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TransportLayer implements Layer{

    static final int MAXPACKETINTSIZE = 200;
    static final int PORT = 25000;
    ArrayList<byte[]> dataPackets;
    byte[] destinationIp;
    byte[] sourceIp;
    byte[] fileName;
    byte[] allData;
    byte fileNameLength;

    public TransportLayer(){
        this.dataPackets = new ArrayList<>();
    }

    @Override
    public void getFromHigherLayer(byte[] buffer) throws IOException {
        this.destinationIp = Arrays.copyOfRange(buffer, 0, 4);
        this.sourceIp = Arrays.copyOfRange(buffer, 4, 8);
        this.fileNameLength = buffer[8];
        this.fileName = Arrays.copyOfRange(buffer, 9, 9 + Byte.valueOf(this.fileNameLength).intValue());
        this.allData = Arrays.copyOfRange(buffer, 9 + Byte.valueOf(this.fileNameLength).intValue(), buffer.length);
        sendToLowerLayer(allData);
    }

    public void createPackets(){
        int counter = 0;
        int numOfPackets = (int) Math.floor(this.allData.length/MAXPACKETINTSIZE) + 2;
        String packetBodyStr;
        String packetHeaderStr;
        String fileNameStr = new String(this.fileName);
        String portStr = String.valueOf(PORT);
        String maxPackets = intToStr(numOfPackets, 5);
        String destinationStr = new String(this.destinationIp);
        String sourceDestinationStr = new String(this.sourceIp);

        while (counter < numOfPackets){
            String counterStr = intToStr(counter, 5);
            packetHeaderStr = destinationStr+','+sourceDestinationStr+','+portStr+','+counterStr+','+maxPackets;
            if (counter == 0){
                packetBodyStr = fileNameStr;
            }
            else if (((counter-1)*200 + 200 )< this.allData.length){
                packetBodyStr = new String(Arrays.copyOfRange(this.allData, (counter-1)*200, (counter-1)*200 + 200));
            }
            else{
                packetBodyStr = new String(Arrays.copyOfRange(this.allData, (counter-1)*200, this.allData.length));
            }
            counter += 1;
            String packetStr = packetHeaderStr+';'+packetBodyStr;
            byte[] packet = new byte[packetStr.length()];
            ByteBuffer packetBuffer = ByteBuffer.wrap(packet);
            packetBuffer.put(packetStr.getBytes());
            dataPackets.add(packet);
            System.out.println(Arrays.toString(packet));
        }
    }

    public String intToStr(int num, int maxChars){
        String number = String.valueOf(num);
        while(number.length() < maxChars){
            number = '0' + number;
        }
        return number;
    }

    @Override
    public void sendToLowerLayer(byte[] buffer) throws IOException {
        createPackets();
        for (int i=0; i<dataPackets.size();i++){
            //DataLinkLayer.getFromHigherLayer
            System.out.println("Sending packet number " + i);
        }
    }

    @Override
    public void getFromLowerLayer(byte[] buffer) throws IOException{

    }

    @Override
    public void sendToHigherLayer() {

    }

    public void sendMissedPacketNotice(int packetNumber){

    }

    public void createMissedPacketNotice(int packetNumber){
        String missedPacketStr = "MISSED PACKET";
        String missedPacketNumber = intToStr(packetNumber, 5);
        String packetHeaderStr = missedPacketStr + ','+missedPacketNumber;
        byte[] missedPacketHeader = packetHeaderStr.getBytes();
    }
}
