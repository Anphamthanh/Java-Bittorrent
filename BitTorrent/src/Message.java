import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class Message {
	
	public static boolean is_unchoke(byte[] msg) {		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);

		try {
			
			w.writeInt(1);
			w.writeByte(1);
			w.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		byte[] unchoke = baos.toByteArray();
		
		return Arrays.equals(msg, unchoke);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
