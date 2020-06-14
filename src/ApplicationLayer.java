import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ApplicationLayer implements Layer{

    byte[] filePath;
    byte[] fileNameBytes;
    byte[] ipDestination;
    byte[] byteFile;
    Layer transportLayer;

    public ApplicationLayer(Layer transportLayer){
        this.transportLayer = transportLayer;
    }

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
        //byte[] bytesToSend = new byte[1 + this.byteFile.length + this.fileNameBytes.length];
        //ByteBuffer bytesToSendBuffer = ByteBuffer.wrap(bytesToSend);
        //bytesToSendBuffer.put(fileNameLength).put(fileNameBytes).put(byteFile);
        //transfere a la couche de transport en dessous
        //transportLayer.getFromHigherLayer(this.fileNameBytes, ipDestination, 0);
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
    public void getFromLowerLayer(byte [] buffer) throws IOException {

        String[] body = new String(buffer).split(";");

        try {
            FileWriter myWriter = new FileWriter("C:\\Users\\jordl\\OneDrive - USherbrooke\\S3\\APP3\\"+new String(body[0]));
            myWriter.write(new String (body[1]));
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }



    }

    @Override
    public void sendToHigherLayer() {
        System.err.println("Cannot send to a higher Layer");
    }

    @Override
    public void getFromHigherLayer(byte[] buffer, byte[] ipDestination, int port) {
        System.err.println("Cannot send to a higher Layer");
    }

}
