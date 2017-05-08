package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class NioServer implements Runnable{
	
//	private static final org.slf4j.Logger log = LoggerFactory.getLogger(NioServer.class);
	private InetAddress hostAddress = null;
	private int port;
	private MessageProcessor processor = null;
	private ServerSocketChannel serverChannel = null;
    private Selector selector = null;
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    int bytesRead;
    
    // A list of Change Ops Request instances
    private List changeRequests = new LinkedList();
    
    // Might come in handy
	// A list of PendingChange instances
	private List pendingChanges = new LinkedList();
	
    // Maps a SocketChannel to a list of ByteBuffer instances
    private Map pendingData = new HashMap();
	
    /**
     * Initializes an instance of Non I/O Blocking server ready to accept new incoming connections.
     * @param host - IP address of host. If null, wildcard address is assigned.
     * @param portNum - A valid port value is between 0 and 65535. A port number of zero will let 
     * 	the system pick up an ephemeral port in a bind operation. 
     */
    public NioServer(InetAddress host, int portNum, MessageProcessor processor){
    	try {
    		this.processor = processor;
    		this.hostAddress = host;
    		this.port=portNum;
			this.selector = Selector.open();
			this.serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			
			InetSocketAddress serverSocketAddress= new InetSocketAddress(this.hostAddress, this.port);
//			InetSocketAddress serverSocketAddress= new InetSocketAddress("localhost", this.port);
			System.out.println(serverSocketAddress.getHostName());
			serverChannel.socket().bind(serverSocketAddress);
			
			serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /* (non-Javadoc)
     * Main Loop for Server
     * @see java.lang.Runnable#run()
     */
    @Override
	public void run() {
		while(true){
			try{
				
				// Process any pending changes
				synchronized (this.pendingChanges) {
					Iterator changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket.keyFor(this.selector);
							key.interestOps(change.ops);
						}
					}
					this.pendingChanges.clear();
				}
				
				System.out.println("I am a server waiting for new connections");
				this.selector.select();
				
				Iterator selectedKeys = this.selector.selectedKeys().iterator();
				while(selectedKeys.hasNext())
				{
					SelectionKey selectedKey = (SelectionKey)selectedKeys.next();
					System.out.println("Key selected "+selectedKey.interestOps());
					selectedKeys.remove();
					
					if(selectedKey.isValid()){
						if(selectedKey.isAcceptable()){
							System.out.println("Acceptable");
							accept(selectedKey);
						}else{
							if(selectedKey.isReadable()){
								System.out.println("Readable");
								read(selectedKey);
							}
							else
							{
								if (selectedKey.isWritable()) {
									System.out.println("Writable");
									this.write(selectedKey);
								}
							}
						}
							
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
    
    private void accept(SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        SelectionKey acceptedKey = socketChannel.register(this.selector, SelectionKey.OP_READ);
        System.out.println("Accepted");
      }
    
	
	/**
	 * reads the data from previously accepted key
	 * @param key
	 * @throws IOException 
	 */
	private void read(SelectionKey key) throws IOException{
	    int numBytes=0;
	    SocketChannel c = (SocketChannel) key.channel();
	 
	    this.buffer.clear();
	 
	    try {
			numBytes = c.read(this.buffer);
			    if(numBytes== -1){
			    	System.out.println("No Data, Closing key");
			        key.channel().close();
			        key.cancel();
			    }
			    else{
//			        this.bytesRead = numBytes;
			        System.out.println("Bytes Read "+numBytes);
//			        System.out.println("Read: "+new String(this.buffer.array()));
			        Message recMsg = Message.deserialize(this.buffer.array());
			        System.out.println("Message received"+recMsg.getMessageType()+" "+recMsg.getContent());
//			        System.out.println("BlockChain"+recMsg.getChain().toString());
//			        this.processor.processData(this, c, this.buffer.array(), numBytes);
			        // TODO - Add processing of data
			        // Hand the data off to our worker thread
			        // check if data received is Block, BlockChain, Transaction
			        // process accordingly
			        // this.worker.processData(this, c, this.buffer.array(), numBytes); 
			    }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error, Closing key");
			e.printStackTrace();
			key.channel().close();
	        key.cancel();
	        return;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    if (numBytes == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}
	    this.processor.processData(this, c, this.buffer.array(), numBytes);
	}

	public void send(SocketChannel socket, byte[] data) {
		synchronized (this.pendingChanges) {
			// Indicate we want the interest ops set changed
//			System.out.println("Creating pending changes");
			this.pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (this.pendingData) {
				List queue = (List) this.pendingData.get(socket);
				if (queue == null) {
//					System.out.println("queue null");
					queue = new ArrayList();
					this.pendingData.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}

		// Finally, wake up our selecting thread so it can make the required changes
		this.selector.wakeup();
	}
	
	private void write(SelectionKey key) throws IOException{
		System.out.println("In Write");
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(socketChannel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	
	public class ChangeRequest {
		  public static final int REGISTER = 1;
		  public static final int CHANGEOPS = 2;
		  
		  public SocketChannel socket;
		  public int type;
		  public int ops;
		  
		  public ChangeRequest(SocketChannel socket, int type, int ops) {
		    this.socket = socket;
		    this.type = type;
		    this.ops = ops;
		  }
		}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NioServer server;
		try 
		{
			MessageProcessor processor = new MessageProcessor();
			new Thread(processor).start();
			server = new NioServer(InetAddress.getByName("192.168.0.23"), 1111, processor);
			Thread t = new Thread(server);
			t.start();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}