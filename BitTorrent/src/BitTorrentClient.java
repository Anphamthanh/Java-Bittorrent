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
	private int BLOCK_LENGTH = 1<<14;
	private int TOTAL_PIECE = 0;
	private int PIECE_SIZE = 0;
	
	private TorrentFileHandler torrentFileHandler;
	private TorrentFile torrentFile;
	private static boolean COMPACT = true; 
	private int uploaded = 0;
	private int downloaded = 0;
	private int interval = 0;
	private int left = 0;
	private HashMap<String, Integer> peerList;
	private ArrayList<Peer> peer_pool;
	private int current_piece_index = 0;
	private int current_block_offset = 0;
	private byte[] current_bitfield;
	private int current_peer_index = 0;
	private Peer current_peer;
	private boolean finished = false;
	
	
	
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
		TOTAL_PIECE = torrentFile.piece_hash_values_as_hex.size();
		PIECE_SIZE = torrentFile.piece_length;
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
		
		this.peer_pool = Utils.getPeerArray(trackerResponse.getPeerList());
		
		
		
		return 0;
	}
	
	public int contactPeers() throws MalformedURLException, IOException {
		
		if (this.peer_pool == null) {
			System.out.println("There is no peer to work with!");
			return -1;
		}
		
		while(!finished) {

			current_peer = this.peer_pool.get(current_peer_index);

			try {

				System.out.println("Contacting IP " + current_peer.getIP() + " Port " + current_peer.getPort());

				Socket socket = new Socket(current_peer.getIP(), current_peer.getPort()); 

				System.out.println("Just connected to " + socket.getRemoteSocketAddress()); 

				DataOutputStream output_stream = new DataOutputStream(socket.getOutputStream());
				DataInputStream input_stream = new DataInputStream(socket.getInputStream());
				byte[] response = null; 	        

				while(true) {
					response = MessageHandler.send_handshake(output_stream, input_stream, torrentFile, this.PEER_ID);
					System.out.println("Client received: " + Utils.byteArrayToByteString(response)+ " from peer");
					if (MessageHandler.is_handshake(response, torrentFile, PEER_ID)) {
						break;
					}
					Utils.sleep(2000);
				}

				Message message;

				while(true) {
					message = MessageHandler.send_interested(output_stream, input_stream);
					System.out.println("Client received: \n" + message);
					if (MessageHandler.is_unchoke(message)) {
						break;
					}
					Utils.sleep(2000);
				}

				if (MessageHandler.is_bitfield(message)) {					
					if (!MessageHandler.is_piece_available(message.content, current_piece_index)) {
						current_peer_index++;
						continue;
					}
				}
				else {
					current_peer_index++;
					continue;					
				}
				
				current_bitfield = message.content;
							
				message = MessageHandler.send_request(output_stream, input_stream, current_piece_index, current_block_offset, BLOCK_LENGTH);
				System.out.println("Client received: \n" + message);

				finished = true;
				output_stream.close();
				input_stream.close();
				socket.close();
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		return 0;
	}
	

}
