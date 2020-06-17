import java.io.*;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


/**
 * Application layer
 *
 * @author Ludovic Boulanger
 * @author Jordan Choquet
 */

public class ApplicationLayer implements Layer{

    byte[] filePath;
    byte[] ipDestination;
    byte[] byteFile;
    Layer downwardLayer;


    /**
     * Constructor
     *
     * @param port
     * @param getError
     * @throws SocketException
     */
    public ApplicationLayer(int port, String getError) throws SocketException {
        downwardLayer = new TransportLayer(port, this, getError);
    }

    /**
     * Sends to the transport layer
     * 
     *
     * @param filePath
     * @param ipDestination
     * @param toPort
     * @throws IOException
     */
    @Override
    public void sendToLowerLayer(byte[] filePath, byte[] ipDestination, int toPort) throws IOException {
        this.filePath = filePath;
        this.ipDestination = ipDestination;

        //trouver le  nom de fichier en bytes
        //Trouve la longueur du fichier
        byte fileNameLength = Integer.valueOf(filePath.length).byteValue();

        //Lire le fichier
        getAllFileBytes(new String(filePath));

        //adressIP en byte

        //creer le buffer de byte
        byte[] bytesToSend = new byte[1 + this.byteFile.length + this.filePath.length];
        ByteBuffer bytesToSendBuffer = ByteBuffer.wrap(bytesToSend);
        bytesToSendBuffer.put(fileNameLength).put(filePath).put(byteFile);
        //transfere a la couche de transport en dessous
        downwardLayer.getFromHigherLayer(bytesToSend, ipDestination, toPort);
    }

    /**
     * Receives an address in string separated by '.',
     * than splits on every '.' and converts it into int type
     * and adds it to a byte array
     *
     * @param ipAdress
     * @param adress
     */
    public void getIpAdressInBytes(String ipAdress, byte[] adress){
        String[] ipAdressDestination = ipAdress.split("\\.");
        for (int i = 0; i < 4;i++) {
            adress[i] = Integer.valueOf(ipAdressDestination[i]).byteValue();
        }
    }

    /**
     * Takes the file in string format and transfers it in bytes
     *
     * @param fileName
     * @throws IOException
     */
    public void getAllFileBytes(String fileName) throws IOException {
       this.byteFile = Files.readAllBytes(Paths.get(fileName));
    }

    /**
     * Receives packet from the transport layer and
     * outputs the information in a file with a string format
     * which is stored on the computer's repository
     *
     * @param buffer
     * @param ipSource
     * @param sourcePort
     * @throws IOException
     */
    @Override
    public void getFromLowerLayer(byte[] buffer, byte[] ipSource, int sourcePort) throws IOException {
        byte fileNameLength = buffer[0];
        byte[] fileNameBytes = Arrays.copyOfRange(buffer,1, 1+Integer.valueOf(fileNameLength));
        System.out.println("Writing to " + new String(fileNameBytes));
        String fileInWords = new String(Arrays.copyOfRange(buffer,1+Integer.valueOf(fileNameLength), buffer.length));
        //String filePath = "C:\\Users\\ludov\\OneDrive - USherbrooke\\Ete 2020\\APP3\\"+new String(fileNameBytes);
        String filePath = "C:\\Users\\jordl\\OneDrive - USherbrooke\\S3\\APP3\\"+new String(fileNameBytes);
        try {
            FileWriter writer = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            bufferedWriter.write(fileInWords);

            bufferedWriter.close();
            System.out.println("Closed file");
        } catch (IOException e) {
            System.err.println("Could not write to file");
            e.printStackTrace();
        }
        System.out.println("Done writing to file");

    }

    /**
     * There is no higher layer to be accessed
     */
    @Override
    public void sendToHigherLayer() {
        System.err.println("Cannot send to a higher Layer");
    }

    /**
     * There is no higher layer to be accessed
     *
     * @param buffer
     * @param ipDestination
     * @param port
     */
    @Override
    public void getFromHigherLayer(byte[] buffer, byte[] ipDestination, int port) {
        System.err.println("Cannot send to a higher Layer");
    }

    /**
     * Waits action to be taken from the lower layer
     *
     * @throws IOException
     */
    @Override
    public void listen() throws IOException {
        downwardLayer.listen();
    }

}
