import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TransportLayer implements Layer{

    //Layer lowerLayer = new DataLinkLayer();
    static final int MAXPACKETINTSIZE = 200;
    static final int PORT = 25000;
    ArrayList<byte[]> dataPackets;
    String ipDestination;
    byte[] sourceIp;
    byte[] fileName;
    byte[] allData;
    byte fileNameLength;

    public TransportLayer(){
        this.dataPackets = new ArrayList<>();
    }

    @Override
    public void getFromHigherLayer(byte[] buffer, String ipDestination, int port) throws IOException {
       this.allData = buffer;
       this.ipDestination = ipDestination;
       int numOfPackets = (int) Math.floor(this.allData.length/MAXPACKETINTSIZE) + 1;
       System.out.println(numOfPackets);
       //createPackets(numOfPackets);
    }

    public void createPackets(int numOfPackets){
        int counter = 0;

        String packetBodyStr;
        String packetHeaderStr;
        String fileNameStr = new String(this.fileName);
        String portStr = String.valueOf(PORT);
        String maxPackets = intToStr(numOfPackets, 5);
        String sourceDestinationStr = new String(this.sourceIp);

        while (counter < numOfPackets){
            String counterStr = intToStr(counter, 5);
            packetHeaderStr = this.ipDestination+','+sourceDestinationStr+','+portStr+','+counterStr+','+maxPackets;
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
            //System.out.println(Arrays.toString(packet));
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
    public void sendToLowerLayer(byte[] buffer, String ipDestination, int port) throws IOException {
        //createPackets();
        //this.lowerLayer.getFromHigherLayer(dataPackets.get(0));
        /*
        for (int i=0; i<dataPackets.size();i++){
            //DataLinkLayer.getFromHigherLayer
            //System.out.println("Sending packet number " + i);
        }
        */
    }

    @Override
    public void getFromLowerLayer() {

    }

    @Override
    public void sendToHigherLayer() {

    }

    public void sendACKPacquet(){

    }

    public void createACKpacquet(int packetNumber){
        String ackMessage = "RECEIVED";
        String ackNumber = intToStr(packetNumber, 5);
        String ackPacketHeaderStr = ackMessage+','+ackNumber;
        byte[] ackPacketHeader = ackPacketHeaderStr.getBytes();
    }

    public void sendMissedPacketNotice(int packetNumber){

    }

    public void createMissedPacketNotice(int packetNumber){
        String missedPacketStr = "MISSED PACKET";
        String missedPacketNumber = intToStr(packetNumber, 5);
        String packetHeaderStr = missedPacketStr+','+missedPacketNumber;
        byte[] missedPacketHeader = packetHeaderStr.getBytes();
    }
}
