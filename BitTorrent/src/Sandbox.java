import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream.GetField;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.net.InetAddresses;

public class Sandbox {

	public static void main(String[] args) throws MalformedURLException, IOException {

		BitTorrentClient client = new BitTorrentClient(2512, "torrent_samples/xubuntu-13.10-desktop-i386.iso.torrent"); 
		client.contactTracker();
		
		client.contactPeers();
	}

}
