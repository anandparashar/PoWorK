package mining;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import blockchain.Block;
import globalResources.GlobalResources;
import network.NioClient;
import network.NioServer;
import nonInteractiveProof.Knowledge;
import nonInteractiveProof.Proof;
import nonInteractiveProof.Work;
import nonInteractiveVerification.Verification;

public class Miner {
	
	private Work pow;
	private Knowledge pok;
	
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
	
	public Miner(type minerType){
		switch(minerType){
			case WORK:
				pow = new Work();
				break;
			case KNOWLEDGE:
				pok = new Knowledge();
				break;
			default:
				
		};
	}
	
	private Proof provideProof(){
		if(pow != null){
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
		
		
		System.out.println("Block Generated: "+newBlock.toString());
		return newBlock;
	}
	
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
		
		
		System.out.println("Block Generated: "+newBlock.toString());
		System.out.println("Is Block Valid?"+Block.isBlockvalid(newBlock, prevBlock));
		return newBlock;
	}

	public static void main(String[] args) {
//		NioServer server = new NioServer(null, 100);
//		Thread serverThread = new Thread(server);
//		serverThread.start();
//		
//		NioClient client = new NioClient();
//		Thread clientThread = new Thread(client);
//		clientThread.setDaemon(true);
//		clientThread.start();
//		
//		Boolean checkChain = GlobalResources.env_vars.checkBlockChain("/resources/BlockChain/");
		
		Miner miner = new Miner(type.WORK);
		Block genesisBlock = miner.generateGenesisBlock();
		miner.generateBlock(genesisBlock);
		
	}

}
