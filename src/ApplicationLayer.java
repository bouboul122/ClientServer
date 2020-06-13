import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationLayer implements Layer{

    String filePath;
    byte[] fileBuffer;

    @Override
    public void sendToLowerLayer(byte[] buffer) throws IOException {
        String[] packetInfo = new String(buffer).split("\n");
        this.filePath = packetInfo[0];
        String[] ipAdressDestination = packetInfo[1].split("\\.");
        System.out.println(ipAdressDestination.length);

        //trouver l'adresse et la transformer en byte
        byte[] ipBytes = new byte[4];
        for (int i = 0; i < 4;i++) {
            ipBytes[i] = Integer.valueOf(ipAdressDestination[i]).byteValue();
        }

        //trouver le  nom de fichier en bytes
        byte[] fileNameBytes = packetInfo[0].getBytes();

        //Lire tout le fichier en byte
        byte[] byteFile = Files.readAllBytes(Paths.get(packetInfo[0]));

        //Combiner tout les arrays de bytes pour pouvoir envoyer a la couche de transport
        this.fileBuffer = new byte[ipBytes.length + 1 + fileNameBytes.length + byteFile.length];
        // Le byte buffer permet de tout inserer les differents arrays de bytes dans un array total.
        //Le + 1 est inserer pour prendre en compte la grandeur du nom.
        ByteBuffer byteBuffer = ByteBuffer.wrap(this.fileBuffer);
        byteBuffer.put(ipBytes).put(Integer.valueOf(fileNameBytes.length).byteValue()).put(fileNameBytes).put(byteFile);
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
    public void getFromHigherLayer() {
        System.err.println("Cannot send to a higher Layer");
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
    }
}
