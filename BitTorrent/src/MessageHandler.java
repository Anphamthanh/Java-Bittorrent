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
			System.out.println("Exception occured while trying to get_response!");
			return new Message(0, (byte) 0, new byte[1]);
		}
		
		ByteBuffer length_wrapped = ByteBuffer.wrap(msg_length);
		int length = length_wrapped.getInt();
		
		if (length == 0) {
			return new Message(0, (byte) 0, new byte[1]);
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
		
		byte[] byte_array = new byte[length-1];
		
		try
		{
			input_stream.readFully(byte_array);
		} 
		catch (Exception ignore) {	
			System.out.println("Exception occured while trying to get_response!");
			return new Message(0, (byte) 0, new byte[1]);
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
			System.out.println("Exception occured while trying to get_fixed_length_response!");
			return new byte[1];
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
			System.out.println("Exception occured while trying to send_handshake!");
			return new byte[1];
		}

		byte[] handshake = baos.toByteArray();

		try {
			output_stream.write(handshake);
			output_stream.flush();
		}
		catch(Exception ex) {
			System.out.println("Exception occured while trying to send_handshake!");
			return new byte[1];
		} 
		
		return get_fixed_length_response(input_stream, 1 + "BitTorrent protocol".getBytes().length);
	}

	public static Message send_unchoke(DataOutputStream output_stream, DataInputStream input_stream) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		try {

			w.writeInt(1);
			w.writeByte(1);
			w.flush();

		} catch (IOException e) {
			System.out.println("Exception occured while trying to send_unchoke!");
			return new Message(0, (byte) 0, new byte[1]);
		}

		byte[] unchoke = baos.toByteArray();

		try {
			output_stream.write(unchoke);
			output_stream.flush();
		}
		catch(Exception ex) {
			System.out.println("Exception occured while trying to send_unchoke!");
			return new Message(0, (byte) 0, new byte[1]);
		} 

		return new Message(0, (byte) 0, new byte[1]);
	}
	
	public static Message send_interested(DataOutputStream output_stream, DataInputStream input_stream) {
		
		System.out.println("Sending interested message");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		try {

			w.write(new byte[3]);
			w.writeByte(1);
			w.write(2);
			w.flush();

		} catch (IOException e) {
			System.out.println("Exception occured while trying to send_interested!");
			return new Message(0, (byte) 0, new byte[1]);
		}

		byte[] interested = baos.toByteArray();

		try {
			output_stream.write(interested);
			output_stream.flush();
		}
		catch(Exception ex) {
			System.out.println("Exception occured while trying to send_interested!");
			return new Message(0, (byte) 0, new byte[1]);
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
			System.out.println("Exception occured while trying to send_request!");
			return new Message(0, (byte) 0, new byte[1]);
		}

		byte[] request = baos.toByteArray();

		try {
			output_stream.write(request);
			output_stream.flush();
		}
		catch(Exception ex) {
			System.out.println("Exception occured while trying to send_request!");
			return new Message(0, (byte) 0, new byte[1]);
		} 

		response = get_response(input_stream);

		return response;
	}
	
	public static boolean is_unchoke(Message msg) {		
	
		byte unchoke_id = 1;
		
		return msg.id == unchoke_id;
	}
	
	public static boolean is_choke(Message msg) {		
		
		byte choke_id = 0;
		
		return msg.id == choke_id;
	}
	
	public static boolean is_have(Message msg) {		
		
		byte have_id = 4;
		
		return msg.id == have_id;
	}
	
	

	public static boolean is_handshake(byte[] msg, TorrentFile torrentFile, String PEER_ID) {		

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		

		try {

			w.writeByte(19);
			w.write("BitTorrent protocol".getBytes());
//			w.write(new byte[8]);
//			w.write(torrentFile.info_hash_as_binary);//TODO need to verify hash before accepting connection
			w.flush();

		} catch (IOException e) {
			System.out.println("Exception occured while trying to check handshake!");
			return false;
		}

		byte[] handshake = baos.toByteArray();

		return Arrays.equals(msg, handshake);
	}
	
	public static boolean is_bitfield(Message msg) {		
		
		byte bitfield_id = 5;
		
		return msg.id == bitfield_id;
	}
}