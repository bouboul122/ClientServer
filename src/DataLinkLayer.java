import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.CRC32;


public class DataLinkLayer implements Layer{
    int myPort;
    int toPort;
    byte[] homemadePacket;
    byte[] packetWithCRC;
    FileHandler statisticFile;
    Layer upperLayer;
    boolean generateError;
    boolean alreadyGotError;

    DatagramSocket datagramSocket;
    DatagramPacket receivedPacket;

    public DataLinkLayer(int port, Layer upperLayer, String getError) throws SocketException {
        this.upperLayer = upperLayer;
        this.myPort = port;
        this.datagramSocket = new DatagramSocket(port);
        this.generateError = getError.equals("y");
        this.alreadyGotError = false;
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
        System.out.println("Listening...");
        this.datagramSocket.receive(this.receivedPacket);
        logReport("Packet Received, sending to verification");
        sendToHigherLayer();

    }


    @Override
    public void getFromLowerLayer(byte[] buffer, byte[] ipSource, int sourcePort) throws IOException {
        System.err.println("Invalid operation");
    }


    @Override
    public void sendToHigherLayer() throws IOException {
        byte[] receivedBytes = Arrays.copyOfRange(this.receivedPacket.getData(), 0, this.receivedPacket.getLength());
        byte[] sourceAdress = this.receivedPacket.getAddress().getAddress();
        int sourcePort = this.receivedPacket.getPort();
        byte crcByte = receivedBytes[0];
        byte[] bytesWithoutCRC = Arrays.copyOfRange(receivedBytes,1,receivedBytes.length);

        //Test pour faire une erreur dans le byte
        errorGenerator(bytesWithoutCRC);

        if (checkCRC(crcByte, bytesWithoutCRC)){
            System.out.println("No error! Yoohooo");
            logReport("Packet Verified. Sending it up");
            upperLayer.getFromLowerLayer(bytesWithoutCRC, sourceAdress, sourcePort);
        }
        else {
            System.out.println("Error in the byte");
            String errorMessage = "RESEND";
            byte[] errorMessageBytes = errorMessage.getBytes();
            System.out.println(Arrays.toString(errorMessageBytes));
            upperLayer.getFromLowerLayer(errorMessageBytes, sourceAdress, sourcePort);
        }



    }

    public boolean checkCRC(byte crc, byte[] array){
        CRC32 crcToCheck = new CRC32();
        crcToCheck.update(array);
        if(Byte.compare(Long.valueOf(crcToCheck.getValue()).byteValue(), crc) == 0){
            return true;
        }
        return false;
    }

    public void logReport(String strToWrite) throws IOException {
        Logger logger = Logger.getLogger("MyLog");

        try {
            // This block configure the logger with handler and formatter
            this.statisticFile = new FileHandler("C:\\Users\\ludov\\OneDrive - USherbrooke\\Ete 2020\\APP3\\ClientServer.log", true);
            logger.addHandler(this.statisticFile);
            SimpleFormatter formatter = new SimpleFormatter();
            this.statisticFile.setFormatter(formatter);
            // the following statement is used to log any messages
            //logger.info(strToWrite+"\n");
            this.statisticFile.close();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean errorGenerator(byte[] arrayToChange){
        if(this.myPort == 30002 && this.generateError && Math.floor(Math.random()*4) == 0 && !alreadyGotError){
            arrayToChange[4] = 2;
            alreadyGotError = true;
            return true;
        }
        return false;
    }

}
