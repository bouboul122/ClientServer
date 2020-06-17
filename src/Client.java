import java.io.IOException;

public class Client {


    public static void main(String[] args) throws IOException{

        final int PORTSERVER = 30002;
        final int PORTCLIENT = 30001;


        if (args.length == 0){
            System.err.println("File not found");
        } else {

            System.out.println("Reading " + args[0]);
            String destination = args[1];
            System.out.println("Sending to " + args[1]);
            String getError = args[2];
            System.out.println("Getting an Error? " + getError);
            String[] ipNumbers = args[1].split("\\.");

            ApplicationLayer applicationLayer = new ApplicationLayer(PORTCLIENT, getError);

            byte[] ipDestination = new byte[4];
            for (int i = 0; i < 4;i++) {
                ipDestination[i] = Integer.valueOf(ipNumbers[i]).byteValue();
            }
            byte[] filePath = args[0].getBytes();

            applicationLayer.sendToLowerLayer(filePath, ipDestination, PORTSERVER);
        }



    }
}
