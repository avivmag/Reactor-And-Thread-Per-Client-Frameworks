package tpb;

import java.io.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.org.apache.xml.internal.security.keys.keyresolver.implementations.SecretKeyResolver;

import game.Bluffer;
import game.GameFactory;
import game.TextBasedGameServer;
import protocol.AsyncServerProtocol;
import protocol.ServerProtocolFactory;
import protocol.TBGSProtocol;
import reactor.Reactor;
import tokenizer.CommandMessage;
import tokenizer.CommandParamsSeparatorTokenizer;
import tokenizer.MessageTokenizer;
import tokenizer.TokenizerFactory;

/**
 * The main server for multiple client protocol server.
 * @param <T>
 */
class MultipleClientProtocolServer<T> implements Runnable {
	private SocketChannel socket;
	private int listenPort;
	private ServerSocketChannel _ssChannel;
	private final ServerProtocolFactory<CommandMessage> _protocolFactory;
	private final TokenizerFactory<CommandMessage> _tokenizerFactory;
    private static final Logger logger = Logger.getLogger("edu.spl.tpb");

	public MultipleClientProtocolServer(int port, ServerProtocolFactory<CommandMessage> protocol, TokenizerFactory<CommandMessage> tokenizer)
	{
		socket = null;
		listenPort = port;
		_protocolFactory = protocol;
		_tokenizerFactory = tokenizer;
	}
	
	/**
	 * Create a new server socket channel for with the given ports
	 * @param port
	 * @return
	 * @throws IOException
	 */
	private ServerSocketChannel create(int port) throws IOException{
		ServerSocketChannel ssChanel = ServerSocketChannel.open();
		ssChanel.configureBlocking(true);
		ssChanel.bind(new InetSocketAddress(port));
		return ssChanel;
	}
	
	public void run()
	{
		try {
			_ssChannel = create(listenPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ConnectionHandler<CommandMessage> newConnection;
		while (true)
		{
			try {
				socket = accept();
				if (socket != null){
					newConnection = new ConnectionHandler<CommandMessage>(socket, _protocolFactory.create(), _tokenizerFactory.create());
					new Thread(newConnection).start();
				}
			}
			catch (IOException e) {
				logger.info(e.getMessage());
				break;
			}
		}
	}

	/**
	 * Get a new channel for the connection request
	 * @return
	 * @throws IOException
	 */
	public SocketChannel accept() throws IOException {
		SocketChannel sChannel = _ssChannel.accept();
		sChannel.configureBlocking(true);
		return sChannel;
	}


	// Closes the connection
	public void close() throws IOException
	{
		socket.close();
	}

	public static void main(String[] args) throws IOException
	{

		ServerProtocolFactory<CommandMessage> _protocolFactory = new ServerProtocolFactory<CommandMessage>(){

			@Override
			public AsyncServerProtocol<CommandMessage> create() {
				return new TBGSProtocol(); 
			}
		};
		
		final Charset charset = Charset.forName("UTF-8");
		
		TokenizerFactory<CommandMessage> _tokenizerFactory = new TokenizerFactory<CommandMessage>() {

			@Override
			public MessageTokenizer<CommandMessage> create() {
				return new CommandParamsSeparatorTokenizer("\n", charset);
			}
		};
		
		if (args.length < 1) {
			logger.info("port number is needed");
			System.exit(1);
		}

		Map<String, GameFactory> map = new HashMap<String, GameFactory>();
		map.put("BLUFFER", ()-> { return new Bluffer("./bluffer_questions.json");});
		TextBasedGameServer.getInstance().initialize(map);

		// Get port
		int port = Integer.decode(args[0]).intValue();

		MultipleClientProtocolServer<CommandMessage> server = new MultipleClientProtocolServer<CommandMessage>(port, _protocolFactory, _tokenizerFactory);
		logger.info("thread per client ready on port " + server.listenPort);
		Thread serverThread = new Thread(server);
		serverThread.start();
		try {
			serverThread.join();
		}
		catch (InterruptedException e)
		{
			logger.info("Server stopped");
		}

	}
}