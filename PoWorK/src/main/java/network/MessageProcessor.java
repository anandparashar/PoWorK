package network;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import blockchain.Block;
import blockchain.BlockChain;
import globalResources.GlobalResources;
import network.Message.contentType;
import network.Message.msgType;

public class MessageProcessor implements Runnable{

	private List queue = new LinkedList();

	public void processData(NioServer server, SocketChannel socket, byte[] data, int count) {
		byte[] dataCopy = new byte[count];
		System.arraycopy(data, 0, dataCopy, 0, count);
		try 
		{
			System.out.println("Processing Message");
			Message recMsg = Message.deserialize(data);
			processMessage(recMsg, server, socket);

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processMessage(Message msg, NioServer server, SocketChannel socket) throws IOException{
		if(msg.getMessageType().equals(msgType.BROADCAST))
		{
			System.out.println("Broaadcast message");
			processBroadcastMsg(msg, server, socket);
		}
		else
		{
			if(msg.getMessageType().equals(msgType.REQUEST))
			{
				processRequestMsg(msg, server, socket); 
			}
			else
			{
				if(msg.getMessageType().equals(msgType.RESPONSE)){
					processResponseMsg(msg, server, socket);
				}
			}
		}
	}

	private void processBroadcastMsg(Message msg, NioServer server, SocketChannel socket) {
		if(msg.getContent().equals(contentType.BLOCK))
		{
			System.out.println("block");
			processReceivedBlockMessage(msg, server, socket);
		}
		if(msg.getContent().equals(contentType.BLOCKCHAIN))
		{
			// TODO : Do nothing for now
		}
		if(msg.getContent().equals(contentType.TRANSACTION))
		{			
			// TODO : Freak out! You haven't implemented Transaction generation and transmission yet!
		}
	}


	@SuppressWarnings("deprecation")
	private void processReceivedBlockMessage(Message msg, NioServer server, SocketChannel socket) 
	{
		System.out.println("processing block");
		Block recBlock = msg.getBlock();
		//Current Block in chain
		Block currBlock = GlobalResources.env_vars.mainChain.getLastBlock();

		if (currBlock != null) {
			System.out.println("A current Block exists");
			//If the index of the currentBlock in our view is less than the index of the received block, process received block.
			// else received block is ignored.
			if (currBlock.getIndex().compareTo(recBlock.getIndex()) < 0) {
				Boolean isRecBlockValid = Block.isBlockvalid(recBlock, currBlock);
				if (isRecBlockValid) {
					System.out.println("Block Valid");
					System.out.println("suspending existing thread");
					// TODO : Stop mining thread
					if (GlobalResources.env_vars.miningThread != null)
						GlobalResources.env_vars.miningThread.suspend();

					System.out.println("Adding block to block chain");
					GlobalResources.env_vars.mainChain.addBlock(recBlock);
					System.out.println("starting new thread");
					//TODO: Start mining on new block received
					Thread mine = new Thread(GlobalResources.env_vars.manager.getMiner());
					mine.start();

					GlobalResources.env_vars.miningThread = mine;

				}
			} 
		}
		else
		{
			Boolean isRecBlockValid = Block.isBlockValidIsolated(recBlock);
			if (isRecBlockValid) {
				System.out.println("Block Valid");
				System.out.println("suspending existing thread, if any");
				// TODO : Stop mining thread
				if (GlobalResources.env_vars.miningThread != null)
					GlobalResources.env_vars.miningThread.suspend();

				System.out.println("Adding block to block chain");
				GlobalResources.env_vars.mainChain.addBlock(recBlock);
				System.out.println("starting new thread");
				//TODO: Start mining on new block received
				Thread mine = new Thread(GlobalResources.env_vars.manager.getMiner());
				mine.start();

				GlobalResources.env_vars.miningThread = mine;

			}
		}

	}

	private void processResponseMsg(Message msg, NioServer server, SocketChannel socket) {
		// TODO Auto-generated method stub
		//Do nothing, as per design, server shouldn't get response messages.

	}

	private void processRequestMsg(Message msg, NioServer server, SocketChannel socket) throws IOException {
		if(msg.getContent().equals(contentType.BLOCK))
		{
			Block toSendBlock = GlobalResources.env_vars.mainChain.getLastBlock();
			Message respMsg = new Message(msgType.RESPONSE, contentType.BLOCK, toSendBlock);
			// TODO : Send toSendBlock
			synchronized(queue) {
				queue.add(new ServerDataEvent(server, socket, Message.serialize(respMsg)));
				queue.notify();
			}
		}
		if(msg.getContent().equals(contentType.BLOCKCHAIN))
		{
			// TODO : Send GlobalResources.env_vars.mainChain
			BlockChain chain = GlobalResources.env_vars.mainChain;
//			if(!chain.isEmpty())
//			{
				Message respMsg = new Message(msgType.RESPONSE, contentType.BLOCKCHAIN, chain);
				synchronized(queue) 
				{
					System.out.println("Creating Server Data Event");
					queue.add(new ServerDataEvent(server, socket, Message.serialize(respMsg)));
					queue.notify();
				}
//			}
		}
		if(msg.getContent().equals(contentType.TRANSACTION))
		{			
			// TODO : Freak out! You haven't implemented Transaction generation and transmission yet!
		}
	}


	@Override
	public void run() {
		ServerDataEvent dataEvent;
	    
	    while(true) {
	      // Wait for data to become available
	      synchronized(queue) {
	        while(queue.isEmpty()) {
	          try {
	            queue.wait();
	          } catch (InterruptedException e) {
	          }
	        }
	        dataEvent = (ServerDataEvent) queue.remove(0);
	      }
	      
	      // Return to sender
	      System.out.println("Sending response...");
	      dataEvent.server.send(dataEvent.socket, dataEvent.data);
	    }

	}



}
