import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class MessageHandler {
	
	public static boolean is_unchoked(byte[] msg) {
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			outputStream.write(new byte[3]);
			outputStream.write(1);
			outputStream.write(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] unchoked = outputStream.toByteArray();
		
		return Arrays.equals(unchoked, msg);
	}
}