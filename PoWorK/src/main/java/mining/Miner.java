package mining;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import blockchain.Block;
import globalResources.GlobalResources;
import network.NioClient;
import network.NioServer;
import network.Message;
import network.Message.contentType;
import network.Message.msgType;
import network.MessageProcessor;
import network.RspHandler;
import nonInteractiveProof.Knowledge;
import nonInteractiveProof.Proof;
import nonInteractiveProof.Work;
import nonInteractiveVerification.Verification;

public class Miner implements Runnable{
	
	Block prevBlock = null;
	 
	
	private type minerType = type.WORK;
	private Work pow;
	private Knowledge pok;
	
	/**
	 * Defines the type of proof a miner computes. 
	 * {@link type.WORK} assumes no knowledge of secret and computes Proof of Work same as Bitcoin.
	 * {@link type.KNOWLEDGE} assumes miner to be in witness of a secret, using which a proof is computed.
	 * @author anand
	 *
	 */
	public enum type{
		WORK, KNOWLEDGE
	};

	private static BigInteger rand256(){
		SecureRandom r = new SecureRandom();
		BigInteger rand = new BigInteger(256, r);
		while(rand.equals(BigInteger.ZERO)){
			rand = new BigInteger(256, r);
		}
		return rand;
	}
	
	/**
	 * Initializes a miner according to the {@link minerType} specified. If invalid, {@link minerType} is initialized as {@link type.WORK}.
	 * For more, see {@link type}
	 * @param minerType
	 */
	public Miner(type minerType){
		switch(minerType){
			case WORK:
				this.minerType = type.WORK;
				pow = new Work();
				break;
			case KNOWLEDGE:
				this.minerType = type.KNOWLEDGE;
				pok = new Knowledge();
				break;
			default:
				this.minerType = type.WORK;
				pow = new Work();
		};
	}
	
	/**
	 * Computes the indistinguishable {@link Proof} of work or Knowledge and returns it for {@link Block} generation.
	 * @return {@link Proof}
	 */
	private Proof provideProof(){
		if(this.minerType.equals(type.WORK)){
			try {
				return pow.provideProof();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			try {
				return pok.provideProof();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public Block generateGenesisBlock(){
		
		Verification verify;
		
		Proof proof = provideProof();
		
		verify = new Verification(proof);
		
		Boolean isValid = verify.verify();
		
		while(!isValid){
			proof = provideProof();
			verify = new Verification(proof);
			isValid = verify.verify();
		}
		
		long time = System.currentTimeMillis() / 1000L;
		
		Block newBlock = new Block(BigInteger.ZERO, BigInteger.ZERO.toByteArray(), Long.toString(time), rand256().toByteArray(), proof);
		
		System.out.println("Genesis Block Generated: "+newBlock.toString());
		return newBlock;
	}
	
	/**
	 * Function to generate a new {@link Block} succeeding the {@link prevBlock} by computing the Proof of Work or Knowledge.
	 * Assumes validity of {@link prevBlock} has been checked
	 * Calling function must perform validity check for Block generated. See {@link blockchain.Block.isBlockvalid} for more information.
	 * @param prevBlock
	 * @return new {@link Block} generated
	 */
	public Block generateBlock(Block prevBlock){
		Verification verify;
		
		Proof proof = provideProof();
		
		verify = new Verification(proof);
		
		Boolean isValid = verify.verify();
		
		while(!isValid){
			proof = provideProof();
			verify = new Verification(proof);
			isValid = verify.verify();
		}
		
		long time = System.currentTimeMillis() / 1000L;
		
		Block newBlock = new Block(prevBlock.getIndex().add(BigInteger.ONE), prevBlock.getCurrentBlockHash(), Long.toString(time), rand256().toByteArray(), proof);
		
		
		System.out.println("Next Block Generated: "+newBlock.toString());
//		System.out.println("Is Block Valid?"+Block.isBlockvalid(newBlock, prevBlock));
		return newBlock;
	}
	
	public void stopMining(){
		try {
			synchronized (Thread.currentThread()){
			Thread.currentThread().wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			Block prevBlock = GlobalResources.env_vars.mainChain.getLastBlock();
			
			NioClient client = GlobalResources.env_vars.globalClient;
			
			if(prevBlock != null)
			{
				Block newBlock = generateBlock(prevBlock);
				if(Block.isBlockvalid(newBlock, prevBlock))
				{
					RspHandler handler = new RspHandler();
					Message msg = new Message(msgType.BROADCAST, contentType.BLOCK, newBlock);		
					try {
						client.send(client.init(InetAddress.getByName("10.159.8.121"), 1111), Message.serialize(msg), handler);
//						client.send(client.init(InetAddress.getByName("10.159.8.121"), 123), Message.serialize(msg), handler);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					GlobalResources.env_vars.mainChain.addBlock(newBlock);
				}
			}
			else
			{
				System.out.println("First Block");
				Block genesisBlock = generateGenesisBlock();
				if(Block.isBlockvalid(genesisBlock, null))
				{
					RspHandler handler = new RspHandler();
					Message msg = new Message(msgType.BROADCAST, contentType.BLOCK, genesisBlock);
					try {
						client.send(client.init(InetAddress.getByName("10.159.8.121"), 1111), Message.serialize(msg), handler);
//						client.send(client.init(InetAddress.getByName("10.159.8.121"), 123), Message.serialize(msg), handler);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					GlobalResources.env_vars.mainChain.addBlock(genesisBlock);
				}
			}
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		try 
		{
//			MessageProcessor processor = new MessageProcessor();
//			new Thread(processor).start();
//			NioServer server = new NioServer(InetAddress.getByName("192.168.0.23"), 1111, processor);
//			Thread tserver = new Thread(server);
//			tserver.start();
//					
//			NioClient client = new NioClient();
//			Thread tclient = new Thread(client);
//			tclient.setDaemon(true);
//			tclient.start();
			GlobalResources.env_vars.setFilepath("./resources/BlockChain/");
			
			
			Miner miner = new Miner(type.KNOWLEDGE);
			
			Block genesisBlock = miner.generateGenesisBlock();
			if(Block.isBlockvalid(genesisBlock, null))
			{
//				System.out.println("Sending Block");
//				RspHandler handler = new RspHandler();
//				Message msg = new Message(msgType.BROADCAST, contentType.BLOCK, genesisBlock);
//				client.send(client.init(InetAddress.getByName("192.168.0.23"), 1111), Message.serialize(msg), handler);
//				handler.waitForResponse();
				GlobalResources.env_vars.mainChain.addBlock(genesisBlock);
				
			}
					
			
//			Block newBlock = miner.generateBlock(genesisBlock);
//			if(Block.isBlockvalid(newBlock, genesisBlock))
//			{
////				System.out.println("Sending Block");
////				RspHandler handler = new RspHandler();
////				Message msg = new Message(msgType.BROADCAST, contentType.BLOCK, genesisBlock);
////				client.send(client.init(InetAddress.getByName("192.168.0.23"), 1111), Message.serialize(msg), handler);
////				handler.waitForResponse();
//				GlobalResources.env_vars.mainChain.addBlock(newBlock);
//				
//			}
			
//			Message msg = new Message(msgType.REQUEST, contentType.BLOCKCHAIN);
//			RspHandler handler = new RspHandler();
//			client.send(client.init(InetAddress.getByName("192.168.0.23"), 1111), Message.serialize(msg), handler);
//			handler.waitForResponse();
			System.out.println("Done");
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
