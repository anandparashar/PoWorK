package nonInteractiveProof;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import globalResources.GlobalResources;

public class Work {

	private static MessageDigest sha256digest ;

	/*
	 * 	Returns a random BigInteger of 256 bits length
	 */
	private static BigInteger rand256(){
		SecureRandom r = new SecureRandom();
		BigInteger rand = new BigInteger(256, r);
		while(rand.equals(BigInteger.ZERO)){
			rand = new BigInteger(256, r);
		}
		return rand;
	}
	
	/*
	 * 	Returns the byte[] equivalent of the double
	 
	private static byte[] toByteArray(double value) {
	    byte[] bytes = new byte[8];
	    ByteBuffer.wrap(bytes).putDouble(value);
	    return bytes;
	}*/
	
	/*
	 * Returns the double equivalent of the byte[]
	 
	private static double toDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}*/
	
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
	
	private BigInteger getSHA256Hash(BigDecimal ip) throws NoSuchAlgorithmException
	{
		try {
			if(sha256digest==null)
			{
				sha256digest = MessageDigest.getInstance("SHA-256");
			}
			byte[] hash = sha256digest.digest(ip.unscaledValue().toByteArray());
			BigDecimal deci = new BigDecimal(new BigInteger(1, hash));
//			return new BigInteger(hash); 
			return deci.unscaledValue();
		}
		 catch (NoSuchAlgorithmException e) {
				throw e;
			}
			catch(Exception exp){
				throw exp;
			}
	}
	
	/*
	 * 	TODO: Check and implement proper and efficiently
	 */
	private static BigInteger findSol(BigInteger puz) throws NoSuchAlgorithmException{
//		BigInteger sol = BigInteger.ZERO;
		BigInteger sol = new BigInteger("0");
		BigInteger throttle = GlobalResources.env_vars.getThrottle();
		//making it easy
		System.out.println("Number of bits before throttling:"+ puz.bitLength());
		puz = puz.divide(throttle);
		System.out.println("Throttled: "+puz);
		System.out.println("Number of bits after throttling:"+ puz.bitLength());
		try {
			BigInteger hash=getSHA256Hash(sol);
			while(hash.abs().compareTo(puz.abs())>0)
			{
				sol=sol.add(BigInteger.ONE);
//				System.out.println("trying: "+sol);
				hash =getSHA256Hash(sol);
//				System.out.println("hash :"+hash.abs());
			}
			System.out.println("Solution: "+sol);
			System.out.println("Hash: "+hash.abs());
			return sol;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			throw e;
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	
	private static double doubleXOR(double a, double b){
		return Double.longBitsToDouble(Double.doubleToRawLongBits(a)^Double.doubleToRawLongBits(b));
	}
	
	
	public Proof provideProof() throws NoSuchAlgorithmException{
		
		System.out.println("Running");
		long startTime = System.currentTimeMillis();
//		HashMap<String, BigInteger> messageDict = new HashMap<String, BigInteger>();
		Proof proof = new Proof();
		BigInteger g = GlobalResources.env_vars.getG();
		BigInteger x = GlobalResources.env_vars.getX();
		
		BigInteger cTilda = rand256();
//		messageDict.put("cTilda", cTilda);
		proof.cTilda=cTilda;
//		System.out.println("************************************************************************************************\n");
//		System.out.println("cTilda :"+cTilda);
//		System.out.println("************************************************************************************************\n");
		
		BigInteger r = rand256();
//		messageDict.put("r", r);
		proof.r=r;
//		System.out.println("************************************************************************************************\n");
//		System.out.println("r :"+r);
//		System.out.println("************************************************************************************************\n");
		BigInteger gR = GlobalResources.env_vars.Exponentiate(r);
		BigInteger xC = GlobalResources.env_vars.ExponentiateMToN(GlobalResources.env_vars.getxElement(), cTilda);
//		System.out.println("gR: "+gR);
//		System.out.println("xC: "+xC);
		BigInteger a = gR.divide(xC);
		BigDecimal aDec;

		BigDecimal gRDec = new BigDecimal(gR);
		BigDecimal xCDec = new BigDecimal(xC);
		aDec = gRDec.divide(xCDec,10, BigDecimal.ROUND_HALF_UP);
//		System.out.println("a: "+aDec);
//		a = aDec.toBigInteger();
		
//		messageDict.put("a", a);
		proof.a = aDec;
//		System.out.println("************************************************************************************************\n");
//		System.out.println("a :"+aDec);
//		System.out.println("************************************************************************************************\n");
		
		BigInteger c = getSHA256Hash(aDec);
//		messageDict.put("c", c);
		proof.c=c;
//		System.out.println("************************************************************************************************\n");
//		System.out.println("c :"+c);
//		System.out.println("************************************************************************************************\n");
		
		BigInteger puz = c.xor(cTilda);
//		messageDict.put("puz", puz);
		proof.puz=puz;
//		System.out.println("************************************************************************************************\n");
		System.out.println("Puz :"+puz);
//		System.out.println("************************************************************************************************\n");
		
		BigInteger sol = findSol(puz);
//		messageDict.put("sol", sol);
		proof.sol=sol;
//		System.out.println("************************************************************************************************\n");
//		System.out.println("sol found out to be :"+sol);
//		System.out.println("************************************************************************************************\n");
		proof.g = g;
		proof.x = x;
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime-startTime;
		System.out.println("Time Taken: "+ timeTaken/60000.0+ " minutes.");
		return proof;
	}
	
	public static void main(String args[])
	{
		BigInteger sol = BigInteger.TEN;
		
		try {
			System.out.println(getSHA256Hash(sol));
			
			sol = sol.negate();
			
			System.out.println(getSHA256Hash(sol));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
