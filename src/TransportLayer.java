import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TransportLayer implements Layer{

    Layer upwardLayer;
    Layer lowerLayer;
    static final int MAXPACKETINTSIZE = 200;
    static final int PORT = 25000;
    ArrayList<byte[]> dataPackets;
    byte[] ipDestination;
    byte[] sourceIp;
    byte[] fileName;
    byte[] allData;
    byte fileNameLength;

    public TransportLayer(Layer upwardLayer){
        this.lowerLayer = new DataLinkLayer();
        this.upwardLayer = upwardLayer;
        this.dataPackets = new ArrayList<>();
    }

    @Override
    public void getFromHigherLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException {
       this.allData = buffer;
       this.ipDestination = ipDestination;
       int numOfPackets = (int) Math.floor(this.allData.length/MAXPACKETINTSIZE) + 1;
       System.out.println(numOfPackets);
       createPackets(numOfPackets);
       sendToLowerLayer(buffer, ipDestination, port);
    }

    public void createPackets(int numOfPackets){
        int counter = 0;

        String packetBodyStr;
        String packetHeaderStr;
        String portStr = String.valueOf(PORT);
        String maxPackets = intToStr(numOfPackets, 5);
        String ipDestinationStr = new String(this.ipDestination);

        while (counter < numOfPackets){
            String counterStr = intToStr(counter, 5);
            packetHeaderStr = counterStr+","+maxPackets+";";

            if (((counter)*200 + 200 )< this.allData.length){
                packetBodyStr = new String(Arrays.copyOfRange(this.allData, (counter)*200, (counter)*200 + 200));
            }
            else{
                packetBodyStr = new String(Arrays.copyOfRange(this.allData, (counter)*200, this.allData.length));
            }

            counter += 1;
            byte[] packet = new byte[packetHeaderStr.getBytes().length + packetBodyStr.getBytes().length];
            ByteBuffer packetBuffer = ByteBuffer.wrap(packet);
            packetBuffer.put(packetHeaderStr.getBytes()).put(packetBodyStr.getBytes());
            dataPackets.add(packet);
        }
        for (byte[] packetToString : dataPackets){
            System.out.println(Arrays.toString(packetToString));
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
    public void sendToLowerLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException {
        this.lowerLayer.getFromHigherLayer(dataPackets.get(0), ipDestination, port);
        for (int i=0; i<dataPackets.size();i++){
            System.out.println("Sending packet number " + i);
        }
    }

    @Override
    public void getFromLowerLayer(byte[] buffer) throws IOException{
        String bufferStr = new String(buffer);
        String paquetNumber = bufferStr.split(";")[0];
        String maxPaquets = bufferStr.split(";")[1];
        if (Integer.valueOf(paquetNumber) == dataPackets.size()){
            dataPackets.add(buffer);
            //sendACKPaquet
            //sendToHigherLayer
        } else {
            //sendMissedPaquetNotice
        }

    }

    @Override
    public void sendToHigherLayer() {

    }

    public void sendACKPacquet(byte[] packetBytes, byte[] ipDestination, int port){

    }

    public void createACKpacquet(int packetNumber){
        String ackMessage = "RECEIVED";
        String ackNumber = intToStr(packetNumber, 5);
        String ackPacketHeaderStr = ackMessage+','+ackNumber;
        byte[] ackPacketHeader = ackPacketHeaderStr.getBytes();
    }

    public void sendMissedPacketNotice(byte[] packetBytes, byte[] ipDestination, int port){

    }

    public void createMissedPacketNotice(int packetNumber){
        String missedPacketStr = "MISSED PACKET";
        String missedPacketNumber = intToStr(packetNumber, 5);
        String packetHeaderStr = missedPacketStr+','+missedPacketNumber;
        byte[] missedPacketHeader = packetHeaderStr.getBytes();
    }
}
