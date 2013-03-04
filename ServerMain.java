import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.SecureRandom;
import java.util.Date;

/*
 * Author: Ken Wu
 *
 * 2009-10-03
 * 
 */

public class ServerMain {
    private static final int WAITINGFORUSERNAME = 0;
    private static final int WAITINGFORFILENAME = 1;
    private static final int FILESENT = 2;
    
    private static int state;
    
    
    
    private static String username;
    
    private static BigInteger N;
    private static BigInteger e;
    private static BigInteger d;
    
    private static String K;
    
    private static SecureRandom secRand;
    private static BigInteger C;
    
    private static void initialize() {
        System.out.println("initialize() called");
        state = WAITINGFORUSERNAME;
        
        username = "";
        
        N = null;
        e = null;
        d = null;
        
        K = "";
        
        secRand = new SecureRandom();
        C = null;
    }
    
    public static void main(String args[]) {
        initialize();
        
        /* Testing RSA
        String s = "123";
        System.out.println("s = " + s);
        BigInteger M = new BigInteger(s.getBytes());
        System.out.println("M = " + M.toString(16));
        BigInteger C = M.modPow(e,N);
        System.out.println("C = " + C.toString(16));
        BigInteger D = C.modPow(d,N);
        System.out.println("D = " + D.toString(16));
        String S = new String(D.toByteArray());
        System.out.println("S = " + S);
         */
        
        
        ServerSocket ftpServer = null;
        String inputString = null;
        BufferedReader in;
        PrintStream out;
        Socket clientSocket = null;
        
        try {
            ftpServer = new ServerSocket(5096);
        } catch (IOException e) {
            System.out.println(e);
        }
        
        while(true) {
            
            try {
                clientSocket = ftpServer.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintStream(clientSocket.getOutputStream());
                
                System.out.println("clientSocket accepted");
                
                while (state != FILESENT) {
                    inputString = in.readLine();
                    
                    System.out.println("length = " + inputString.length() + ", inputString = " + inputString);
                    
                    if(inputString.indexOf("QUIT") != -1) {
                        break;
                    }
                    
                    
                    processInput(inputString, out);
                    
                    
                }
                
                in.close();
                out.close();
                clientSocket.close();
                
            } catch (IOException e) {
                System.out.println(e);
            }
            
            initialize();
        }
    }
    
    private static boolean processUsername(String inputString) {
        File userFile = new File(inputString + "/.sfs");
        
        System.out.println("userFile" + userFile);
        
        if(userFile.exists()) {
            try {
                FileReader fileIn = new FileReader(userFile);
                BufferedReader reader = new BufferedReader(fileIn);
                String Ntemp = reader.readLine();
                String etemp = reader.readLine();
                String ktemp = reader.readLine();
                
                N = new BigInteger(Ntemp,16);
                e = new BigInteger(etemp,16);
                K = ktemp.trim();
                
                System.out.println("N.toString(16) " + N.toString(16));
                System.out.println("e.toString(16) " + e.toString(16));
                System.out.println("K " + K);
                
                
                
                return true;
            } catch(Exception e) {
                System.out.println(e);
                return false;
            }
        } else
            return false;
    }
    
    
    
    
    private static void processInput(String inputString, PrintStream out) throws IOException {
        String outputString = null;
        
        if (state == WAITINGFORUSERNAME) {
            if(processUsername(inputString) == true) {
                secRand.setSeed(new Date().getTime());
                C = new BigInteger(128, secRand);
                
                System.out.println("C.toString(16) " + C.toString(16));
                
                outputString = N.toString(16) + "," + e.toString(16) + "," + K + "," + C.toString(16) + "\r\n";
                out.print(outputString);
                state = WAITINGFORFILENAME;
            } else {
                outputString = "ERROR: Invalid Username, try again\r\n";
                initialize();
                out.print(outputString);
            }
        } else if (state == WAITINGFORFILENAME) {
            try {
                BigInteger R = new BigInteger(inputString,16);
                String t = new String(R.modPow(e,N).toByteArray());
                System.out.println("R.toString(16) " + R.toString(16));
                System.out.println("t " + t);
                
                System.out.println("check string = " + "(" + C + ",");
                if(t.startsWith("(" + C.toString(16) + ",")) {
                    secRand.setSeed(new Date().getTime());
                    
                    BigInteger S = new BigInteger(128, secRand);
                    System.out.println("S.toString(16) " + S.toString(16));
                    String s = S.toString(16);
                    System.out.println("s before pad " + s);
                    
                    while(s.length() < 16)
                        s += "\0";
                    
                    if(s.length() > 16)
                        s = s.substring(0,16);
                    
                    System.out.println("s after pad " + s);
                    
                    BigInteger x = new BigInteger(s.getBytes());
                    System.out.println("X " + x);
                    BigInteger y = x.modPow(e,N);
                    System.out.println("y.toString(16) " + y.toString(16));
                    
                    
                    String filename = t.substring( t.indexOf(',')+1 , t.indexOf(')') );
                    
                    System.out.println("filename " + filename);
                    try {
                        File requestedFile = new File(filename);
                        FileInputStream fileIn = new FileInputStream(requestedFile);
                        int bytesRead;
                        byte[] file = new byte[(int)requestedFile.length()];
                        bytesRead = fileIn.read(file);
                        
                        System.out.println("bytesRead " + bytesRead);
                        
                        System.out.println("Encrypt a file, input:  s.byte = " + s.getBytes().length + ", file.byte = " + file.length);
                        byte[] Z = ServerAES.encrypt(s.getBytes(), file);
                        System.out.println("Encrypt a file, output  z.byte = " + Z.length);
                        outputString = y.toString(16) + ",";
                        state = FILESENT;
                        out.print(outputString);
                        
                        // Edit by ken
                        String tmp = "";
                        for(int i=0; i<Z.length; i++){
                            tmp = Print.hex(Z[i]);
                            //System.out.println(tmp);
                             out.print(tmp); // writing two characters
                        }
                        
                        //out.print(Z);
                        out.print("\r\n");
                        
                        System.out.println("OK, server has send out " + Z.length + " bytes");
                    } catch(FileNotFoundException e) {
                        outputString = "ERROR: File not found, start over\r\n";
                        out.print(outputString);
                        initialize();
                        state = WAITINGFORUSERNAME;
                        
                    }
                } else {
                    outputString = "ERROR: Invalid Filename Format, start over\r\n";
                    out.print(outputString);
                    initialize();
                    state = WAITINGFORUSERNAME;
                    
                }
                
            } catch(NumberFormatException e) {
                System.out.println(e);
                outputString = "ERROR: Invalid Filename Format, start over\r\n";
                out.print(outputString);
                initialize();
                state = WAITINGFORUSERNAME;
            }
            
        }
        
        //System.out.println("outputString = " + outputString);
    }
    
    
}