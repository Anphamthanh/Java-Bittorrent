import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Formatter;
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
		Bencoder bencoder = new Bencoder();
		HashMap<String, Object> reponseHashMap = new HashMap<String, Object>();
		String rawPeerList = "";
		HashMap<String, Integer> peerList = new HashMap<String, Integer>();

		reponseHashMap = bencoder.unbencodeDictionary(responseStream);
		complete = (int) reponseHashMap.get("complete");
		incomplete = (int) reponseHashMap.get("incomplete");
		interval = (int) reponseHashMap.get("interval");
		rawPeerList = (String) reponseHashMap.get("peers");
		peerList = parsePeerList(rawPeerList);

		TrackerResponse response = new TrackerResponse(complete, incomplete,
				interval, peerList);

		return response;
	}

	/**
	 * Parse the raw peer list to a hashmap<ip, port>
	 * 
	 * @param rawPeerList
	 * @return
	 */
	private static HashMap<String, Integer> parsePeerList(String rawPeerList) {
		HashMap<String, Integer> retHashMap = new HashMap<String, Integer>();
		char[] charStream = rawPeerList.toCharArray();
		int i = 0;

		while (i < rawPeerList.length()) {
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
	private static int charToUnsignedInt(char c) {
		return (int) ((c) & 0xFF);
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
	private static int parsePort(char high, char low) {
		int retVal = 0;
		retVal = retVal | (int) (((high & 0xff) << 8) | (low & 0xff));
		return retVal;
	}

	/**
	 * Print out peer list
	 * 
	 * @param peerList
	 */
	public static void dumpPeerList(HashMap<String, Integer> peerList) {
		Set<String> keys = peerList.keySet();

		System.out.println("IP:port");

		for (String itr : keys) {
			System.out.print(itr + ":" + peerList.get(itr) + "\n");
		}
	}

	/**
	 * Create an array of peer list
	 * 
	 * @param peerList
	 */
	public static ArrayList<Peer> getPeerArray(HashMap<String, Integer> peerList) {
		Set<String> keys = peerList.keySet();

		ArrayList<Peer> peer_list = new ArrayList<Peer>();

		for (String itr : keys) {
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
	 * 
	 * @return
	 * @throws SocketException
	 */
	public static String getIP() throws SocketException {
		String ip = "";
		Enumeration<NetworkInterface> e = NetworkInterface
				.getNetworkInterfaces();

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
	 * 
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

		for (int i = 0; i < twelveBytesHash.length; i++) {
			peerID = Byte.toString(twelveBytesHash[i]);
		}

		return peerID;
	}

	/**
	 * Calculate PeerID
	 * 
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
	public static String byteArrayToURLString(byte in[]) {
		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0)
			return null;

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };
		StringBuffer out = new StringBuffer(in.length * 2);

		while (i < in.length) {
			// First check to see if we need ASCII or HEX
			if ((in[i] >= '0' && in[i] <= '9')
					|| (in[i] >= 'a' && in[i] <= 'z')
					|| (in[i] >= 'A' && in[i] <= 'Z') || in[i] == '$'
					|| in[i] == '-' || in[i] == '_' || in[i] == '.'
					|| in[i] == '+' || in[i] == '!') {
				out.append((char) in[i]);
				i++;
			} else {
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
	public static String byteArrayToByteString(byte in[]) {
		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0)
			return null;

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };
		StringBuffer out = new StringBuffer(in.length * 2);

		while (i < in.length) {
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

	public static byte[] generateSHA1Hash(byte[] bytes) {
		try {
			byte[] hash = new byte[20];
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			hash = sha.digest(bytes);

			return hash;
		} catch (NoSuchAlgorithmException e) {
			System.err
					.println("Error: [TorrentFileHandler.java] \"SHA-1\" is not a valid algorithm name.");
			System.exit(1);
		}
		return null;
	}

	public static void sleep(int timer) {
		try {
			Thread.sleep(timer);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Write a byte array to the given file. Writing binary data is
	 * significantly simpler than reading it.
	 */
	public static void write(byte[] aInput, String aOutputFileName) {
		log("Writing binary file...");
		try {
			OutputStream output = null;
			try {
				output = new BufferedOutputStream(new FileOutputStream(
						aOutputFileName));
				output.write(aInput);
			} finally {
				output.close();
			}
		} catch (FileNotFoundException ex) {
			log("File not found.");
		} catch (IOException ex) {
			log(ex);
		}
	}
	
	public static void appendToFile(byte[] data, String filename) throws FileNotFoundException, IOException{
		try (FileOutputStream output = new FileOutputStream(filename, true)) {
		    output.write(data);
		}
	}
	
	public static byte[] read_file(String filename) {
		File file = new File(filename);
	    byte[] fileData = new byte[(int) file.length()];
	    
	    try {
		    DataInputStream dis = new DataInputStream(new FileInputStream(file));
			dis.readFully(fileData);
		    dis.close();
		} catch (IOException e) {
			System.out.println("Exception occurs while reading file " + filename);
			return new byte[1];
		}
	    
	    return fileData;
	}

	static void log(Object aThing) {
		System.out.println(String.valueOf(aThing));
	}
	
	public static String get_piece_SHA1(byte[] piece) throws NoSuchAlgorithmException{
	    MessageDigest md = MessageDigest.getInstance("SHA-1"); 
	    return byteArray2Hex(md.digest(piece));
	}

	private static String byteArray2Hex(final byte[] hash) {
	    Formatter formatter = new Formatter();
	    for (byte b : hash) {
	        formatter.format("%02x", b);
	    }
	    String result = formatter.toString();
	    formatter.close();
	    return result;
	}
	
	/**
	 * Check piece validity
	 */
	public static boolean check_piece(TorrentFile torrent_file, int piece_index, String temp_file) {
		String data_hash = (String) torrent_file.piece_hash_values_as_hex.elementAt(piece_index);
	    MessageDigest md;
	    String piece_hash = "";
		try {
			md = MessageDigest.getInstance("SHA-1");
		    piece_hash = byteArray2Hex(md.digest(read_file(temp_file)));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception occurs while checking piece!");
			return false;
		} 
		System.out.println("Data " + data_hash);
		System.out.println("Piece " + piece_hash);
		return data_hash == piece_hash;
	}

}
