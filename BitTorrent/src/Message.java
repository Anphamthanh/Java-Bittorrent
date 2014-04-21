
public class Message {	
	
	public int length;
	public byte[] content;
	public int id;
	
	public Message(int l, byte[] c, int id) {
		length = l;
		content = c;
		this.id = id;
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
