package blockchain;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.bouncycastle.asn1.cms.Time;

import nonInteractiveProof.Proof;
import nonInteractiveProof.Work;
import nonInteractiveVerification.Verification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author anand
 *
 */
public class Block implements Serializable{
	
//	private static final long serialVersionUID = 1L;
	
	@Override
	public String toString() {
		return "Block [index=" + index + ", prevBlockhashPointer=" + Arrays.toString(prevBlockhashPointer)
				+ ", timestamp=" + timestamp + ", merkelRootHash=" + Arrays.toString(merkelRootHash)
				+ ", currentBlockHash=" + Arrays.toString(currentBlockHash) + ", poWorK=" + poWorK.toString() + "]";
	}

	private BigInteger index;
	private byte[] prevBlockhashPointer;
	private String timestamp;
	private byte[] merkelRootHash;
	private byte[] currentBlockHash;
	// TODO: Add Proof
	private Proof poWorK;
	
	/**
	 * @return the index
	 */
	public BigInteger getIndex() {
		return index;
	}

	/**
	 * @return the prevBlockhashPointer
	 */
	public byte[] getPrevBlockhashPointer() {
		return prevBlockhashPointer;
	}

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the merkelRootHash
	 */
	public byte[] getMerkelRootHash() {
		return merkelRootHash;
	}

	/**
	 * @return the currentBlockHash
	 */
	public byte[] getCurrentBlockHash() {
		return currentBlockHash;
	}
	
	/**
	 * Serializes a block for transmission.
	 * @param blck
	 * @return
	 * @throws IOException
	 */
	public static byte[] serialize (Block blck) throws IOException
	{
		 try(ByteArrayOutputStream b = new ByteArrayOutputStream())
		 {
	            try(ObjectOutputStream o = new ObjectOutputStream(b))
	            {
	                o.writeObject(blck);
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
	 * De-serializes a block transmitted by a peer.
	 * @param blckByteArray
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws Exception
	 */
	public static Block deserialize (byte[] blckByteArray) throws IOException, ClassNotFoundException, Exception{
		try
		{
			try(ByteArrayInputStream b = new ByteArrayInputStream(blckByteArray))
			{
	            try(ObjectInputStream o = new ObjectInputStream(b))
	            {
	            	return (Block) o.readObject();
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
	
	//Constructors
	
	/**
	 * Constructor of Blockchain Block when block hash is known.
	 * @param in
	 * @param prevHash
	 * @param ts
	 * @param merkelRoot
	 * @param currentBlockhash
	 */
	public Block(BigInteger in, byte[] prevHash, String ts, byte[] merkelRoot, Proof proof, byte[] currentBlockhash){
		this.index=in;
		this.prevBlockhashPointer=prevHash;
		this.timestamp=ts;
		this.merkelRootHash= merkelRoot;
		this.currentBlockHash = currentBlockhash;
		this.poWorK=proof;
	}
	
	
	/**
	 * Constructor of Blockchain Block. Hashcode of the block is calculated using the supplied parameters.
	 *  
	 * @param in
	 * @param prevHash
	 * @param ts
	 * @param merkelRoot
	 */
	public Block(BigInteger in, byte[] prevHash, String ts, byte[] merkelRoot, Proof proof){
		this.index=in;
		this.prevBlockhashPointer=prevHash;
		this.timestamp=ts;
		this.merkelRootHash= merkelRoot;
		this.poWorK = proof;
		try {
			this.currentBlockHash = Block.hashCode(this);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Calculate hashcode of the current Block
	
	public static byte[] hashCode(Block blck) throws NoSuchAlgorithmException {
		byte[] indx= blck.getIndex().toByteArray();
		byte[] arrTS = blck.getTimestamp().getBytes();
		int dataLen= indx.length+ blck.getPrevBlockhashPointer().length+ arrTS.length +blck.getMerkelRootHash().length;
		byte[] blckData = new byte[dataLen];
		
		//Concatenate all fields into single byte array
		System.arraycopy(indx, 0, blckData, 0, indx.length);
		System.arraycopy(blck.getPrevBlockhashPointer(), 0, blckData, indx.length, blck.getPrevBlockhashPointer().length);
		System.arraycopy(arrTS, 0, blckData, indx.length+blck.getPrevBlockhashPointer().length, arrTS.length);
		System.arraycopy(blck.getMerkelRootHash(), 0, blckData, indx.length+blck.getPrevBlockhashPointer().length+arrTS.length, blck.getMerkelRootHash().length);
		
		MessageDigest sha256digest ;
		try
		{
			sha256digest = MessageDigest.getInstance("SHA-256");
			return sha256digest.digest(blckData);
		}
		 catch (NoSuchAlgorithmException e) {
				throw e;
			}
			catch(Exception exp){
				throw exp;
			}
		/*final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(currentBlockHash);
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		result = prime * result + Arrays.hashCode(merkelRootHash);
		result = prime * result + Arrays.hashCode(prevBlockhashPointer);
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;*/		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Block other = (Block) obj;
		if (!Arrays.equals(currentBlockHash, other.currentBlockHash))
			return false;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		if (!Arrays.equals(merkelRootHash, other.merkelRootHash))
			return false;
		if (!Arrays.equals(prevBlockhashPointer, other.prevBlockhashPointer))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

	/**
	 * Checks validity of {@link currBlock} w.r.t {@link prevBlock}. 
	 * Also, verifies {@link Proof} for {@link currBlock}. See, {@link Verification.verify} for more.
	 * @param currBlock
	 * @param prevBlock
	 * @return
	 */
	public static boolean isBlockvalid(Block currBlock, Block prevBlock){
		
		if (prevBlock != null) {
			if (currBlock.getIndex().equals(prevBlock.getIndex().add(BigInteger.ONE))) {
				if (Arrays.equals(currBlock.getPrevBlockhashPointer(), prevBlock.getCurrentBlockHash())) {
					try {
						if (Arrays.equals(Block.hashCode(currBlock), currBlock.getCurrentBlockHash())) {
							if (new Verification(currBlock.poWorK).verify()) {
								return true;
							}
						}
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return false;
		}
		else{
			System.out.println("Checking Genesis Block");
			//check if genesis block is valid
			if(currBlock.getIndex().equals(BigInteger.ZERO)){
				System.out.println("Index valid");
				if(Arrays.equals(currBlock.getPrevBlockhashPointer(),BigInteger.ZERO.toByteArray())){
					try {
						if(Arrays.equals(Block.hashCode(currBlock), currBlock.getCurrentBlockHash())){
							if (new Verification(currBlock.poWorK).verify()) {
								return true;
							}
						}
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	public static void main(String[] args) {
		
		Block blck=null;
		try {
			blck = new Block(BigInteger.ONE, new byte[10], "431243857", new byte[10], new Work().provideProof());
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			byte[] serializedBlock = Block.serialize(blck);
			
			for(int i =0; i<serializedBlock.length; i++)
			{
				System.out.print(serializedBlock[i]);
			}
			
			Block deBlock = Block.deserialize(serializedBlock);
			System.out.println("\n De-Serialized: ");
			System.out.println(deBlock.getIndex());
			System.out.println(deBlock.getTimestamp());
			System.out.println(deBlock.poWorK.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
