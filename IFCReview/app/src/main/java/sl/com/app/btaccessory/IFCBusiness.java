package sl.com.app.btaccessory;

/**
 * Created by shenlong on 9/23/2015.
 */
public class IFCBusiness {
    /**
     * Convert hex string to byte array
     * @param hexCode : like "CA 04 00 0C A5 A5 A5 A5 94 CA 06 00 0C 00 00 00 00 BA 01 BB"
     * @return byte array to send via bluetooth
     */
    public static byte [] getData(String hexCode)
    {
        String[] hexs = hexCode.split(" ");
        int count = hexs.length;
        byte[] bytes = new byte[count];
        for(int i = 0 ; i < count; i++)
        {
            int hex = Integer.decode("0x" + hexs[i]);
            bytes[i] = (byte)hex;
        }
        return bytes;
    }
}
