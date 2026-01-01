
import java.lang.reflect.Array;

public class Misc {

    public static byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;

        byte[] c = (byte[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    } 
    
}
