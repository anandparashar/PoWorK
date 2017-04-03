package nonInteractiveProof;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import globalResources.Env_Variables;

public class Work {

	private static MessageDigest sha256digest ;

	/*
	 * 	Returns a random BigInteger of 256 bits length
	 */
	private static BigInteger rand256(){
		SecureRandom r = new SecureRandom();
		return new BigInteger(256, r);
	}
	
	/*
	 * 	Returns the byte[] equivalent of the double
	 */
	private static byte[] toByteArray(double value) {
	    byte[] bytes = new byte[8];
	    ByteBuffer.wrap(bytes).putDouble(value);
	    return bytes;
	}
	
	/*
	 * Returns the double equivalent of the byte[]
	 */
	private static double toDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}
	
	/*
	 *	Finds SHA-256 Hash of supplied double input
	 */
	private static double getSHA256Hash(double ip) throws NoSuchAlgorithmException
	{
		try {
			if(sha256digest==null)
			{
				sha256digest = MessageDigest.getInstance("SHA-256");
			}
			return toDouble(sha256digest.digest(toByteArray(ip))); 
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
	private static double findSol(Double puz) throws NoSuchAlgorithmException{
		Double sol = new Double(0);
		try {
			while(getSHA256Hash(puz)!=puz){
				sol++;
			}
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
	
	
	public static HashMap<String, Double> provideProof(BigInteger y, BigInteger g) throws NoSuchAlgorithmException{
		HashMap<String, Double> messageDict = new HashMap<String, Double>();
		
		double cTilda = rand256().doubleValue();
		messageDict.put("cTilda", cTilda);
		System.out.println("************************************************************************************************\n");
		System.out.println("cTilda :"+cTilda);
		System.out.println("************************************************************************************************\n");
		
		double r = rand256().doubleValue();
		messageDict.put("r", r);
		System.out.println("************************************************************************************************\n");
		System.out.println("r :"+r);
		System.out.println("************************************************************************************************\n");
		
		double a = Math.pow(Env_Variables.g.doubleValue(), r)/(Math.pow(Env_Variables.x.doubleValue(), cTilda));
		messageDict.put("a", a);
		System.out.println("************************************************************************************************\n");
		System.out.println("a :"+a);
		System.out.println("************************************************************************************************\n");
		
		double c = getSHA256Hash(a);
		messageDict.put("c", c);
		System.out.println("************************************************************************************************\n");
		System.out.println("c :"+c);
		System.out.println("************************************************************************************************\n");
		
		double puz = doubleXOR(c, cTilda);
		messageDict.put("puz", puz);
		System.out.println("************************************************************************************************\n");
		System.out.println("Puz :"+puz);
		System.out.println("************************************************************************************************\n");
		
		double sol = findSol(puz);
		messageDict.put("sol", sol);
		System.out.println("************************************************************************************************\n");
		System.out.println("sol found out to be :"+sol);
		System.out.println("************************************************************************************************\n");
		
		return messageDict;
	}
}