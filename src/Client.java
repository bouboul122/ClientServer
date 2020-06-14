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
            byte[] buffer = (filePath + "\n" + destination).getBytes();

            byte[] bufferByte = ("one-liners.txt;Life is wonderful. Without it we'd all be dead.").getBytes();
            //applicationLayer.sendToLowerLayer(buffer);
            System.out.println("-----------------------------");
            applicationLayer.getFromLowerLayer(bufferByte);
        }



    }
}
