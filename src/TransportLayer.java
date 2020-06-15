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
    byte[] lastPacketSent;
    int numPacketsAcknowledged;

    byte[] ipSource;
    int clientPort;

    public TransportLayer(int port, Layer upwardLayer, String getError) throws SocketException {
        this.myPort = port;
        this.lowerLayer = new DataLinkLayer(myPort, this, getError);
        this.upwardLayer = upwardLayer;
        this.dataPackets = new ArrayList<>();
        this.lenOfBytesToSend = 0;
        int numPacketsAcknowledged = 0;
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
        String maxPackets = intToStr(numOfPackets, 5);

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
        if(new String(buffer).split(",")[0].equals("RECEIVED")){
            System.out.println("Sending received packet for packet number " + new String(buffer).split(",")[1]);
            this.lowerLayer.getFromHigherLayer(buffer, ipDestination, port);
        }
        else if (new String(buffer).split(",")[0].equals("RESEND")){
            System.out.println("Sending missed packet for packet number " + new String(buffer).split(",")[1]);
            this.lowerLayer.getFromHigherLayer(buffer, ipDestination, port);
        }
        else{
            for (int i=0; i<dataPackets.size();i++){
                System.out.println("Sending packet number " + i);
                lastPacketSent = dataPackets.get(i);
                this.lowerLayer.getFromHigherLayer(dataPackets.get(i), ipDestination, port);
                this.lowerLayer.listen();
            }
        }
    }

    @Override
    public void getFromLowerLayer(byte[] buffer, byte[] ipSource, int sourcePort) throws IOException{
        byte[] byteHeader = Arrays.copyOfRange(buffer,0,11);


        String[] byteHeaderStr= new String(byteHeader).split(",");
        System.out.println(byteHeaderStr[0]);

        if (byteHeaderStr[0].equals("RECEIVED")){
            System.out.println("ACK for packet " + this.numPacketsAcknowledged + " received! Continuing to send...");
            this.numPacketsAcknowledged += 1;
            System.out.println("number of acknowleged packets is now: " + this.numPacketsAcknowledged);
        } else if (Arrays.equals(buffer, "RESEND".getBytes())){
            this.ipSource = ipSource;
            this.clientPort = sourcePort;
            System.out.println("Creating a missed packet notice!");
            byte[] missedPacket = createMissedPacketNotice(numPacketsAcknowledged);
            sendMissedPacketNotice(missedPacket, ipSource, sourcePort);
        } else if (byteHeaderStr[0].equals("RESENDLAST")){
            System.out.println("Sending missed packet");
            sendMissedPacketNotice(lastPacketSent, ipSource, sourcePort);
        } else if (Integer.parseInt(byteHeaderStr[0]) == this.numPacketsAcknowledged){
            byte[] bytesWithoutHeader = Arrays.copyOfRange(buffer, 12, buffer.length);
            System.out.println("Received packet number " + byteHeaderStr[0] + " out of " + byteHeaderStr[1]);
            this.lenOfBytesToSend += bytesWithoutHeader.length;
            dataPackets.add(bytesWithoutHeader);
            this.numPacketsAcknowledged +=1;

            //sendACKPaquet
            byte[] ackPacket = createACKpacket(this.dataPackets.size()-1);

            sendACKPacket(ackPacket, ipSource, sourcePort);
            System.out.println("Added packet number " + Integer.parseInt(byteHeaderStr[0]));
            if (Integer.parseInt(byteHeaderStr[0]) == Integer.parseInt(byteHeaderStr[1]) - 1){
                System.out.println("Sending everything to upper layer");
                this.numPacketsAcknowledged = 0;
                sendToHigherLayer();

            }
        }
    }

    @Override
    public void sendToHigherLayer() throws IOException {
        byte[] bytesToSend = new byte[lenOfBytesToSend];
        ByteBuffer bytesToSendBuffer = ByteBuffer.wrap(bytesToSend);
        for (byte[] packet: dataPackets){
            bytesToSendBuffer.put(packet);
        }
        this.dataPackets.clear();
        upwardLayer.getFromLowerLayer(bytesToSend, this.ipDestination, this.toPort);
    }

    public void sendACKPacket(byte[] packetBytes, byte[] ipDestination, int port) throws IOException {
        sendToLowerLayer(packetBytes, ipDestination, port);
    }

    public byte[] createACKpacket(int packetNumber){
        String ackMessage = "RECEIVED";
        String ackNumber = intToStr(packetNumber, 5);
        String ackPacketHeaderStr = ackMessage+','+ackNumber;
        byte[] ackPacketHeader = ackPacketHeaderStr.getBytes();
        return ackPacketHeader;
    }

    public void sendMissedPacketNotice(byte[] packetBytes, byte[] ipDestination, int port) throws IOException {
        sendToLowerLayer(packetBytes, ipDestination, port);
    }

    public byte[] createMissedPacketNotice(int packetNumber){
        String missedPacketStr = "RESENDLAST";
        String missedPacketNumber = intToStr(packetNumber, 5);
        String packetHeaderStr = missedPacketStr+','+missedPacketNumber;
        byte[] missedPacketHeader = packetHeaderStr.getBytes();
        return missedPacketHeader;
    }
}
