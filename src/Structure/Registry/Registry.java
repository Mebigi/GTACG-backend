package Structure.Registry;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Registry {
	private static int ids;
	private int id;
	private String key; 
	private long start;
	private int len;
	private long lenSeq;
	private File f;
	
	public Registry(String key, String header, long start, int len, int lenHeader, long lenSeq, File f) {
		this.key = key;
		this.start = start;
		this.len = len;
		this.lenSeq = lenSeq;
		this.f = f;
		this.id = ids++;
	}
	
	public String get() throws IOException {
		RandomAccessFile rand = new RandomAccessFile(f, "r");
		FileChannel channel = rand.getChannel();
		ByteBuffer seq = ByteBuffer.allocate(len);
		channel.read(seq, start);
		rand.close();
		return new String(seq.array());
	}
	
	@Override
	public String toString() {
		return key + "\t" +
			start + "\t" +
			len + "\t" +
			lenSeq + "\t" +
			f;
	}
	
	public int getId() {
		return id;
	}
	
	public String getKey() {
		return key;			
	}
	
	public int getLengthHeader() {
		return (int)(len - lenSeq);
	}
	
	public long getLengthByteSeq() {
		return lenSeq;
	}

	public String getHeader() throws IOException {
		String seq = get();
		return seq.substring(0, seq.indexOf("\n"));
	}
	
	public int getLength() {
		return len;
	}
	
	public File getFile() {
		return f;
	}
}
