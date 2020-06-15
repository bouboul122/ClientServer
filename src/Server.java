import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException {

        final int PORTSERVER = 30002;
        final int PORTCLIENT = 30001;


        ApplicationLayer applicationLayer = new ApplicationLayer(PORTSERVER, args[0]);
        while (true){
            applicationLayer.listen();
        }

    }

}
