import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;


public class BitTorrentClient {
	
	private int PORT_NUMBER;
	private String IP;
	private String PEER_ID;
	
	private TorrentFileHandler torrentFileHandler;
	private TorrentFile torrentFile;
	private static boolean COMPACT = true; 
	private int uploaded = 0;
	private int downloaded = 0;
	private int interval = 0;
	private int left = 0;
	private HashMap<String, Integer> peerList;
	
	
	public BitTorrentClient(int port, String torrentPath) throws SocketException{
		IP = Utils.getIP();
		this.PORT_NUMBER = port;
		this.PEER_ID = Utils.calculatePeerID();
		
		torrentFileHandler = new TorrentFileHandler();
		torrentFile = torrentFileHandler.openTorrentFile(torrentPath);
		
		if (torrentFile == null){
			System.err.println("Error: There was a problem when unencoding the torrent file.");
		}
		
		left = torrentFile.file_length - downloaded;
	}
	
	public int contactTracker() throws MalformedURLException, IOException{
		
		String charset = "UTF-8";
		String tracker_url = torrentFile.tracker_url;
		String info_hash = torrentFile.info_hash_as_url;
		
		String query = "";
		query = String
				.format("info_hash=%s&peer_id=%s&port=%s&uploaded=%s&downloaded=%s&left=%s&compact=%s",
						info_hash, PEER_ID, Integer.toString(PORT_NUMBER),
						Integer.toString(uploaded),
						Integer.toString(downloaded), Integer.toString(left),
						(COMPACT) ? "1" : "0");

		URLConnection connection = new URL(tracker_url + "?" + query)
				.openConnection();
		connection.setRequestProperty("Accept-Charset", charset);
		InputStream response = connection.getInputStream(); 
		byte[] byteStream = IOUtils.toByteArray(response);

		TrackerResponse trackerResponse = Utils.handleTrackerResponse(byteStream);
		Utils.dumpPeerList(trackerResponse.getPeerList());
		this.peerList = trackerResponse.getPeerList();
		
		return 0;
	}

}
