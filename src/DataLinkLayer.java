import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

/*
public class DataLinkLayer implements Layer{
    byte[] homemadePacket;
    byte[] homemadePacketHeader;
    byte[] homemadePacketBody;
    byte[] packetWithCRC;
    byte[] destinationIp;
    byte[] sourceIp;
    byte[] port;

    @Override
    public void sendToLowerLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException {
        // send avec les datagrams packets
        //enregistre dans un fichier .log

    }

    @Override
    public void getFromLowerLayer() {

    }

    @Override
    public void sendToHigherLayer() {

    }

    @Override
    public void getFromHigherLayer(byte[] buffer, byte[] ipDestination, int port) throws IOException {
        this.homemadePacket = buffer;
        String bufferStr = new String(homemadePacket);
        String newPacketHeader = bufferStr.split(";")[0] + ",";
        System.out.println(newPacketHeader);
        this.homemadePacketHeader = newPacketHeader.getBytes();
        this.homemadePacketBody = bufferStr.split(";")[1].getBytes();
        addCRC();

    }

    public void addCRC(){
        CRC32 crc = new CRC32();
        //Check si ca split au point virgule en lincluant ou non
        //Doit ajouter la virgule avant de calculer le CRC
        //Recalcule le size du packetWithCRC dependamment du resultat
        crc.update(this.homemadePacket);
        byte[] packetWithCRC = new byte[this.homemadePacket.length];
        ByteBuffer packetBuffer = ByteBuffer.wrap(packetWithCRC);
        packetBuffer.put(this.homemadePacketHeader).put(this.homemadePacketBody);
        this.packetWithCRC = packetWithCRC;
        System.out.println(Arrays.toString(packetWithCRC));

    }
}
*/