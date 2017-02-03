import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class logger {
    String data;
    BufferedWriter writer = null;

    public logger(String stackTrace) {
        this.data = stackTrace;
    }

    public void writelog() {
        try {
            writer = new BufferedWriter(new FileWriter("error_log.txt", true));
            writer.write(data + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
