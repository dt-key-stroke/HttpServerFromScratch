import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.zip.GZIPOutputStream;

public class Compression {
    private static final int BUFFER_SIZE = 512;

    public static String toHex(byte[] arg) throws UnsupportedEncodingException {
        return String.format("%040x", new BigInteger(1, arg));
    }


    public static String gzip(String payload) throws IOException {
        var uncompressedData = payload.getBytes();
        ByteArrayOutputStream bos = null;
        GZIPOutputStream gzipOS = null;
        try {
            bos = new ByteArrayOutputStream(uncompressedData.length);
            gzipOS = new GZIPOutputStream(bos);
            gzipOS.write(uncompressedData);
            gzipOS.close();
            return toHex(bos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert gzipOS != null;
                gzipOS.close();
                bos.close();
            }
            catch (Exception ignored) {
            }
        }
        return "";
    }

    public static void main() throws IOException {
        var res = Compression.gzip("paaineapple");
        System.out.println(res);
    }

}
