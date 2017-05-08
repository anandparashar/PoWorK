package network;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import blockchain.Block;
import blockchain.BlockChain;
import globalResources.GlobalResources;
import network.Message.contentType;
import network.Message.msgType;

public class RspHandler {
	private byte[] rsp = null;
	
	public synchronized boolean handleResponse(byte[] rsp) {
		this.rsp = rsp;
		this.notify();
		return true;
	}
	
	public synchronized void waitForResponse() {
		while(this.rsp == null) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
		
		System.out.println(new String(this.rsp));
		try {
			
			Message respMsg = Message.deserialize(this.rsp);
			processMessage(respMsg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void processMessage(Message msg) throws IOException{
		if(msg.getMessageType().equals(msgType.RESPONSE)){
			processResponseMsg(msg);
		}
	}

	private void processResponseMsg(Message msg) {
		if(msg.getContent().equals(contentType.BLOCK))
		{
			System.out.println("Block Response Received");
			Block recBlock = msg.getBlock();
			//Current Block in chain
			Block currBlock = GlobalResources.env_vars.mainChain.getLastBlock();
			Boolean isRecBlockValid = Block.isBlockvalid(recBlock, currBlock);
			if(isRecBlockValid){
				// TODO : Stop mining on thread and start mining on new block
				System.out.println(currBlock.toString());
				
			}
		}
		else
		{
			if(msg.getContent().equals(contentType.BLOCKCHAIN)){
				System.out.println("Block Chain Received");
				BlockChain chain = msg.getChain();
//				System.out.println(chain.toString());
				
				GlobalResources.env_vars.mainChain = chain;
				// TODO : Start mining
				if(GlobalResources.env_vars.miningThread != null)
					GlobalResources.env_vars.miningThread.suspend();
				
				//TODO: Start mining on new block received
				Thread mine = new Thread(GlobalResources.env_vars.manager.getMiner());
				mine.start();
				
				GlobalResources.env_vars.miningThread = mine;
				
			}
		}
		
	}
}

