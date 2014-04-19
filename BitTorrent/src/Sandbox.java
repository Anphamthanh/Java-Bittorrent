import java.io.IOException;
import java.net.MalformedURLException;

public class Sandbox {

	public static void main(String[] args) throws MalformedURLException, IOException {

		BitTorrentClient client = new BitTorrentClient(2512, "torrent_samples/xubuntu-13.10-desktop-i386.iso.torrent"); 
		client.contactTracker();
		
		client.contactPeers();
	}

}
