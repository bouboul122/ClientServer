import java.io.IOException;
import java.nio.ByteBuffer;

public class Client {


    public static void main(String[] args) throws IOException {

        final int PORT = 25000;

        TransportLayer transportLayer = new TransportLayer();
        ApplicationLayer applicationLayer = new ApplicationLayer(PORT);

        if (args.length == 0){
            System.err.println("File not found");
        } else {

            System.out.println("Reading " + args[0]);
            String destination = args[1];
            System.out.println("Sending to " + args[1]);
            //String source = args[2];
            //System.out.println("Sending from " + source);
            byte[] ipDestination = args[1].getBytes();
            byte[] filePath = args[0].getBytes();

            applicationLayer.sendToLowerLayer(filePath, ipDestination, 0);


            byte[] bufferByte = ("one-liners.txt;Life is wonderful. Without it we'd all be dead.").getBytes();
            System.out.println("-----------------------------");
//          applicationLayer.getFromLowerLayer(bufferByte);
        }



    }
}
