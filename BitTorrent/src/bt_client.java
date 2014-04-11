import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;

public class bt_client {

	public static void main(String[] args) throws MalformedURLException,
			IOException {
		// TODO Auto-generated method stub
		TorrentFileHandler torrent_file_handler = new TorrentFileHandler();
		TorrentFile torrent_file = torrent_file_handler
				.openTorrentFile("torrent_samples/xubuntu-13.10-desktop-i386.iso.torrent");

		if (torrent_file == null) {
			System.out.println("Cannot handle torrent file.");
			return;
		}

		String charset = "UTF-8";
		String tracker_url = torrent_file.tracker_url;
		String info_hash = torrent_file.info_hash_as_url;
		String peer_id = "-TR2820-7syhu6ju33mh";
		short port = 6969;
		int uploaded = 0;
		int downloaded = 0;
		int left = 874512384;
		boolean compact = true;

		String url = tracker_url + info_hash + peer_id + Short.toString(port)
				+ uploaded + downloaded + left + compact;
		String query = "";
		query = String
				.format("info_hash=%s&peer_id=%s&port=%s&uploaded=%s&downloaded=%s&left=%s&compact=%s",
						info_hash, peer_id, Short.toString(port),
						Integer.toString(uploaded),
						Integer.toString(downloaded), Integer.toString(left),
						(compact) ? "1" : "0");

		URLConnection connection = new URL(tracker_url + "?" + query)
				.openConnection();
		connection.setRequestProperty("Accept-Charset", charset);
		InputStream response = connection.getInputStream();

		
		
		byte[] byteStream = IOUtils.toByteArray(response);
		Utils utils = new Utils();
		TrackerResponse trackerResponse = utils.handleTrackerResponse(byteStream);
		System.out.println(utils.getIP());
	}

}
