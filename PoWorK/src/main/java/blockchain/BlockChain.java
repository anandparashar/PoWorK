package blockchain;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

import globalResources.GlobalResources;

public class BlockChain implements Serializable{
	LinkedList<Block> blockChain = new LinkedList<Block>();
	
	public Boolean isEmpty()
	{
		return blockChain.isEmpty();
	}
	
	
	public void addBlock(Block blck){
		try
		{
			if(blockChain.add(blck))
			{
				if(blockChain.indexOf(blck)>=10)
				{
					while(blockChain.indexOf(blck)<10)
					{						
						blockChain.removeFirst();
					}
				}
			}
			writeToFile(Block.serialize(blck));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void addBlock(byte[] blckByteArray)
	{
		try 
		{	
			Block blck = Block.deserialize(blckByteArray);
			
			if(blockChain.add(blck))
			{
				if(blockChain.indexOf(blck)>=10)
				{
					while(blockChain.indexOf(blck)<10)
					{						
						blockChain.removeFirst();
					}
				}
			}
			writeToFile(blckByteArray);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(ClassNotFoundException e)
		{
			System.out.println("Message corrupt");
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeToFile(byte[] blckByteArray) {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try 
		{
			
			File file = new File(GlobalResources.env_vars.getFilepath()+GlobalResources.env_vars.getFilename());
			
			if(!file.exists())
			{
				file.createNewFile();
			}
			
			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);

			bw.write(new String(blckByteArray));
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}
		
		
	}

	public boolean removeOldBlock()
	{
		try
		{
			if(!blockChain.isEmpty()){
				blockChain.removeFirst();
				return true;
			}else
			{
				return false;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public Block getLastBlock(){
		if(!blockChain.isEmpty()){
			return blockChain.peekLast();
		}
		return null;
	}
	
	/**
	 * Serializes a blockchain for transmission.
	 * @param blck
	 * @return
	 * @throws IOException
	 */
	public static byte[] serialize (BlockChain chain) throws IOException
	{
		 try(ByteArrayOutputStream b = new ByteArrayOutputStream())
		 {
	            try(ObjectOutputStream o = new ObjectOutputStream(b))
	            {
	                o.writeObject(chain);
	            }
	            catch(IOException e)
	            {
	            	throw e;
	            }
	            return b.toByteArray();
	     }catch(IOException e)
		 {
	        	throw e;
		 }
		 
	}
	
	
	/**
	 * De-serializes a blockchain transmitted by a peer.
	 * @param chainByteArray
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws Exception
	 */
	public static BlockChain deserialize (byte[] chainByteArray) throws IOException, ClassNotFoundException, Exception{
		try
		{
			try(ByteArrayInputStream b = new ByteArrayInputStream(chainByteArray))
			{
	            try(ObjectInputStream o = new ObjectInputStream(b))
	            {
	            	return (BlockChain) o.readObject();
	            }
			}catch(IOException e)
			{
	           	throw e;
	        }
		}catch(Exception e)
		{
			throw e;
		}
	}

	@Override
	public String toString() {
		return "BlockChain [blockChain=" + blockChain + "]";
	}

}
