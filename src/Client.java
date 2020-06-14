import java.io.IOException;
import java.nio.ByteBuffer;

public class Client {


    public static void main(String[] args) throws IOException {

        TransportLayer transportLayer = new TransportLayer();
        ApplicationLayer applicationLayer = new ApplicationLayer(transportLayer);

        if (args.length == 0){
            System.err.println("File not found");
        } else {

            String filePath = args[0];
            System.out.println("Reading " + args[0]);
            String destination = args[1];
            System.out.println("Sending to " + args[1]);
            String source = args[2];
            System.out.println("Sending from " + source);
            byte[] buffer = (filePath + ";" + destination + ";" + source).getBytes();

            applicationLayer.sendToLowerLayer(buffer, args[2], 0);

        }



    }
}
