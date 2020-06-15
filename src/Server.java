import java.awt.event.KeyEvent;
import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException {

        final int PORTSERVER = 30002;
        final int PORTCLIENT = 30001;
        boolean keepRunning = true;


        ApplicationLayer applicationLayer = new ApplicationLayer(PORTSERVER, args[0]);
        while (keepRunning){
            applicationLayer.listen();
        }

    }

}
