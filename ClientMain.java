

import java.io.*;
import java.net.*;
import java.math.BigInteger;
import java.util.Iterator;

/*
 * Author: Ken Wu
 *
 * 2009-10-03
 * 
 */

public class ClientMain {
    private static final int QUIT = 0;
    private static final int RESTART = 1;
    private static final int PROCEED = 2;
    
    private static int state = PROCEED;
    
    public static void main(String[] args) {
        Socket ftpSocket = null;
        PrintStream out = null;
        BufferedReader in = null;
        
        try {
            ftpSocket = new Socket("127.0.0.1", 5096);
            out = new PrintStream(ftpSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(ftpSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("ERROR: Don't know about host: hostname");
        } catch (IOException e) {
            System.err.println("ERROR: Couldn't get I/O for the connection to: hostname");
        }
        
        
        if (ftpSocket != null && out != null && in != null) {
            try {
                System.out.println("INFO: ftpSocket opened");
                
                String responseLine = null;
                
                BigInteger N = null;
                BigInteger e = null;
                BigInteger d = null;
                BigInteger C = null;
                String K = "";
                
                String t = null;
                BigInteger x = null;
                BigInteger R = null;
                
                String username = null;
                String password = null;
                String filename = null;
                
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                
                
                
                do // loop still quit or file received
                {
                    state = PROCEED;
                    
                    do //loop still proper username entered or quit
                    {
                        System.out.println("Enter username: ");
                        username = userInput.readLine();
                        
                        System.out.println("INFO: username " + username);
                        
                        if(username.equals("QUIT")) {
                            state = QUIT;
                            out.print("QUIT\r\n");
                            break;
                        }
                        out.print(username + "\r\n");
                        responseLine = in.readLine();
                        
                        System.out.println("INFO: responseLine after username sent " + responseLine);
                    }while(responseLine.startsWith("ERROR:"));
                    
                    
                    if(state != QUIT) //state == PROCEED only, error if state == RESTART
                    {
                        System.out.println("Enter password: ");
                        password = userInput.readLine();
                        
                        System.out.println("INFO: password before pad or check " + password);
                        
                        while(password.length() < 16)
                            password += "\0";
                        
                        if(password.length()>16)
                            password = password.substring(0,16);
                        
                        System.out.println("INFO: password after pad or check " + password);
                        
                        int indexOfComma1 = responseLine.indexOf(',');
                        int indexOfComma2 = responseLine.indexOf(',',indexOfComma1+1);
                        int indexOfComma3 = responseLine.indexOf(',',indexOfComma2+1);
                        
                        N = new BigInteger(responseLine.substring(0,indexOfComma1),16);
                        e = new BigInteger(responseLine.substring(indexOfComma1+1,indexOfComma2),16);
                        K = responseLine.substring(indexOfComma2+1,indexOfComma3).trim();
                        C = new BigInteger(responseLine.substring(indexOfComma3+1),16);
                        
                        System.out.println("INFO: N.toString(16) " + N.toString(16));
                        System.out.println("INFO: e.toString(16) " + e.toString(16));
                        System.out.println("INFO: K " + K);
                        System.out.println("INFO: C.toString(16) " + C.toString(16));
                        
                        System.out.println("INFO: K.getBytes() " + K.getBytes() );
                        
                        System.out.println("k length " + K.length());
                        
                        
                        byte[] temp = new byte[K.length()/2];
                        for(int i = 0; i < K.length(); i+=2) {
                            temp[i/2] = new Integer(Integer.parseInt(K.substring(i,i+2),16)).byteValue();
                            
                            //System.out.println("i = " + i + ", K.substring(i,i+2) = " + K.substring(i,i+2));
                            //System.out.println("Integer.parseInt(K.substring(i,i+2),16)) = " + Integer.parseInt(K.substring(i,i+2),16));
                            //System.out.println("temp[i/2] = " + temp[i/2]);
                            
                        }
                        System.out.println("INFO: temp " + temp);
                        K = K.trim();
                        
                        
                        try {
                            byte[] decrypted = ClientAES.decrypt(password.getBytes(),temp);
                            Print.printArray("d =", decrypted);
                            //d = new BigInteger(decrypted);
                            String dtemp = "";
                            
                            
                            for (int i = 0; i < decrypted.length; i++) {
                                if(i == (decrypted.length - 1)) {
                                    // check if the last byte contains the leading zero
                                    String tt = Print.hex(decrypted[i]);
                                    if(tt.charAt(0) == '0')
                                        dtemp += tt.substring(1);
                                    else
                                        dtemp += tt;
                                } else {
                                    dtemp += Print.hex(decrypted[i]);
                                }
                            }
                            
                            
                            d = new BigInteger(dtemp,16);
                            
                            System.out.println("INFO: dtemp " + dtemp);
                            System.out.println("INFO: d " + d);
                            System.out.println("INFO: d.toString(16) " + d.toString(16));
                            
                        } catch(javax.crypto.BadPaddingException bpe) {
                            System.out.println("Incorrect Password, ERROR: " + bpe);
                            out.print("ERROR: Wrong password\r\n");
                            responseLine = in.readLine();
                            System.out.println("INFO: respeonseLine from wrong password " + responseLine);
                            state = RESTART;
                        }
                        
                        //if password incorrect, set state = RESTART;
                        //parse K to get d using password
                        //d = new BigInteger("ec4b669a69a01a47ea445aaa9c4449769413ef8b4121ad327e96cdcc4364762697d9f2eb0119af1e29b6169817f26eb9fe3a59d1a650365027c40e522847bde4607b6d1d8731a79af5dfcf30b2a763eec740374e83aefee17352c763f09696aba6d5a787ae04b5f4746f47112730c1c82edec636da1d9be9e8228b70d53ecd94cc7384be2f36581dfa3c69ddf937d4548e4e66a6f4ac26579b2be030b2b16ca4dcb50b977be5affa9f411865ed5f2debc85d5c40273c1a63e12d6e13fac498148cba3071409227bfb7b3898b08f46f5b95e839358a3a55e826d7e637b68babfd8d8a938ad3aaeca66a0deea7afda37de79a6d36c967025dbc6063e5a832c4df",16);
                    }
                    
                    if(state != QUIT && state != RESTART) //state = PROCEED only
                    {
                        System.out.println("Enter filename: ");
                        filename = userInput.readLine();
                        
                        System.out.println("INFO: filename before check " + filename);
                        
                        if(filename.length() > 240)
                            filename = filename.substring(0,240);
                        
                        System.out.println("INFO: filename after check " + filename);
                        
                        t = "(" + C.toString(16) + "," + filename + ")";
                        x = new BigInteger(t.getBytes());
                        R = x.modPow(d,N);
                        
                        System.out.println("INFO: t " + t);
                        System.out.println("INFO: x " + x);
                        System.out.println("INFO: R.toString(16) " + R.toString(16));
                        
                        out.print(R.toString(16) + "\r\n");
                        responseLine = in.readLine();
                        
                        System.out.println("INFO: responseLine after filename sent " + responseLine);
                        
                        if(!responseLine.startsWith("ERROR:")) {
                            
                            String yString = responseLine.substring(0,responseLine.indexOf(','));
                            BigInteger y = new BigInteger(yString,16);
                            BigInteger Sx = y.modPow(d,N);
                            String St = new String(Sx.toByteArray());
                            BigInteger S  = new BigInteger(St,16);
                            String Z = responseLine.substring(responseLine.indexOf(',')+1);
                            
                            
                            
                            System.out.println("INFO: yString " + yString);
                            System.out.println("INFO: y " + y);
                            System.out.println("INFO: Sx " + Sx);
                            System.out.println("INFO: St " + St);
                            System.out.println("INFO: S " + S);
                            System.out.println("INFO: Z " + Z);
                            //use S to get Z
                            
                            File outputFile = null;
                            if(filename.indexOf('.') > 0)
                                outputFile = new File(filename.substring(0,filename.indexOf('.')) + "_bar." + filename.substring(filename.indexOf('.')+1));
                            else
                                outputFile = new File(filename + "_bar");
                            FileOutputStream  fileOut = new FileOutputStream(outputFile);
                            
                            //while( (((double)Z.length())/ 16.0) != (Z.length()/16) )
                            //Z = "\0" + Z;
                            
                            System.out.println("INFO: outputFile " + outputFile.getPath());
                            
                            try {
                                //Now i am going to convert back the string to bytes myself
                                byte[] finalZbytes = new byte[ (int)(Math.ceil(Z.length()/2.0)) ];
                                for(int i = 0; i < Z.length(); i+=2) {
                                    int end = i+2;
                                    if(end == Z.length())
                                        end = i+1;
                                    if(end == Z.length())
                                        break;
                                    finalZbytes[i/2] = new Integer(Integer.parseInt(Z.substring(i,end),16)).byteValue();
                                    //System.out.println("client received " + i + "'th byte");
                                    //System.out.println("i = " + i + ", K.substring(i,i+2) = " + K.substring(i,i+2));
                                    //System.out.println("Integer.parseInt(K.substring(i,i+2),16)) = " + Integer.parseInt(K.substring(i,i+2),16));
                                    //System.out.println("temp[i/2] = " + temp[i/2]);
                                    
                                }
                                System.out.println("OK, client has received " + finalZbytes.length + " bytes");
                                // end
                                System.out.println("Decrypt a file :  St.byte = " + St.getBytes().length + ", file.byte = " + finalZbytes.length);
                                fileOut.write(ClientAES.decryptWithPadding(St.getBytes(), finalZbytes));
                            } catch(Exception eDecrypt) {
                                //System.out.println("Unable to decrypt File, closing client");
				//System.out.println(eDecrypt);
                            }
                            
                        } else {
                            System.out.println("ERROR: Filename Error, restarting");
                            state = RESTART;
                        }
                    }
                }while(state == RESTART); // End loop if username enter state == QUIT  or file received therefore state  == PROCEED
                
                
                
                out.close();
                in.close();
                ftpSocket.close();
            } catch (UnknownHostException e) {
                System.err.println("Trying to connect to unknown host: " + e);
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }
}
