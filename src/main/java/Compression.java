import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class Compression {
    private static final int BUFFER_SIZE = 512;

    public static String gzip(String payload) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(payload.getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOs = new GZIPOutputStream(os)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) > -1) {
                gzipOs.write(buffer, 0, bytesRead);
            }
        }
        String compressed = os.toString();
        return compressed;
    }

    public static void test() throws IOException {
        var res = Compression.gzip("pineapple");
        System.out.println(res);
    }

}
