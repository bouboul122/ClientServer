import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Transport Layor
 *
 * @author Ludovic Boulanger, Jordan Choquet
 */
public class TransportLayer implements Layer{

    int numOfFailures;
    int myPort;
    int toPort;
    Layer upwardLayer;
    DataLinkLayer lowerLayer;
    static final int MAXPACKETINTSIZE = 200;
    static final String ACKNOWLEDGEMESSAGE = "ACKNO";
    static final String RESENDMESSAGE = "NOACK";
    static final String TERMINATEMESSAGE = "RESETCONNECT";
    ArrayList<byte[]> dataPackets;
    int lenOfBytesToSend;
    byte[] ipDestination;
    byte[] allData;
    byte[] lastPacketSent;
    int numPacketsAcknowledged;
    int packetsSent;

    byte[] ipSource;
    int clientPort;

    public TransportLayer(int port, Layer upwardLayer, String getError) throws SocketException {
        this.myPort = port;
        this.lowerLayer = new DataLinkLayer(myPort, this, getError);
        this.upwardLayer = upwardLayer;
        this.dataPackets = new ArrayList<>();
        this.lenOfBytesToSend = 0;
        int numPacketsAcknowledged = 0;
        this.numOfFailures = 0;

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
        if(new String(buffer).split(",")[0].equals(ACKNOWLEDGEMESSAGE)) {
            System.out.println("Sending ACK for packet number " + new String(buffer).split(",")[1]);
            this.lowerLayer.getFromHigherLayer(buffer, ipDestination, port);
        } else if (new String(buffer).split(",")[0].equals(RESENDMESSAGE)){
            System.out.println("Sending Missed packet notice for packet " + new String(buffer).split(",")[1]);
            this.numOfFailures += 1;
            if (numOfFailures == 3){
                this.dataPackets.clear();
                this.numPacketsAcknowledged = 0;
                System.out.println("RESETTING CONNECTION");
                byte[] resetMessage = createResetMessage();
                this.sendResetMessage(resetMessage, ipDestination, port);
            } else{
                this.lowerLayer.getFromHigherLayer(buffer, ipDestination, port);
            }

        }else{
            while(this.packetsSent < dataPackets.size()){
                System.out.println("Sending packet number " + this.packetsSent);
                this.lowerLayer.getFromHigherLayer(dataPackets.get(this.packetsSent), ipDestination, port);
                this.packetsSent++;
                this.lowerLayer.setSocketTimeout();
                this.lowerLayer.listen();
            }
        }
    }

    @Override
    public void getFromLowerLayer(byte[] buffer, byte[] ipSource, int sourcePort) throws IOException{
        byte[] byteHeader = Arrays.copyOfRange(buffer,0,11);
        String byteHeaderStr= new String(byteHeader);
        String[] headerArray = byteHeaderStr.split(",");
        System.out.println(headerArray[0]);

        if (headerArray[0].equals(ACKNOWLEDGEMESSAGE)){
            System.out.println("ACK for packet " + headerArray[1] + " received! Continuing to send...");
            this.numPacketsAcknowledged += 1;
            System.out.println("number of acknowleged packets is now: " + this.numPacketsAcknowledged);
        } else if (headerArray[0].equals(RESENDMESSAGE)){
            System.out.println("Need to Resend packet "+ headerArray[1]);
            this.packetsSent = Integer.parseInt(headerArray[1]);
        } else if(headerArray[0].equals(TERMINATEMESSAGE)){
            this.packetsSent = 0;
            //Cote serveur qui prend les paquets et les met dans datapackets
        } else if (Integer.parseInt(headerArray[0]) == this.numPacketsAcknowledged){
            byte[] bytesWithoutHeader = Arrays.copyOfRange(buffer, 12, buffer.length);
            System.out.println("Received packet number " + headerArray[0] + " out of " + headerArray[1]);
            this.lenOfBytesToSend += bytesWithoutHeader.length;
            dataPackets.add(bytesWithoutHeader);
            this.numPacketsAcknowledged +=1;

            //sendACKPaquet
            byte[] ackPacket = createACKpacket(headerArray[0]);

            sendACKPacket(ackPacket, ipSource, sourcePort);
            System.out.println("Added packet number " + Integer.parseInt(headerArray[0]));
            if (Integer.parseInt(headerArray[0]) == Integer.parseInt(headerArray[1]) - 1){
                System.out.println("Sending everything to upper layer");
                this.numPacketsAcknowledged = 0;
                sendToHigherLayer();
            }
         //Need a reTransmit
        } else if (Integer.parseInt(headerArray[0]) > this.numPacketsAcknowledged) {
            byte[] missedPacket = createMissedPacketNotice(intToStr(this.numPacketsAcknowledged, 5));
            sendMissedPacketNotice(missedPacket, ipSource, sourcePort);
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

    public byte[] createACKpacket(String packetNumber){
        String ackMessage = this.ACKNOWLEDGEMESSAGE;
        String ackPacketHeaderStr = ackMessage+','+packetNumber;
        byte[] ackPacketHeader = ackPacketHeaderStr.getBytes();
        return ackPacketHeader;
    }

    public void sendMissedPacketNotice(byte[] packetBytes, byte[] ipDestination, int port) throws IOException {
        sendToLowerLayer(packetBytes, ipDestination, port);
    }

    public byte[] createMissedPacketNotice(String packetNumber){
        String missedPacketStr = this.RESENDMESSAGE;
        String packetHeaderStr = missedPacketStr+','+packetNumber;
        byte[] missedPacketHeader = packetHeaderStr.getBytes();
        return missedPacketHeader;
    }

    public byte[] createResetMessage(){
        byte[] resetConnectionMessage = TERMINATEMESSAGE.getBytes();
        return resetConnectionMessage;
    }

    public void sendResetMessage(byte[] packetBytes, byte[] ipDestination, int port) throws IOException {
        sendToLowerLayer(packetBytes, ipDestination, port);
    }
}
