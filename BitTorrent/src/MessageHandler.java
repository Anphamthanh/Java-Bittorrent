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
		try {
			output_stream.writeByte(19);
			output_stream.write("BitTorrent protocol".getBytes());
			output_stream.write(new byte[8]);
			output_stream.write(torrentFile.info_hash_as_binary);
			output_stream.writeBytes(PEER_ID);	
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		} 
		return get_fixed_length_response(input_stream, 49 + "BitTorrent protocol".getBytes().length);
	}
	
	public static int send_keepalive(DataOutputStream output_stream, TorrentFile torrentFile, String PEER_ID) {
		try {
			output_stream.write(new byte[4]);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return -1;
		} 
		return 0;
	}
	
	public static Message send_unchoke(DataOutputStream output_stream, DataInputStream input_stream) {
		try {
			output_stream.writeInt(1);
			output_stream.writeByte(1);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		} 

		return get_response(input_stream); 
	}
	
	public static Message send_interested(DataOutputStream output_stream, DataInputStream input_stream) {
		try {
			output_stream.write(new byte[3]);
			output_stream.writeByte(1);
			output_stream.write(2);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		} 

		return get_response(input_stream);
	}
	
	public static Message send_request(DataOutputStream output_stream, DataInputStream input_stream,
			int piece_index, int byte_offset, int block_length_in_bytes) {
		try {
			output_stream.writeInt(13);			
			output_stream.writeByte(6);
			output_stream.writeInt(piece_index);
			output_stream.writeInt(byte_offset);
			output_stream.writeInt(block_length_in_bytes);
			
			
//			byte[] byte_index = ByteBuffer.allocate(4).putInt(piece_index).array();			
//			output_stream.write(byte_index);
//			
//			byte[] block_offset = ByteBuffer.allocate(4).putInt(byte_offset).array();			
//			output_stream.write(block_offset);			
//			
//			byte[] block_length = ByteBuffer.allocate(4).putInt(block_length_in_bytes).array();			
//			output_stream.write(block_length);
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		} 
		return get_response(input_stream);
	}
	
	public static boolean is_unchoke(Message msg) {		
	
		byte unchoke_id = 1;
		if (msg.id == unchoke_id) {
			return true;
		}
		
		return is_bitfield(msg);
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