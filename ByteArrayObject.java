import java.io.*;

/*
 * Author: Ken Wu
 *
 * 2009-10-04
 * 
 */

public class ByteArrayObject implements Serializable {

	public byte[] b;

	public ByteArrayObject (int i) {
		b = new byte[i];
	}
}