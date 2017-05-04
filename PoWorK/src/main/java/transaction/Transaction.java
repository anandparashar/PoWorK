package transaction;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author anand
 *
 */
public class Transaction {
	
	private BigInteger txHash;
	private static MessageDigest sha256digest ;
	
	/*
	 * 	Returns a random BigInteger of 256 bits length
	 */
	private static BigInteger rand256(){
		SecureRandom r = new SecureRandom();
		return new BigInteger(2, r);
	}
	
	/*
	 *	Finds SHA-256 Hash of supplied BigInteger input
	 */
	private static BigInteger getSHA256Hash(BigInteger ip) throws NoSuchAlgorithmException
	{
		try {
			if(sha256digest==null)
			{
				sha256digest = MessageDigest.getInstance("SHA-256");
			}
			return new BigInteger(sha256digest.digest(ip.toByteArray()));
		}
		 catch (NoSuchAlgorithmException e) {
				throw e;
			}
			catch(Exception exp){
				throw exp;
			}
	}
	
	public Transaction()
	{
		try {
			this.txHash= getSHA256Hash(rand256());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BigInteger getTxHash() {
		return txHash;
	}
}
