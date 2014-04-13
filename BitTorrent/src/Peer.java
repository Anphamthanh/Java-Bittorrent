
/**
 * Class for storing peer information
 *
 *
 */
public class Peer {
	
	private String ip;
	private int port;
	
	public Peer(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public String getIP() {
		return this.ip;
	}
	
	public int getPort() {
		return this.port;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
