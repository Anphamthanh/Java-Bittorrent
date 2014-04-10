import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	public TrackerResponse handleTrackerResponse(byte[] responseStream) {
		
		int complete, incomplete, interval;
		Bencoder bencoder 					= new Bencoder();
		HashMap reponseHashMap 				= new HashMap();
		String rawPeerList	 				= "";
		HashMap<String, Integer> peerList 	= new HashMap<String, Integer>();

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
	private HashMap<String, Integer> parsePeerList(String rawPeerList) {
		HashMap<String, Integer> retHashMap = new HashMap<>();
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
	private int charToUnsignedInt(char c){
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
	private int parsePort(char high, char low){
		int retVal 	= 0;
		retVal 		= retVal | (int)(((high & 0xff) << 8) | (low & 0xff));
		return retVal;
	}
	
	/**
	 * Print out peer list
	 * @param peerList
	 */
	public void dumpPeerList(HashMap<String, Integer> peerList){
		Set<String> keys = peerList.keySet();
		
		System.out.println("IP:port");
		
		for(String itr: keys){
			System.out.print(itr + ":" + peerList.get(itr) + "\n");
		}
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
	public String getIP() throws SocketException {
		String ip = "";
		Enumeration e = NetworkInterface.getNetworkInterfaces();
		
		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration ee = n.getInetAddresses();

			while (ee.hasMoreElements()) {
				InetAddress i = (InetAddress) ee.nextElement();
				ip = i.getHostAddress();

				if (validate(ip)) {
					return ip;
				}
			}
		}
		
		return ip;
	}
}
