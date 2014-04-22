import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class TorrentFileHandler
{
	
	private class Index
	{
		public int index;

		public Index()
		{
			super();
			this.index = 0;
		}
	}

	private final int NULL_TYPE = 0;

	private final int STRING = 1;

	private final int INTEGER = 2;

	private final int LIST = 3;

	private final int DICTIONARY = 4;

	private final int STRUCTURE_END = 5;


	private TorrentFile torrent_file;
	
	private Bencoder bencoder;


	public TorrentFileHandler()
	{
		super();
		this.torrent_file = new TorrentFile();
		this.bencoder = new Bencoder();
	}

	public TorrentFile openTorrentFile(String file_name)
	{
		byte[] file_data = getBytesFromFile(file_name);
		HashMap file_data_map;
		Index index = new Index();

		file_data_map = parseDictionary(file_data, index);
		
		if(!storeDataInTorrent(file_data_map))
		{
			return null;
		}
		
		return this.torrent_file;
	}

	private byte[] getBytesFromFile(String file_name)
	{
		File file = new File(file_name);
		long file_size_long = -1;
		byte[] file_bytes = null;
		InputStream file_stream;

		try
		{
			file_stream = new FileInputStream(file);

			// Verify that the file exists
			if (!file.exists())
			{
				System.err
						.println("Error: [TorrentFileHandler.java] The file \""
								+ file_name
								+ "\" does not exist. Please make sure you have the correct path to the file.");
				file_stream.close();
				return null;
			}

			// Verify that the file is readable
			if (!file.canRead())
			{
				System.err
						.println("Error: [TorrentFileHandler.java] Cannot read from \""
								+ file_name
								+ "\". Please make sure the file permissions are set correctly.");
				file_stream.close();
				return null;
			}

			// The following code was derived from
			// http://javaalmanac.com/egs/java.io/File2ByteArray.html
			file_size_long = file.length();

			if (file_size_long > Integer.MAX_VALUE)
			{
				System.err.println("Error: [TorrentFileHandler.java] The file \"" + file_name
						+ "\" is too large to be read by this class.");
				file_stream.close();
				return null;
			}


			file_bytes = new byte[(int) file_size_long];

			int file_offset = 0;
			int bytes_read = 0;

			while (file_offset < file_bytes.length
					&& (bytes_read = file_stream.read(file_bytes, file_offset,
							file_bytes.length - file_offset)) >= 0)
			{
				file_offset += bytes_read;
			}


			if (file_offset < file_bytes.length)
			{
				file_stream.close();
				throw new IOException("Could not completely read file \""
						+ file.getName() + "\".");
			}

			file_stream.close();

		}
		catch (FileNotFoundException e)
		{
			System.err
					.println("Error: [TorrentFileHandler.java] The file \""
							+ file_name
							+ "\" does not exist. Please make sure you have the correct path to the file.");
			return null;
		}
		catch (IOException e)
		{
			System.err
					.println("Error: [TorrentFileHandler.java] There was a general, unrecoverable I/O error while reading from \""
							+ file_name + "\".");
			System.err.println(e.getMessage());
		}

		return file_bytes;
	}

	private int getEncodedType(byte[] data, Index index)
	{
		// The value to be returned
		int return_value = NULL_TYPE;

		// Set return_value according to the byte at data[index.index]
		switch ((char) data[index.index])
		{
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return_value = STRING;
				break;
			case 'i':
				return_value = INTEGER;
				break;
			case 'l':
				return_value = LIST;
				break;
			case 'd':
				return_value = DICTIONARY;
				break;
			case 'e':
				return_value = STRUCTURE_END;
				break;
			default:
				System.err
						.println("Error: [TorrentFileHandler.java] The byte at position "
								+ index.index
								+ " in the .torrent file is not the beginning of a bencoded data type.");
				break;
		}

		return return_value;
	}

	
	private String parseString(byte[] data, Index index)
	{
		String return_string = null;
		int temp_index = index.index;
		int power_of_ten = 1;
		int length_of_string = 0;
		boolean first_digit = false;
		StringBuffer temp_string = new StringBuffer();

		while (data[temp_index] != (byte) ':')
		{
			if (first_digit)
			{
				power_of_ten *= 10;
			}
			first_digit = true;
			temp_index++;
		}

		while (data[index.index] != (byte) ':')
		{
			length_of_string += ((data[index.index] - 48) * power_of_ten);
			power_of_ten /= 10;
			index.index++;
		}

		index.index++;


		while ((length_of_string > 0) && (index.index <= data.length))
		{
			temp_string.append((char) data[index.index]);

			length_of_string--;
			index.index++;
		}

		return_string = temp_string.toString();
		
		return return_string;
	}

	private Integer parseInteger(byte[] data, Index index)
	{
		Integer return_integer;
		int temp_value = 0;
		int power_of_ten = 1;
		boolean first_digit = false;
		boolean is_negative = false;


		index.index++;
		
		if(data[index.index] == (byte)'-')
		{
			is_negative = true;
			index.index++;
		}
		int temp_index = index.index;


		while (data[temp_index] != (byte) 'e')
		{
			if (first_digit)
			{
				power_of_ten *= 10;
			}
			first_digit = true;
			temp_index++;
		}

	
		while (data[index.index] != (byte) 'e')
		{
			temp_value += ((data[index.index] - 48) * power_of_ten);
			power_of_ten /= 10;
			index.index++;
		}

		index.index++;

		if(is_negative)
		{
			return_integer = new Integer(-temp_value);
		}
		else
		{
			return_integer = new Integer(temp_value);
		}
		
		return return_integer;
	}

	private Vector parseList(byte[] data, Index index)
	{
		Vector return_list = new Vector();

		index.index++;

		int next_data_type = getEncodedType(data, index);

		while ((next_data_type != STRUCTURE_END)
				&& (next_data_type != NULL_TYPE) && (index.index < data.length))
		{
			switch(next_data_type)
			{
				case INTEGER:
					return_list.add(parseInteger(data, index));
					break;
				case STRING:
					return_list.add(parseString(data, index));
					break;
				case LIST:
					return_list.add(parseList(data, index));
					break;
				case DICTIONARY:
					return_list.add(parseDictionary(data, index));
					break;
				default:
					System.err.println("Error: [TorrentFileHandler.java] The object at position " + index.index
							+ " is not a valid bencoded data type.");
					return null;
			}
			next_data_type = getEncodedType(data, index);
		}


		index.index++;
		
		return return_list;
	}

	private HashMap parseDictionary(byte[] data, Index index)
	{
		HashMap returned_map = new HashMap(10);
		String key;
		Object value;

		index.index++;

		int next_data_type = getEncodedType(data, index);


		while ((next_data_type != NULL_TYPE)
				&& (next_data_type != STRUCTURE_END)
				&& (index.index < data.length))
		{

			if (next_data_type != STRING)
			{
				System.err
						.println("Error: [TorrentFileHandler.java] The bencoded object beginning at index "
								+ index.index
								+ " is not a String, but must be according to the BitTorrent definition.");
			}

			key = parseString(data, index);

			next_data_type = getEncodedType(data, index);

			switch (next_data_type)
			{
				case INTEGER:
					value = parseInteger(data, index);
					break;
				case STRING:
					value = parseString(data, index);
					break;
				case LIST:
					value = parseList(data, index);
					break;
				case DICTIONARY:
					if(key.equalsIgnoreCase("info"))
					{
						int old_index = index.index;
						value = parseDictionary(data, index);
						byte[] info = new byte[index.index-old_index];
					
						for(int i = 0; i < info.length; i++)
						{
							info[i] = data[old_index + i];
						}
						torrent_file.info_hash_as_binary = Utils.generateSHA1Hash(info);
						torrent_file.info_hash_as_url = Utils.byteArrayToURLString(torrent_file.info_hash_as_binary);
						torrent_file.info_hash_as_hex = Utils.byteArrayToByteString(torrent_file.info_hash_as_binary);
					}
					else
					{
						value = parseDictionary(data, index);
					}
					break;
				default:
					System.err.println("Error: [TorrentFileHandler.java] The value of the key \"" + key
							+ "\" is not a valid bencoded data type.");
					return null;
			}

			returned_map.put(key, value);
			
			next_data_type = getEncodedType(data, index);
		}
		
		//Skip the 'e'
		index.index++;
		
		return returned_map;
	}
	
	private boolean storeDataInTorrent(Map torrent_data_map)
	{
		Map info_map = (Map)torrent_data_map.get("info");
		if(info_map == null)
		{
			System.err.println("Error: [TorrentFileHandler.java] Could not retrieve the info dictionary.");
			return false;
		}
		if(!getPieceHashes((String)info_map.get("pieces")))
		{
			return false;
		}
		
		torrent_file.tracker_url = (String)torrent_data_map.get("announce");
		if(torrent_file.tracker_url == null)
		{
			System.err.println("Error: [TorrentFileHandler.java] Could not retrieve the tracker URL.");
			return false;
		}
		
		torrent_file.file_length = ((Integer)info_map.get("length")).intValue();
		if(torrent_file.file_length < 0)
		{
			System.err.println("Error: [TorrentFileHandler.java] Could not retrieve the file length.");
			return false;
		}
		
		torrent_file.piece_length = ((Integer)info_map.get("piece length")).intValue();
		if(torrent_file.piece_length < 0)
		{
			System.err.println("Error: [TorrentFileHandler.java] Could not retrieve the piece length.");
			return false;
		}
		
		torrent_file.pieces = torrent_file.file_length/torrent_file.piece_length;
		
		return true;
		
	}
	
	private boolean getPieceHashes(String hash_string)
	{
		if(hash_string.length() % 20 != 0)
		{
			System.err.println("Error: [TorrentFileHandler.java] The SHA-1 hash for the file's pieces is not the correct length.");
			return false;
		}
		
		byte[] binary_data = new byte[hash_string.length()];
		byte[] individual_hash;
		int number_of_pieces = binary_data.length / 20;
		
		for(int i = 0; i < binary_data.length; i++)
		{
			binary_data[i] = (byte)hash_string.charAt(i);
		}
		
		for(int i = 0; i < number_of_pieces; i++)
		{
			individual_hash = new byte[20];
			for(int j = 0; j < 20; j++)
			{
				individual_hash[j] = binary_data[(20*i)+j];
			}
			torrent_file.piece_hash_values_as_binary.add(individual_hash);
			torrent_file.piece_hash_values_as_hex.add(Utils.byteArrayToByteString(individual_hash));
			torrent_file.piece_hash_values_as_url.add(Utils.byteArrayToURLString(individual_hash));
		}
		
		return true;
	}


}
