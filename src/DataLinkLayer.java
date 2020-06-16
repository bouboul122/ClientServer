import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.CRC32;


/**
 * Data Link Layer
 *
 * @author Ludovic Boulanger
 * @author Jordan Choquet
 *
 */

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

    /**
     * Constructor of the class
     *
     * @param port
     * @param upperLayer
     * @param getError
     * @throws SocketException
     */

    public DataLinkLayer(int port, Layer upperLayer, String getError) throws SocketException {
        this.upperLayer = upperLayer;
        this.myPort = port;
        this.datagramSocket = new DatagramSocket(port);
        this.generateError = getError.equals("y");
        this.alreadyGotError = false;
    }

    /**
     * Receives a buffer of the higher layer,
     * which in this case is the transport layer.
     *
     * @param buffer
     * @param ipDestination
     * @param port
     * @throws IOException
     */

    @Override
    public void getFromHigherLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException {
        this.toPort = port;
        this.homemadePacket = buffer;
        this.packetWithCRC = new byte[homemadePacket.length + 1];
        addCRC();
        sendToLowerLayer(this.packetWithCRC, ipDestination, this.toPort);

    }

    /**
     * Adds a CRC to the existing packet
     */
    public void addCRC(){
        CRC32 crc = new CRC32();
        crc.update(homemadePacket);
        ByteBuffer bufferWithCRC = ByteBuffer.wrap(this.packetWithCRC);
        bufferWithCRC.put(Long.valueOf(crc.getValue()).byteValue()).put(homemadePacket);
    }

    /**
     * Sends to the lower layor,
     * which in this case is the Server's data link layer
     *
     * @param buffer
     * @param ipDestination
     * @param port
     * @throws IOException
     */
    @Override
    public void sendToLowerLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException {

        InetAddress address = InetAddress.getByAddress(ipDestination);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, this.toPort);
        this.datagramSocket.send(packet);

    }


    /**
     * Client is waiting for the packet to be sent
     * @throws IOException
     */
    @Override
    public void listen() throws IOException {
        byte[] buffer = new byte[400];
        this.receivedPacket = new DatagramPacket(buffer, buffer.length);
        try{
            System.out.println("Listening...");
            this.datagramSocket.receive(this.receivedPacket);
            logReport("Packet Received, sending to verification");
            sendToHigherLayer();
        } catch (SocketTimeoutException e){
            System.out.println("Timed out");
            //throw new IOException("Socket time out test");
        }


    }


    /**
     * Does not receive anything from the Server's Data link layer
     *
     * @param buffer
     * @throws IOException
     */
    @Override
    public void getFromLowerLayer(byte[] buffer, byte[] ipSource, int sourcePort) throws IOException {
        System.err.println("Invalid operation");
    }


    /**
     * Sends to the transport layer
     * If the packet is corrupted the server will be
     * put in the listening mode waiting to receive de
     * retransmission of the packet
     *
     * @throws IOException
     */
    @Override
    public void sendToHigherLayer() throws IOException {
        byte[] receivedBytes = Arrays.copyOfRange(this.receivedPacket.getData(), 0, this.receivedPacket.getLength());
        byte[] sourceAdress = this.receivedPacket.getAddress().getAddress();
        int sourcePort = this.receivedPacket.getPort();
        byte crcByte = receivedBytes[0];
        byte[] bytesWithoutCRC = Arrays.copyOfRange(receivedBytes,1,receivedBytes.length);

        //Test pour faire une erreur dans le byte
        bytesWithoutCRC = errorGenerator(bytesWithoutCRC);

        if (checkCRC(crcByte, bytesWithoutCRC)){
            System.out.println("No error! Yoohooo");
            logReport("Packet Verified. Sending it up");
            upperLayer.getFromLowerLayer(bytesWithoutCRC, sourceAdress, sourcePort);
        }
        else {
            System.out.println("Error in the byte");
            System.out.println(Arrays.toString(bytesWithoutCRC));
            //throw new IOException("Ca chie");
            listen();
            //upperLayer.getFromLowerLayer(bytesWithoutCRC, sourceAdress, sourcePort);
        }



    }

    /**
     * @param crc
     * @param array
     * @return
     */
    public boolean checkCRC(byte crc, byte[] array){
        CRC32 crcToCheck = new CRC32();
        crcToCheck.update(array);
        if(Byte.compare(Long.valueOf(crcToCheck.getValue()).byteValue(), crc) == 0){
            return true;
        }
        return false;
    }

    /**
     * Creates a logger report which writes in a file .log
     * the time, date and type of operation accomplished
     * The logger is saved in a repository on the computer
     *
     * @param strToWrite
     * @throws IOException
     */
    public void logReport(String strToWrite) throws IOException {
        Logger logger = Logger.getLogger("MyLog");

        try {
            // This block configure the logger with handler and formatter
            //this.statisticFile = new FileHandler("C:\\Users\\ludov\\OneDrive - USherbrooke\\Ete 2020\\APP3\\ClientServer.log", true);
            this.statisticFile = new FileHandler("C:\\Users\\jordl\\OneDrive - USherbrooke\\S3\\APP3\\ClientServer.log", true);
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

    /**
     * Random generator that has the odds on 2 to trigger an error
     * on the fourth byte of a trame
     *
     * @param arrayToChange
     * @return returns true if the odds are good
     */
    public byte[] errorGenerator(byte[] arrayToChange){
        if(this.myPort == 30002 && this.generateError && Math.floor(Math.random()*20) == 0){
            arrayToChange = "BADPACKET".getBytes();
            this.alreadyGotError = true;

        }
        return arrayToChange;
    }

    /**
     * Sets a timeout used to when the server
     * is waiting for an acknowledged packet
     *
     * @throws SocketException
     */
    public void setSocketTimeout() throws SocketException {
        this.datagramSocket.setSoTimeout(200);
    }

}
