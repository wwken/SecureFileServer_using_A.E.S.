import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;

/*
 * Author: Ken Wu
 *
 * 2009-10-03
 * 
 */

public class AESKGenerator {
    
    /**
     * Turns array of bytes into string
     *
     * @param buf	Array of bytes to convert to hex string
     * @return	Generated hex string
     */
    public static String asHex(byte buf[]) {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        int i;
        
        for (i = 0; i < buf.length; i++) {
            if (((int) buf[i] & 0xff) < 0x10)
                strbuf.append("0");
            
            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
        }
        
        return strbuf.toString();
    }
    
    
    public static byte[] encrypt(byte[] password, byte[] d) {
        byte[] encrypted = null;
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(password, "AES");
            // Instantiate the cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encrypted = cipher.doFinal(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }
    
    public static byte[] decrypt(byte[] password, byte[] k) {
        byte[] original = null;
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(password, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            original = cipher.doFinal(k);
            //String originalString = new String(original);
            //System.out.println("Original string: " + originalString + " " + asHex(original));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return original;
    }
    
    public static byte[] getKStringToKBytes (String k) {
        String D = k;
        byte [] inD = null;
	System.out.println("In processing the decrpyt, k = " + k);
        int counter = 0;
            // first caculate how many bytes i needed for this d
            for(int i=0; i<D.length(); i++, i++) {
                counter++;
            }
            byte [] tmpb = new byte[counter]; // initialize that many slots for bytes
            for(int i=0, index=0; i<D.length(); i++, i++, index++) {
                int j = i+1;
                if(j < D.length()) { // still within the range
                    String s = D.substring(i, j+1);
                    tmpb[index] = (new Integer( Integer.parseInt(s, 16) )).byteValue();
                    //System.out.println("hex : " + s + ", int in 16 : " + (new Integer( Integer.parseInt(s, 16) )).intValue() );
                } else {
                    String s = D.substring(i);
                    tmpb[index] = (new Integer( Integer.parseInt(s, 16) )).byteValue();
                    //System.out.println("hex : " + s + ", int in 16 : " + (new Integer( Integer.parseInt(s, 16) )).intValue() );
                }
            }
            
            // Now i am prependint the tmpb to be of multiple 16 bytes
            int dSize = ((int)(Math.ceil( ((double)counter) / 16.0 ))) * 16;
            inD = new byte[dSize]; // initialize that many slots for bytes
            System.out.println("before unpadded size = " + counter + ", now padded size = " + dSize);
            int d = dSize - counter; int index = 0;
            for(; d>0; d--, index++) {
                inD[index] = 0;
            }
            for(int i=0; i<counter; i++, index++){
                inD[index] = tmpb[i];
            }
            return inD;
    }
}
