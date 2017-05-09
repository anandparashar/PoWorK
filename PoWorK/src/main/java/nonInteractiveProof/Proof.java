package nonInteractiveProof;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import blockchain.Block;

public class Proof implements Serializable{
	public BigInteger cTilda;
	public BigInteger r;
	public BigInteger c;
	public BigDecimal a;
	public BigInteger puz;
	public BigInteger sol;
	public BigInteger x;
	public BigInteger g;
	public BigInteger throttle;
	public BigInteger p;
	public BigInteger q;
	
//	private static final long serialVersionUID = 2L;
	
	
	@Override
	public String toString() {
		return "Proof [cTilda=" + cTilda + ", r=" + r + ", c=" + c + ", a=" + a + ", puz=" + puz + ", sol=" + sol
				+ ", x=" + x + ", g=" + g + "]";
	}
	
	/**
	 * Serializes a Proof for transmission.
	 * @param proof
	 * @return
	 * @throws IOException
	 */
	public static byte[] serialize (Proof proof) throws IOException
	{
		 try(ByteArrayOutputStream b = new ByteArrayOutputStream())
		 {
	            try(ObjectOutputStream o = new ObjectOutputStream(b))
	            {
	                o.writeObject(proof);
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
	 * De-serializes a Proof transmitted by a peer.
	 * @param prfByteArray
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws Exception
	 */
	public static Proof deserialize (byte[] prfByteArray) throws IOException, ClassNotFoundException, Exception{
		try
		{
			try(ByteArrayInputStream b = new ByteArrayInputStream(prfByteArray))
			{
	            try(ObjectInputStream o = new ObjectInputStream(b))
	            {
	            	return (Proof) o.readObject();
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
