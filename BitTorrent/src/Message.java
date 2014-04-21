
public class Message {	
	
	public int length;
	public byte[] content;
	public byte id;
	
	public Message(int l, byte id, byte[] c) {
		length = l;
		content = c;
		this.id = id;
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("Message id = " + id + "\nLength = " + length + "\nContent: " + Utils.byteArrayToByteString(content) +"\n");
		return ret.toString();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
