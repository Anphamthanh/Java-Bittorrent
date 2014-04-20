
public class Message {	
	
	public int length;
	public byte[] content;
	
	public Message(int l, byte[] c) {
		length = l;
		content = c;
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("Length " + length + "\tContent " + content +"\n");
		return ret.toString();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
