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

public class KGenerator {
    
    private String password = "hehe"; // it is read from the user
    
    private String passwordFile = "password";
    
    private String inputFile = "in";
    private String inputFile_D = "in_D";

    private String sfsFile = "rob/.sfs";    
    
    private String N;
    private String e;
    private String D;
    
    public KGenerator() {
    }
    
    public void setPassword(String p) {
        password = p;
    }
    
    public byte[] getPasswordBytes() {
        String p = password;
        while(p.length() < 16)
            p = p + "\0";
        
        p = p.substring(0, 16);
        return p.getBytes();
    }
    
    public String getD() {
        return D;
    }
    
    
    // it will return the k to the calling function as well
    public byte[] generateSFSFile() {
        // first read from a file first
        byte [] encrypted = null;
        try {
            
            // first write the password to the file first
            BufferedWriter passwordWriter = new BufferedWriter(new FileWriter(passwordFile));
            passwordWriter.write(password, 0, password.length());
            passwordWriter.close();
            
            BufferedReader in_All = new BufferedReader(new FileReader(inputFile));
            N = in_All.readLine();
            e = in_All.readLine();
            D = in_All.readLine();
            
            // as soon as i receive d, i write it back to the file for the succequent library calls
            BufferedWriter dWriter = new BufferedWriter(new FileWriter(inputFile_D));
            dWriter.write(D, 0, D.length());
            dWriter.close();
            
            
            
            //System.out.println(N);
            //System.out.println(e);
            //System.out.println(d);
            
            byte[] inD = AESKGenerator.getKStringToKBytes(D);
            
            // Get the KeyGenerator
            byte[] passwordBytes = getPasswordBytes();
            encrypted = AESKGenerator.encrypt(passwordBytes, inD);
            
            
            System.out.println("encrypted string: " + asHex(encrypted));
            
            // Now start generate a K = AES (password, D);
            System.out.println("------------------ Input file ------------------------");
            //GetBytes getInput = new GetBytes(inputFile_D, 16);
            //byte[] in = getInput.getBytes();
            
            Print.printArray("D       :     ", inD);
            Print.printArray("Password:           ", passwordBytes);
            
            System.out.println("------------------ Output file ------------------------");
            Print.printArray("K:    ", encrypted);
            
            
            // now cipherout is actually K, represent in byte[]
            // OK, now i am going to write it back to the .sfs file
            BufferedWriter sfsWriter = new BufferedWriter(new FileWriter(sfsFile));
            
            // write back N
            sfsWriter.write(N, 0, N.length());
            sfsWriter.write("\n", 0, 1);
            sfsWriter.write(e, 0, e.length());
            sfsWriter.write("\n", 0, 1);
            String tmp = "";
            for(int i=0; i<encrypted.length; i++){
                tmp = Print.hex(encrypted[i]);
                //System.out.println(tmp);
                sfsWriter.write(tmp, 0, 2); // writing two characters
            }
            sfsWriter.close();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }
    
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
    
    
    
    public static void main(String[] args) {
        try {
            KGenerator kg = new KGenerator();
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Please enter the server password : ");
            String userpass = userInput.readLine();
            kg.setPassword(userpass);
            
            byte[] enctyped = kg.generateSFSFile();
            
            String K = "HELLO";
            /*
            // debug to see michael part
            byte[] temp = new byte[K.length()/2];
            for(int i = 0; i < K.length(); i+=2) {
                temp[i/2] = new Integer(Integer.parseInt(K.substring(i,i+2),16)).byteValue();
                
                System.out.println("i = " + i + ", K.substring(i,i+2) = " + K.substring(i,i+2));
                System.out.println("Integer.parseInt(K.substring(i,i+2),16)) = " + Integer.parseInt(K.substring(i,i+2),16));
                System.out.println("temp[i/2] = " + temp[i/2]);
                
            }
            */
            
            System.out.println(" In Main debugging ...");
            // debug, to see if the encrypted go back to the decrypted one
            byte [] decrypted = AESKGenerator.decrypt(kg.getPasswordBytes(), enctyped);
            
            Print.printArray("K       :     ", enctyped);
            Print.printArray("D       :     ", decrypted);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
}
