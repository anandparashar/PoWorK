package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import blockchain.Block;
import blockchain.BlockChain;
import transaction.Transaction;


/**
 * Class to prepare a {@link Block} or the {@link BlockChain} or a {@link Transaction} to be transmitted to a peer.
 * Can also be used to decipher the message received from a peer.
 * Please use {@link serialize} to serialize and transmit a {@link Message} and {@link deserialize} to de-serialize a received object.
 * @author Anand
 *
 */
public class Message {
	private msgType messageType=null;
	private contentType content = null;
	public enum msgType{
		BROADCAST, REQUEST, RESPONSE
	};
	
	public enum contentType{
		BLOCK, TRANSACTION, BLOCKCHAIN
	};
	
	private Block block=null;
	private BlockChain chain = null;
	private Transaction tx = null;
	
	/**
	 * Constructor to transmit a {@link Block}
	 * @param msgType
	 * @param cnt
	 * @param blck
	 */
	public Message(msgType msgType, contentType cnt, Block blck){
		this.block = blck;
		this.messageType = msgType;
		this.content = cnt;
	}
	
	
	/**
	 * Constructor to transmit the view of the {@link BlockChain}
	 * @param msgType
	 * @param cnt
	 * @param chn
	 */
	public Message(msgType msgType, contentType cnt, BlockChain chn){
		this.chain = chn;
		this.messageType = msgType;
		this.content = cnt;
	}
	
	/**
	 * Constructor to transmit {@link Transaction}
	 * @param msgType
	 * @param cnt
	 * @param tx
	 */
	public Message(msgType msgType, contentType cnt, Transaction tx){
		this.tx = tx;
		this.messageType = msgType;
		this.content = cnt;
	}
	
	/**
	 * Serializes a block for transmission.
	 * @param msg an Object of {@link Message}
	 * @return serialized byte array of input parameter
	 * @throws IOException
	 */
	public static byte[] serialize (Message msg) throws IOException
	{
		 try(ByteArrayOutputStream b = new ByteArrayOutputStream())
		 {
	            try(ObjectOutputStream o = new ObjectOutputStream(b))
	            {
	                o.writeObject(msg);
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
	 * De-serializes a byte array transmitted by a peer to a {@link Message} object if valid 
	 * @param msgByteArray, the serialized byte array received via peer2peer communication
	 * @return {@link Message} object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws Exception
	 */
	public static Message deserialize (byte[] msgByteArray) throws IOException, ClassNotFoundException, Exception{
		try
		{
			try(ByteArrayInputStream b = new ByteArrayInputStream(msgByteArray))
			{
	            try(ObjectInputStream o = new ObjectInputStream(b))
	            {
	            	return (Message) o.readObject();
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
}