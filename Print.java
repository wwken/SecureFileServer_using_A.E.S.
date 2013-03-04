// Print: print arrays of bytes

import java.io.*;
/*
 * Author: Ken Wu
 *
 * 2009-10-03
 * 
 */

public class Print {
   private static final int Nb = 4;
   private static String[] dig = {"0","1","2","3","4","5","6","7",
											 "8","9","a","b","c","d","e","f"};


	static FileOutputStream out;
	static ObjectOutputStream s;

	static FileInputStream in;
	static ObjectInputStream sin;



	public static void initWrite (){
		try {
			 out = new FileOutputStream("ciphertext.txt");
			  s = new ObjectOutputStream(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void initRead() {
		try {
			 in = new FileInputStream("ciphertext.txt");
			 sin = new ObjectInputStream(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

   // hex: print a byte as two hex digits
   public static String hex(byte a) {
      return dig[(a & 0xff) >> 4] + dig[a & 0x0f];
   }

   public static void printArray(String name, byte[] a) {
      System.out.print(name + " ");
      for (int i = 0; i < a.length; i++) {
         System.out.print(hex(a[i]) + " ");
			//Print.writeArrayToFile(a[i]);
		}
      System.out.println();
   }

   public static void printArray(String name, byte[][] s) {
      System.out.print(name + " ");
      for (int c = 0; c < Nb; c++)
         for (int r = 0; r < 4; r++) {
            System.out.print(hex(s[r][c]) + " ");
				//Print.writeArrayToFile(s[r][c]);
			}
      System.out.println();
   }


	public static void writeByteObject(byte[] b) {
		try {
			if(out == null)
				initWrite();

			ByteArrayObject bao = new ByteArrayObject(b.length);
			for (int i = 0; i < b.length; i++) {
				bao.b[i] = b[i];
			}
			s.writeObject(bao);
			s.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static byte[] readByteObject() {
		byte[] bb = null;
		try {
			if(in == null)
				initRead();

			ByteArrayObject bao = (ByteArrayObject)sin.readObject();
			bb = new byte [bao.b.length];
			for (int i = 0; i < bao.b.length; i++) {
					//System.out.println(" reading");
				bb[i] = bao.b[i];
			}



		} catch (Exception e) {
			e.printStackTrace();
		}
		return bb;

	}

	public static String intToBinary (int i) {
		int d = 1;
		String output = "";
		while (d > 0) {
			if(d%2 > 0)
				output = "1" + output;
			else
				output = "0" + output;

				d = d / 2;
		}
		return output;
	}

}
