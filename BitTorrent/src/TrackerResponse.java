import java.util.HashMap;


public class TrackerResponse {
	
	private int complete;
	private int incomplete;
	private int interval;
	private HashMap<Byte[], Integer> peerList;
	
	public TrackerResponse(int complete, int incomplete, int interval){
		this.complete = complete;
		this.incomplete = incomplete;
		this.interval = interval;
		this.peerList = new HashMap<Byte[], Integer>();
	}
	
	public TrackerResponse(int complete, int incomplete, int interval, HashMap<Byte[], Integer> peerList){
		this.complete = complete;
		this.incomplete = incomplete;
		this.interval = interval;
		this.peerList = peerList;
	}
	
	public int getComplete(){
		return this.complete;
	}
	
	public void setComplete(int complete){
		this.complete = complete;
	}
	
	public int getInomplete(){
		return this.incomplete;
	}
	
	public void setIncomplete(int incomplete){
		this.incomplete = incomplete;
	}
	
	public int getInterval(){
		return this.interval;
	}
	
	public void setInterval(int interval){
		this.interval= interval;
	}
	
	public HashMap<Byte[], Integer> getPeerList(){
		return this.peerList;
	}
	
	public void updatePeerList(HashMap<Byte[], Integer> peerList){
		this.peerList = peerList;
	}
	
	public void addPeerInfo(Byte[] IP, int port){
		peerList.put(IP, port);
	}
}
