package tpb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.util.Vector;

import protocol.ServerProtocol;
import tokenizer.MessageTokenizer;

/**
 * The connection helper for thread per client.
 * @param <T>
 */
class ConnectionHandler<T> implements Runnable {
	private static final int BUFFER_SIZE = 1024;
	protected final SocketChannel _sChannel;
	private ServerProtocol<T> protocol;
	private MessageTokenizer<T> tokenizer;
	protected Vector<ByteBuffer> _outData = new Vector<ByteBuffer>();
	private T msg = null;

	public ConnectionHandler(SocketChannel sChannel, ServerProtocol<T> p, MessageTokenizer<T> tokenizer)
	{
		_sChannel = sChannel;
		protocol = p;
		this.tokenizer = tokenizer;
	}

	public void run()
	{
		try {
			read();
		} 
		catch (IOException e) {
			System.out.println("Error in I/O");
		} 
		close();
	}

	public void read() throws IOException
	{
		boolean canRead = true;
		while (canRead)
		{
			ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
			int numBytesRead = 0;
			try {
				numBytesRead = _sChannel.read(buf);
			} catch (IOException e) {
				numBytesRead = -1;
				// No more bytes can be read from the channel
				close();
				// tell the protocol that the connection terminated.
				//	protocol.connectionTerminated();
				return;
			}
			// is the channel closed??
			if (numBytesRead == -1) {

			}
			buf.flip();
			tokenizer.addBytes(buf);
			if (tokenizer.hasMessage()){
				msg = tokenizer.nextMessage();
				protocol.processMessage(msg, (m) -> {
					try {
						ByteBuffer bytes = tokenizer.getBytesForMessage(m);
						addOutData(bytes);
						write();
					} catch (CharacterCodingException e) { e.printStackTrace(); }

				});

				if (protocol.isEnd(msg)){
					canRead = false;
				}
			}
		}
	}

	public synchronized void write() {
		if (_outData.size() == 0) {
			// if nothing left in the output string
			return;
		}
		// if there is something to send
		ByteBuffer buf = _outData.remove(0);
		if (buf.remaining() != 0) {
			try {
				_sChannel.write(buf);
			} catch (IOException e) {
				// this should never happen.
				e.printStackTrace();
			}
			// check if the buffer contains more data
			if (buf.remaining() != 0) {
				_outData.add(0, buf);
			}
		}
		// check if the protocol indicated close.
		if (protocol.isEnd(msg)) {
			if (buf.remaining() == 0) {
				close();
			}
		}
	}

	public synchronized void addOutData(ByteBuffer buf) {
		_outData.add(buf);
	}

	// Closes the connection
	public void close()
	{
		try {
			_sChannel.close();
		}
		catch (IOException e)
		{
			System.out.println("Exception in closing I/O");
		}
	}

}
