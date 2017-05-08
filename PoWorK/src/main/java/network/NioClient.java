package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;



public class NioClient implements Runnable {
	
	private InetAddress hostAddress = null;
	private int hostPort;
	
//	private SocketChannel channel = null;
    private Selector selector = null;
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    int bytesRead;    

	// A list of PendingChange instances
	private List pendingChanges = new LinkedList();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map pendingData = new HashMap();
	
	// Maps a SocketChannel to a RspHandler
	private Map rspHandlers = Collections.synchronizedMap(new HashMap());
	
    
	public NioClient() {
		try 
		{
//			this.hostAddress=host;
//			this.hostPort=port;
			selector = Selector.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}

    //Opens a connection with specified server on specified port
    public SocketChannel init(InetAddress ip, int port){
    	try{
    		SocketChannel channel = SocketChannel.open();
    		
    		channel.configureBlocking(false);
    		
    		try{
    		
    			channel.connect(new InetSocketAddress(ip, port));
    			 
    			synchronized(this.pendingChanges){
    				this.pendingChanges.add(new ChangeRequest(channel,ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
    			}
    		}catch(ConnectionPendingException e){
        		e.printStackTrace();
    		}
    		
    		return channel;
    		
        }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
    
    public void send(SocketChannel channel, byte[] data, RspHandler handler) throws IOException {
//		SocketChannel channel = init(this.hostAddress, this.hostPort);
		// Register the response handler
		this.rspHandlers.put(channel, handler);
		
		// And queue the data we want written
		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(channel);
			if (queue == null) {
				queue = new ArrayList();
				this.pendingData.put(channel, queue);
			}
			queue.add(ByteBuffer.wrap(data));
		}
		System.out.println("Sending...1");
		// Finally, wake up our selecting thread so it can make the required changes
		this.selector.wakeup();
	}
    
    
	@Override
	public void run() {
		
		while (true) {
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
							break;
						case ChangeRequest.REGISTER:
							change.socket.register(this.selector, change.ops);
							break;
						}
					}
					this.pendingChanges.clear();
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
			try {
				System.out.println("I am a client");
				selector.select();

				Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					keyIterator.remove();
					handleKey(key);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}


	private void handleKey(SelectionKey key) throws IOException {
		
		SelectionKey selKey = (SelectionKey)key;
		if (selKey.isValid() && selKey.isConnectable()){
			//Connect
			connect(key);
		}
		if (selKey.isValid() && selKey.isWritable()){
			//write
			// TODO - Change source of buffer with ByteBuffer which needs to be sent.
			write(key, buffer);
		}
		if(selKey.isValid() && selKey.isReadable()){
			//read
			read(key);
		}
	}

	private void connect(SelectionKey key){
	    try{
	    	SocketChannel channel = (SocketChannel) key.channel();
	        if(channel.finishConnect() ){
	            key.interestOps(SelectionKey.OP_WRITE);
	        }
	    }catch(IOException e){
	    	e.printStackTrace();
	        key.cancel();
	        return;
	    }
	}
	
	
	private void write(SelectionKey key, ByteBuffer op) throws IOException{
		SocketChannel channel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(channel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				channel.write(buf);
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
		System.out.println("Sending...2");
	}
	
	private void read(SelectionKey key) throws IOException{
		SocketChannel channel = (SocketChannel) key.channel();
		
		this.buffer.clear();
		
		// Attempt to read off the channel
				int numRead;
				try {
					numRead = channel.read(this.buffer);
				} catch (IOException e) {
					// The remote forcibly closed the connection, cancel
					// the selection key and close the channel.
					key.cancel();
					channel.close();
					return;
				}

				if (numRead == -1) {
					// Remote entity shut the socket down cleanly. Do the
					// same from our end and cancel the channel.
					key.channel().close();
					key.cancel();
					return;
				}

				// Handle the response
				this.handleResponse(channel, this.buffer.array(), numRead);
	}
	
	private void handleResponse(SocketChannel channel, byte[] data, int numBytes) throws IOException{
		byte[] rspData = new byte[numBytes];
		System.arraycopy(data, 0, rspData, 0, numBytes);
		
		RspHandler handler = (RspHandler) this.rspHandlers.get(channel);
		
		// And pass the response to it
		if (handler.handleResponse(rspData)) {
			// The handler has seen enough, close the connection
			channel.close();
			channel.keyFor(this.selector).cancel();
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
		try{
			NioClient client = new NioClient();
			Thread t = new Thread(client);
			t.setDaemon(true);
			t.start();
			RspHandler handler = new RspHandler();
			SocketChannel channel = client.init(InetAddress.getByName("192.168.0.23"), 1111);
			client.send(channel, "Hello".getBytes(), handler);
			handler.waitForResponse();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
