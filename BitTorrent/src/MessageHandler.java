import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MessageHandler {
	
	public static boolean is_piece_available(byte[] bitfield, int piece_index) {
		
		int byte_index = piece_index/8;
		byte data = bitfield[byte_index];
		int mask = 1<<(7 - (piece_index % 8));
		
		return ((data & mask) != 0);
	}
	public static Message get_response(DataInputStream input_stream) {

		byte[] msg_length = new byte[4];
		try
		{
			input_stream.readFully(msg_length);
		} 
		catch (Exception ignore) {	
		}
		
		ByteBuffer length_wrapped = ByteBuffer.wrap(msg_length);
		int length = length_wrapped.getInt();
		
		if (length == 0) {
			return null;//receive a keep alive message
		}
		
		byte[] msg_id = new byte[1];
		try
		{
			input_stream.readFully(msg_id);
		} 
		catch (Exception ignore) {	
		}
		
		ByteBuffer id_wrapped = ByteBuffer.wrap(msg_id);
		byte id = id_wrapped.get(0);
		
		byte[] byte_array = new byte[length];
		
		try
		{
			input_stream.readFully(byte_array);
		} 
		catch (Exception ignore) {	
		}
		
		return new Message(length, id, byte_array);
	}
	
	public static byte[] get_fixed_length_response(DataInputStream input_stream, int expected_length) {
		
		byte[] byte_array = new byte[expected_length];
		
		try
		{
			input_stream.readFully(byte_array);
		} 
		catch (Exception ignore) {	
		}
		
		return byte_array;
	}
	
	public static byte[] send_handshake(DataOutputStream output_stream, DataInputStream input_stream,
			TorrentFile torrentFile, String PEER_ID) {
		
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
		}

		byte[] handshake = baos.toByteArray();

		try {
			output_stream.write(handshake);
			output_stream.flush();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		} 
		
		return get_fixed_length_response(input_stream, 49 + "BitTorrent protocol".getBytes().length);
	}
//	
//	public static boolean is_keepalive_stream(byte[] str) {
//		ByteBuffer length_wrapped = ByteBuffer.wrap(str);
//		int length = length_wrapped.getInt();
//		
//		return length == 0;
//	}
	public static Message send_unchoke(DataOutputStream output_stream, DataInputStream input_stream) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		try {

			w.writeInt(1);
			w.writeByte(1);
			w.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] unchoke = baos.toByteArray();

		try {
			output_stream.write(unchoke);
			output_stream.flush();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		} 

		return get_response(input_stream); 
	}
	
	public static Message send_interested(DataOutputStream output_stream, DataInputStream input_stream) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		try {

			w.write(new byte[3]);
			w.writeByte(1);
			w.write(2);
			w.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] interested = baos.toByteArray();

		try {
			output_stream.write(interested);
			output_stream.flush();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		} 

		return get_response(input_stream);
	}
	
	public static Message send_request(DataOutputStream output_stream, DataInputStream input_stream,
			int piece_index, int byte_offset, int block_length_in_bytes) {
		
		Message response = null;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);

		try {

			w.writeInt(13);
			w.writeByte(6);
			w.writeInt(piece_index);
			w.writeInt(byte_offset);
			w.writeInt(block_length_in_bytes);
			w.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] request = baos.toByteArray();

		try {
			output_stream.write(request);
			output_stream.flush();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		} 

		response = get_response(input_stream);

		return response;
	}
	
	public static boolean is_unchoke(Message msg) {		
	
		byte unchoke_id = 1;
		
		return msg.id == unchoke_id;
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
//			w.write(torrentFile.info_hash_as_binary);//TODO need to verify hash before accepting connection
			w.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		byte[] handshake = baos.toByteArray();

		return Arrays.equals(protocol, handshake);
	}
	
	public static boolean is_bitfield(Message msg) {		
		
		byte bitfield_id = 5;
		
		return msg.id == bitfield_id;
	}
}