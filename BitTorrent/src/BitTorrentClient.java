import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;


public class BitTorrentClient {
	
	private int PORT_NUMBER;
	private String IP;
	private String PEER_ID;
	private String CHARSET = "UTF-8";
	
	private TorrentFileHandler torrentFileHandler;
	private TorrentFile torrentFile;
	private static boolean COMPACT = true; 
	private int uploaded = 0;
	private int downloaded = 0;
	private int interval = 0;
	private int left = 0;
	private HashMap<String, Integer> peerList;
	private ArrayList<Peer> contactingPeers;
	
	
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
		
		String charset = this.CHARSET;
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

		this.peerList = trackerResponse.getPeerList();
		
		this.contactingPeers = Utils.getPeerArray(trackerResponse.getPeerList());
		
		return 0;
	}
	
	public int contactPeers() throws MalformedURLException, IOException {
		
		if (this.contactingPeers == null) {
			System.out.println("There is no peer to work with!");
			return -1;
		}
		
		for (Peer peer : this.contactingPeers) {	
			System.out.println("Having Peer at IP " + peer.getIP() + " Port " + peer.getPort());
		}
		
		Peer test_peer = this.contactingPeers.get(9);
		
		try {

			System.out.println("Contacting IP " + test_peer.getIP() + " Port " + test_peer.getPort());

			Socket socket = new Socket(test_peer.getIP(), test_peer.getPort()); 

			System.out.println("Just connected to " + socket.getRemoteSocketAddress()); 

			DataOutputStream output_stream = new DataOutputStream(socket.getOutputStream());
			DataInputStream input_stream = new DataInputStream(socket.getInputStream());
			byte[] response = null; 	        
	        
	        response = Utils.send_handshake(output_stream, input_stream, torrentFile, this.PEER_ID);
			System.out.println("Client received: " + response + " from peer");
			
			response = Utils.send_interested(output_stream, input_stream, torrentFile, this.PEER_ID);
			System.out.println("Client received: " + response + " from peer");
			
			response = Utils.send_interested(output_stream, input_stream, torrentFile, this.PEER_ID);
			System.out.println("Client received: " + response + " from peer");
			
			output_stream.close();
			input_stream.close();
			socket.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}

}
