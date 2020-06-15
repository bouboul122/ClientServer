import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TransportLayer implements Layer{

    int myPort;
    int toPort;
    Layer upwardLayer;
    Layer lowerLayer;
    static final int MAXPACKETINTSIZE = 200;
    ArrayList<byte[]> dataPackets;
    int lenOfBytesToSend;
    byte[] ipDestination;
    byte[] allData;

    public TransportLayer(int port, Layer upwardLayer, String getError) throws SocketException {
        this.myPort = port;
        this.lowerLayer = new DataLinkLayer(myPort, this, getError);
        this.upwardLayer = upwardLayer;
        this.dataPackets = new ArrayList<>();
        this.lenOfBytesToSend = 0;
    }

    @Override
    public void getFromHigherLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException {
        this.toPort = port;
       this.allData = buffer;
       this.ipDestination = ipDestination;
       int numOfPackets = (int) Math.floor(this.allData.length/MAXPACKETINTSIZE) + 1;
       System.out.println(numOfPackets);
       createPackets(numOfPackets);
       sendToLowerLayer(buffer, ipDestination, this.toPort);
    }

    @Override
    public void listen() throws IOException {
        lowerLayer.listen();
    }

    public void createPackets(int numOfPackets){
        int counter = 0;

        String packetBodyStr;
        String packetHeaderStr;
        String portStr = String.valueOf(toPort);
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
        for (int i=0; i<dataPackets.size();i++){
            System.out.println("Sending packet number " + i);
            this.lowerLayer.getFromHigherLayer(dataPackets.get(i), ipDestination, port);
        }
    }

    @Override
    public void getFromLowerLayer(byte[] buffer) throws IOException{
        byte[] byteHeader = Arrays.copyOfRange(buffer,0,11);
        byte[] bytesWithoutHeader = Arrays.copyOfRange(buffer, 12, buffer.length);

        String[] byteHeaderStr= new String(byteHeader).split(",");
        System.out.println("Received packet number " + byteHeaderStr[0] + " out of " + byteHeaderStr[1]);

        if (Integer.parseInt(byteHeaderStr[0]) == dataPackets.size()){
            this.lenOfBytesToSend += bytesWithoutHeader.length;
            dataPackets.add(bytesWithoutHeader);
            System.out.println("Added packet number " + Integer.parseInt(byteHeaderStr[0]));
            if (Integer.parseInt(byteHeaderStr[0]) == Integer.parseInt(byteHeaderStr[1]) - 1){
                System.out.println("Sending everything to upper layer");
                //sendACKPaquet
                sendToHigherLayer();
            }
        } else {
            //send Error packet
        }

    }

    @Override
    public void sendToHigherLayer() throws IOException {
        byte[] bytesToSend = new byte[lenOfBytesToSend];
        ByteBuffer bytesToSendBuffer = ByteBuffer.wrap(bytesToSend);
        for (byte[] packet: dataPackets){
            bytesToSendBuffer.put(packet);
        }
        upwardLayer.getFromLowerLayer(bytesToSend);
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
