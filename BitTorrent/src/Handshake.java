import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.ParseException;


public class Handshake {

	public static final String BITTORRENT_PROTOCOL_IDENTIFIER = "BitTorrent protocol";
	public static final int BASE_HANDSHAKE_LENGTH = 49;

	ByteBuffer data;
	ByteBuffer infoHash;
	ByteBuffer peerId;

	private Handshake(ByteBuffer data, ByteBuffer infoHash,
			ByteBuffer peerId) {
		this.data = data;
		this.data.rewind();

		this.infoHash = infoHash;
		this.peerId = peerId;
	}

	public ByteBuffer getData() {
		return this.data;
	}

	public byte[] getInfoHash() {
		return this.infoHash.array();
	}

	public byte[] getPeerId() {
		return this.peerId.array();
	}

	public static Handshake parse(ByteBuffer buffer)
			throws ParseException, UnsupportedEncodingException {
		int pstrlen = Byte.valueOf(buffer.get()).intValue();
		if (pstrlen < 0 ||
				buffer.remaining() != BASE_HANDSHAKE_LENGTH + pstrlen - 1) {
			throw new ParseException("Incorrect handshake message length " +
					"(pstrlen=" + pstrlen + ") !", 0);
		}

		// Check the protocol identification string
		byte[] pstr = new byte[pstrlen];
		buffer.get(pstr);
		if (!Handshake.BITTORRENT_PROTOCOL_IDENTIFIER.equals(
				new String(pstr, ("BitTorrent protocol")))) {
				throw new ParseException("Invalid protocol identifier!", 1);
				}

		// Ignore reserved bytes
		byte[] reserved = new byte[8];
		buffer.get(reserved);

		byte[] infoHash = new byte[20];
		buffer.get(infoHash);
		byte[] peerId = new byte[20];
		buffer.get(peerId);
		return new Handshake(buffer, ByteBuffer.wrap(infoHash),
				ByteBuffer.wrap(peerId));
	}
}