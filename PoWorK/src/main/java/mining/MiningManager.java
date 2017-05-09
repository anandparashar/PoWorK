package mining;

import java.net.InetAddress;

import globalResources.GlobalResources;
import mining.Miner.type;
import network.MessageProcessor;
import network.NioClient;
import network.NioServer;

public class MiningManager{
	
	private Miner miner = new Miner(type.KNOWLEDGE);
	
	public void mine(){
		try{
			
			// TODO : Add logic to get BlockChain from peers
			
			MessageProcessor processor = new MessageProcessor();
			new Thread(processor).start();
			NioServer server = new NioServer(InetAddress.getByName("10.159.8.121"), 100, processor);
			Thread tserver = new Thread(server);
			tserver.start();
					
			NioClient client = new NioClient();
			Thread tclient = new Thread(client);
			tclient.setDaemon(true);
			tclient.start();
			
			GlobalResources.env_vars.setFilepath("./resources/BlockChain/");
			
			GlobalResources.env_vars.globalServer = server;
			GlobalResources.env_vars.globalClient = client;
			
			GlobalResources.env_vars.manager = this;
			
//			Thread mine = new Thread(miner);
//			mine.start();
//			
//			GlobalResources.env_vars.miningThread = mine;
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
//		System.out.println("1");
//		synchronized (mine){
//			mine.checkAccess();
//			mine.suspend();
//			System.out.println("waiting");
//		}
	}

	public Miner getMiner() {
		return miner;
	}

	public void setMiner(Miner miner) {
		this.miner = miner;
	}
	
	public static void main(String args[]){
		MiningManager manager = new MiningManager();
		manager.mine();
	}
	
}
