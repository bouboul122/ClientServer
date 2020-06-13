import java.io.*;

public class ApplicationLayer implements Layer{

    @Override
    public void sendToLowerLayer() {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("absolutePath"))) {
            String line = bufferedReader.readLine();
            while (line != null) {
                System.out.println(line);
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        @Override
    public void getFromLowerLayer() {

            try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("absolutePath"))) {
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
}
