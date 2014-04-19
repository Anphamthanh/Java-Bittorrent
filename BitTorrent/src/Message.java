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
	
	public static boolean is_handshake(byte[] msg, TorrentFile torrentFile, String PEER_ID) {		

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);

		try {

			w.writeByte(19);
			w.write("BitTorrent protocol".getBytes());
			w.write(new byte[8]);
			w.write(torrentFile.info_hash_as_binary);
			w.writeBytes(PEER_ID);
			w.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		byte[] handshake = baos.toByteArray();

		return Arrays.equals(msg, handshake);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
