import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;


public class DataLinkLayer implements Layer{
    int myPort;
    int toPort;
    byte[] homemadePacket;
    byte[] packetWithCRC;
    Layer upperLayer;

    DatagramSocket datagramSocket;
    DatagramPacket receivedPacket;

    public DataLinkLayer(int port, Layer upperLayer) throws SocketException {
        this.upperLayer = upperLayer;
        this.myPort = port;
        this.datagramSocket = new DatagramSocket(port);
    }

    @Override
    public void getFromHigherLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException {
        this.toPort = port;
        this.homemadePacket = buffer;
        this.packetWithCRC = new byte[homemadePacket.length + 1];
        addCRC();
        sendToLowerLayer(this.packetWithCRC, ipDestination, this.toPort);

    }

    public void addCRC(){
        CRC32 crc = new CRC32();
        crc.update(homemadePacket);
        ByteBuffer bufferWithCRC = ByteBuffer.wrap(this.packetWithCRC);
        bufferWithCRC.put(Long.valueOf(crc.getValue()).byteValue()).put(homemadePacket);

    }

    @Override
    public void sendToLowerLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException {

        InetAddress address = InetAddress.getByAddress(ipDestination);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, this.toPort);
        this.datagramSocket.send(packet);

    }


    @Override
    public void listen() throws IOException {
        byte[] buffer = new byte[400];
        this.receivedPacket = new DatagramPacket(buffer, buffer.length);
//        System.out.println("Listening...");
        this.datagramSocket.receive(this.receivedPacket);

//        System.out.println("Packet received");
        sendToHigherLayer();
    }


    @Override
    public void getFromLowerLayer(byte[] buffer) throws IOException {
        System.err.println("Invalid operation");
    }


    @Override
    public void sendToHigherLayer() throws IOException {
        byte[] receivedBytes = Arrays.copyOfRange(this.receivedPacket.getData(), 0, this.receivedPacket.getLength());
        byte crcByte = receivedBytes[0];
        byte[] bytesWithoutCRC = Arrays.copyOfRange(receivedBytes,1,receivedBytes.length);
        if (checkCRC(crcByte, bytesWithoutCRC)){
            System.out.println("No error! Yoohooo");
        }
        upperLayer.getFromLowerLayer(bytesWithoutCRC);


    }

    public boolean checkCRC(byte crc, byte[] array){
        CRC32 crcToCheck = new CRC32();
        crcToCheck.update(array);
        if(Byte.compare(Long.valueOf(crcToCheck.getValue()).byteValue(), crc) == 0){
            return true;
        }
        return false;
    }

}
