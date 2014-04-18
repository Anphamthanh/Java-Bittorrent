import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;

public class Utils {
	
	private static final String CLIENT_ID = "-AP6969-";
	
	@SuppressWarnings("unchecked")
	public static TrackerResponse handleTrackerResponse(byte[] responseStream) {
		
		int complete, incomplete, interval;
		Bencoder bencoder 						= new Bencoder();
		HashMap<String, Object> reponseHashMap 	= new HashMap<String, Object>();
		String rawPeerList	 					= "";
		HashMap<String, Integer> peerList 		= new HashMap<String, Integer>();

		reponseHashMap 	= bencoder.unbencodeDictionary(responseStream);		
		complete 		= (int) reponseHashMap.get("complete");
		incomplete 		= (int) reponseHashMap.get("incomplete");
		interval 		= (int) reponseHashMap.get("interval");
		rawPeerList 	= (String) reponseHashMap.get("peers");
		peerList 		= parsePeerList(rawPeerList);
		
		TrackerResponse response = new TrackerResponse(complete, incomplete,
				interval, peerList);
	
		return response;
	}

	/**
	 * Parse the raw peer list to a hashmap<ip, port>
	 * @param rawPeerList
	 * @return
	 */
	private static HashMap<String, Integer> parsePeerList(String rawPeerList) {
		HashMap<String, Integer> retHashMap = new HashMap<String, Integer>();
		char[] charStream = rawPeerList.toCharArray();
		int i = 0;

		while (i < rawPeerList.length()){
			String ip = "";
			int port = 0;
			ip = String.format("%s.%s.%s.%s",
					charToUnsignedInt(charStream[i + 0]),
					charToUnsignedInt(charStream[i + 1]),
					charToUnsignedInt(charStream[i + 2]),
					charToUnsignedInt(charStream[i + 3]));
			
			port = parsePort(charStream[i + 4], charStream[i + 5]);
			
			retHashMap.put(ip, port);
			i += 6;
		}

		return retHashMap;
	}
	
	/**
	 * 
	 * @param char to be converted
	 * @return
	 */
	private static int charToUnsignedInt(char c){
		return (int)((c) & 0xFF);
	}

	/**
	 * This method used to convert raw port number in bytes to int
	 * 
	 * @param higher
	 *            char
	 * @param lower
	 *            char
	 * @return port number in int from the bytes 
	 */
	private static int parsePort(char high, char low){
		int retVal 	= 0;
		retVal 		= retVal | (int)(((high & 0xff) << 8) | (low & 0xff));
		return retVal;
	}
	
	/**
	 * Print out peer list
	 * @param peerList
	 */
	public static void dumpPeerList(HashMap<String, Integer> peerList){
		Set<String> keys = peerList.keySet();
		
		System.out.println("IP:port");
		
		for(String itr: keys){
			System.out.print(itr + ":" + peerList.get(itr) + "\n");
		}
	}
	
	/**
	 * Create an array of peer list
	 * @param peerList
	 */
	public static ArrayList<Peer> getPeerArray(HashMap<String, Integer> peerList){
		Set<String> keys = peerList.keySet();
		
		ArrayList<Peer> peer_list = new ArrayList<Peer>();
		
		for(String itr: keys){
			peer_list.add(new Peer(itr, peerList.get(itr)));
		}
		
		return peer_list;
	}
	
	private static Pattern pattern;
	private static Matcher matcher;

	private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	/**
	 * Validate ip address with regular expression
	 * 
	 * @param ip
	 *            ip address for validation
	 * @return true valid ip address, false invalid ip address
	 */
	private static boolean validate(final String ip) {
		pattern = Pattern.compile(IPADDRESS_PATTERN);
		matcher = pattern.matcher(ip);
		return matcher.matches();
	}
	
	/**
	 * Compute machine external IP that later used for compute peer ID
	 * @return
	 * @throws SocketException
	 */
	public static String getIP() throws SocketException {
		String ip = "";
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		
		while (e.hasMoreElements()) {
			NetworkInterface n = e.nextElement();
			Enumeration<InetAddress> ee = n.getInetAddresses();

			while (ee.hasMoreElements()) {
				InetAddress i = ee.nextElement();
				ip = i.getHostAddress();

				if (validate(ip)) {
					return ip;
				}
			}
		}
		
		return ip;
	}

	/**
	 * compute Peer Id from ip and port
	 * @param port
	 * @param ip
	 * @return
	 */
	public static String calculatePeerID(int port, String ip) {
		String peerID = "";
		String buffer = port + ip;
		byte[] hash = generateSHA1Hash(stringToBytesASCII(buffer));
		byte[] twelveBytesHash = new byte[12];
		System.arraycopy(hash, 7, twelveBytesHash, 0, 12);
		
		peerID = CLIENT_ID;// + byteArrayToByteString(twelveBytesHash);
		
		for (int i = 0; i < twelveBytesHash.length; i++){
			peerID = Byte.toString(twelveBytesHash[i]);
		}

		return peerID;
	}
	
	/**
	 * Calculate PeerID
	 * @return
	 */
	public static String calculatePeerID() {
		String peerID = CLIENT_ID + RandomStringUtils.randomAlphanumeric(12);
		return peerID;
	}
	
	private static byte[] stringToBytesASCII(String str) {
		char[] buffer = str.toCharArray();
		byte[] b = new byte[buffer.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) buffer[i];
		}
		return b;
	}
	
	/*
	 * Stolen from byteArrayToByteString
	 */
	public static String byteArrayToURLString(byte in[])
	{
		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0)
			return null;

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };
		StringBuffer out = new StringBuffer(in.length * 2);

		while (i < in.length)
		{
			// First check to see if we need ASCII or HEX
			if ((in[i] >= '0' && in[i] <= '9')
					|| (in[i] >= 'a' && in[i] <= 'z')
					|| (in[i] >= 'A' && in[i] <= 'Z') || in[i] == '$'
					|| in[i] == '-' || in[i] == '_' || in[i] == '.'
					|| in[i] == '+' || in[i] == '!')
			{
				out.append((char) in[i]);
				i++;
			}
			else
			{
				out.append('%');
				ch = (byte) (in[i] & 0xF0); // Strip off high nibble
				ch = (byte) (ch >>> 4); // shift the bits down
				ch = (byte) (ch & 0x0F); // must do this is high order bit is
				// on!
				out.append(pseudo[(int) ch]); // convert the nibble to a
				// String Character
				ch = (byte) (in[i] & 0x0F); // Strip off low nibble
				out.append(pseudo[(int) ch]); // convert the nibble to a
				// String Character
				i++;
			}
		}

		String rslt = new String(out);

		return rslt;

	}

	/**
	 * 
	 * Convert a byte[] array to readable string format. This makes the "hex"
	 * readable!
	 * 
	 * @author Jeff Boyle
	 * 
	 * @return result String buffer in String format
	 * 
	 * @param in
	 *            byte[] buffer to convert to string format
	 * 
	 */
	// Taken from http://www.devx.com/tips/Tip/13540
	public static String byteArrayToByteString(byte in[])
	{
		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0)
			return null;

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };
		StringBuffer out = new StringBuffer(in.length * 2);

		while (i < in.length)
		{
			ch = (byte) (in[i] & 0xF0); // Strip off high nibble
			ch = (byte) (ch >>> 4); // shift the bits down
			ch = (byte) (ch & 0x0F); // must do this is high order bit is on!
			out.append(pseudo[(int) ch]); // convert the nibble to a String
			// Character
			ch = (byte) (in[i] & 0x0F); // Strip off low nibble
			out.append(pseudo[(int) ch]); // convert the nibble to a String
			// Character
			i++;
		}

		String rslt = new String(out);

		return rslt;
	}

	public static byte[] generateSHA1Hash(byte[] bytes)
	{
		try
		{
			byte[] hash = new byte[20];
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			hash = sha.digest(bytes);
			
			return hash;
		}
		catch (NoSuchAlgorithmException e)
		{
			System.err
					.println("Error: [TorrentFileHandler.java] \"SHA-1\" is not a valid algorithm name.");
			System.exit(1);
		}
		return null;
	}
	
	public static byte[] get_response(DataInputStream input_stream, String CHARSET) {
		int temp;
		byte[] byte_array = new byte[1];
		
		try
		{
			BufferedInputStream buffer = new BufferedInputStream(input_stream);
			temp = buffer.read(byte_array);

		} 
		catch (Exception ignore) {	
		}
		
		return byte_array;
	}
	
	public static byte[] send_handshake(DataOutputStream output_stream, DataInputStream input_stream,
			TorrentFile torrentFile, String PEER_ID, String CHARSET) {
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
		return get_response(input_stream, CHARSET);
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
	
	public static int send_choke(DataOutputStream output_stream, TorrentFile torrentFile, String PEER_ID) {
		try {
			output_stream.write(new byte[3]);
			output_stream.writeByte(1);
			output_stream.write(0);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return -1;
		} 
		return 0;
	}
	
	public static byte[] send_interested(DataOutputStream output_stream, DataInputStream input_stream,
			TorrentFile torrentFile, String PEER_ID, String CHARSET) {
		try {
			output_stream.write(new byte[3]);
			output_stream.writeByte(1);
			output_stream.write(2);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		} 
		return get_response(input_stream, CHARSET);
	}
	
}
