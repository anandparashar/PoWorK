package blockchain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import globalResources.GlobalResources;

public class BlockChain {
	LinkedList<Block> blockChain = new LinkedList<Block>();
	
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

	@Override
	public String toString() {
		return "BlockChain [blockChain=" + blockChain + "]";
	}

}
