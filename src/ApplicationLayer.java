import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ApplicationLayer implements Layer{

    String filePath;
    byte[] fileBuffer;
    byte[] fileNameBytes;
    byte[] ipBytes;
    byte[]byteFile;
    Layer transportLayer;

    public ApplicationLayer(Layer transportLayer){
        this.transportLayer = transportLayer;
    }

    @Override
    public void sendToLowerLayer(byte[] buffer) throws IOException {
        String[] packetInfo = new String(buffer).split("\n");
        this.filePath = packetInfo[0];


        //trouver l'adresse et la transformer en byte
        getDestinationIpAdressInBytes(packetInfo[1]);

        //trouver le  nom de fichier en bytes
        getFileNameInBytes(packetInfo[0]);

        //Lire tout le fichier en byte
        getAllFileBytes(packetInfo[0]);

        //Combiner tout les arrays de bytes pour pouvoir envoyer a la couche de transport
        this.fileBuffer = new byte[this.ipBytes.length + 1 + this.fileNameBytes.length + this.byteFile.length];
        // Le byte buffer permet de tout inserer les differents arrays de bytes dans un array total.
        //Le + 1 est inserer pour prendre en compte la grandeur du nom.
        ByteBuffer byteBuffer = ByteBuffer.wrap(this.fileBuffer);
        byteBuffer.put(this.ipBytes).put(Integer.valueOf(this.fileNameBytes.length).byteValue()).put(this.fileNameBytes).put(byteFile);

        //System.out.println(Arrays.toString(this.fileBuffer));
        //transfere a la couche de transport en dessous
        transportLayer.getFromHigherLayer(this.fileBuffer);
    }

    public void getDestinationIpAdressInBytes(String ipAdress){
        String[] ipAdressDestination = ipAdress.split("\\.");
        this.ipBytes = new byte[4];
        for (int i = 0; i < 4;i++) {
            ipBytes[i] = Integer.valueOf(ipAdressDestination[i]).byteValue();
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
    public void getFromHigherLayer(byte[] buffer) {
        System.err.println("Cannot send to a higher Layer");
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
    }
}
