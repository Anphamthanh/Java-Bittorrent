import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Sandbox {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		char high = 0xc8;
//		char low = 0xd5;
//		
//		System.out.print(bytesToUnsignedDec(high, low));
	}
	
	private static int bytesToUnsignedDec(char high, char low){
		int retVal = (int)((high<<8) | low);
		return retVal;
	}

}
