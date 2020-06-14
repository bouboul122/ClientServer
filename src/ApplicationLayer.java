import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ApplicationLayer implements Layer{

    String filePath;
    byte[] fileNameBytes;
    String ipDestination;
    byte[] byteFile;
    Layer transportLayer;

    public ApplicationLayer(Layer transportLayer){
        this.transportLayer = transportLayer;
    }

    @Override
    public void sendToLowerLayer(byte[] buffer, String ipDestination, int toPort) throws IOException {
        String[] packetInfo = new String(buffer).split(";");
        this.filePath = packetInfo[0];
        this.ipDestination = ipDestination;

        //trouver le  nom de fichier en bytes
        getFileNameInBytes(packetInfo[0]);
        //Trouve la longueur du fichier
        byte fileNameLength = Integer.valueOf(packetInfo[0].length()).byteValue();

        //Lire le fichier
        getAllFileBytes(packetInfo[0]);

        //creer le buffer de byte
        byte[] bytesToSend = new byte[1 + this.byteFile.length + this.fileNameBytes.length];
        ByteBuffer bytesToSendBuffer = ByteBuffer.wrap(bytesToSend);
        bytesToSendBuffer.put(fileNameLength).put(fileNameBytes).put(byteFile);
        //transfere a la couche de transport en dessous
        transportLayer.getFromHigherLayer(this.fileNameBytes, packetInfo[1], 0);
    }

    public void getIpAdressInBytes(String ipAdress, byte[] adress){
        String[] ipAdressDestination = ipAdress.split("\\.");
        for (int i = 0; i < 4;i++) {
            adress[i] = Integer.valueOf(ipAdressDestination[i]).byteValue();
        }
    }

    public void getFileNameInBytes(String fileName){
        this.fileNameBytes = fileName.getBytes();
    }

    public void getAllFileBytes(String fileName) throws IOException {
       this.byteFile = Files.readAllBytes(Paths.get(fileName));
    }

    @Override
    public void getFromLowerLayer() {

            try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.filePath))) {
                String fileContent = "This is a sample text.";
                bufferedWriter.write(fileContent);
            } catch (IOException e) {
            }

    }

    @Override
    public void sendToHigherLayer() {
        System.err.println("Cannot send to a higher Layer");
    }

    @Override
    public void getFromHigherLayer(byte[] buffer, String ipDestination, int port) {
        System.err.println("Cannot send to a higher Layer");
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
    }
}
