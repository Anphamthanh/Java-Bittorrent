import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class MessageHandler {
	
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
		
		int protocol_length = 1 + "BitTorrent protocol".getBytes().length;
		byte[] protocol = new byte[protocol_length];
		System.arraycopy(msg, 0, protocol, 0, protocol_length);

		try {

			w.writeByte(19);
			w.write("BitTorrent protocol".getBytes());
//			w.write(new byte[8]);
//			w.write(torrentFile.info_hash_as_binary);
			w.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		byte[] handshake = baos.toByteArray();

		return Arrays.equals(protocol, handshake);
	}
	
	public static Message process_input_stream(byte[] input) {
		
		int parser_index = 0;
		
		byte[] msg_length_byte = new byte[4];
		System.arraycopy(input, parser_index, msg_length_byte, 0, 4);
		ByteBuffer wrapped = ByteBuffer.wrap(msg_length_byte);
		int msg_length = wrapped.getInt();
		parser_index += 4;

		byte[] msg = new byte[msg_length];
		System.arraycopy(input, parser_index, msg, 0, msg_length);
		return new Message(msg_length, msg);
	}
}