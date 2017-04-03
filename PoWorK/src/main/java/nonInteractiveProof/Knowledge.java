package nonInteractiveProof;

import java.math.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import globalResources.Env_Variables;

public class Knowledge {
	
	private static MessageDigest sha256digest ;
	
	/*
	 * 	Returns the byte[] equivalent of the double
	 */
	public static byte[] toByteArray(double value) {
	    byte[] bytes = new byte[8];
	    ByteBuffer.wrap(bytes).putDouble(value);
	    return bytes;
	}
	
	/*
	 * Returns the double equivalent of the byte[]
	 */
	public static double toDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}
	
	/*
	 * 	Returns a random BigInteger of 256 bits length
	 */
	public static BigInteger rand256(){
		SecureRandom r = new SecureRandom();
		return new BigInteger(256, r);
	}
	
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
		
	public static HashMap<String, Double> provideProof(BigInteger y, BigInteger g) throws NoSuchAlgorithmException{
		HashMap<String, Double> messageDict = new HashMap<String, Double>();
		//get y from Zq
		//g is the generator of Zq
		try {
			double a = Math.pow(g.doubleValue(), y.doubleValue());
			System.out.println("************************************************************************************************\n");
			System.out.println("a :"+a);
			System.out.println("************************************************************************************************\n");
			messageDict.put("a", a);
			
			double c = getSHA256Hash(a);
			messageDict.put("c", c);
			System.out.println("************************************************************************************************\n");
			System.out.println("c :"+c);
			System.out.println("************************************************************************************************\n");
			
			//Generate Random Solution of 256 bits
			//TODO: Check and implement proper and efficient generation
			//It might include determining the difficulty according to hashing power available
			//and creating puzzle ofe appropriate difficulty
			double sol = rand256().doubleValue();
			messageDict.put("sol", sol);
			System.out.println("************************************************************************************************\n");
			System.out.println("Random Solution generated to be :"+sol);
			System.out.println("************************************************************************************************\n");
			
			double puz = getSHA256Hash(sol);
			messageDict.put("puz", puz);
			System.out.println("************************************************************************************************\n");
			System.out.println("Puz found to be :"+puz);
			System.out.println("************************************************************************************************\n");
			
			double cTilda = Double.longBitsToDouble(Double.doubleToRawLongBits(c)^Double.doubleToRawLongBits(puz));
			messageDict.put("cTilda", cTilda);
			System.out.println("************************************************************************************************\n");
			System.out.println("cTilda :"+cTilda);
			System.out.println("************************************************************************************************\n");
			
			double rTilda= y.doubleValue()+ ((cTilda*Env_Variables.w.doubleValue())%Env_Variables.q.doubleValue());
			messageDict.put("r", rTilda);
			System.out.println("************************************************************************************************\n");
			System.out.println("rTilda :"+rTilda);
			System.out.println("************************************************************************************************\n");
			
			return messageDict;			
		}
		 catch (NoSuchAlgorithmException e) {
				throw e;
			}
			catch(Exception exp){
				throw exp;
			}
	}
}
