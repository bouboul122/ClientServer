import java.io.IOException;

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

            applicationLayer.sendToLowerLayer(buffer);

        }

    }
}
