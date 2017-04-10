package nonInteractiveProof;

import java.math.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import globalResources.Env_Variables;
import globalResources.GlobalResources;

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
		
	public static HashMap<String, BigInteger> provideProof() throws NoSuchAlgorithmException{
		HashMap<String, BigInteger> messageDict = new HashMap<String, BigInteger>();
		//get y from Zq
		//g is the generator of Zq
		BigInteger g = GlobalResources.env_vars.getG();
		BigInteger y = GlobalResources.env_vars.createRandomElementInGroup();
		System.out.println("y :"+y);
		try {
			BigInteger a = (GlobalResources.env_vars.Exponentiate(y)).mod(GlobalResources.env_vars.getP());
			System.out.println("************************************************************************************************");
			System.out.println("a :"+a);
			System.out.println("************************************************************************************************\n");
			messageDict.put("a", a);
		
			
			BigInteger c = getSHA256Hash(a);
			messageDict.put("c", c);
			System.out.println("************************************************************************************************");
			System.out.println("c :"+c);
			System.out.println("************************************************************************************************\n");
			
//			//Generate Random Solution of 256 bits
//			//TODO: Check and implement proper and efficient generation
//			//It might include determining the difficulty according to hashing power available
//			//and creating puzzle of appropriate difficulty
			BigInteger sol = rand256();
			messageDict.put("sol", sol);
			System.out.println("************************************************************************************************");
			System.out.println("Random Solution generated to be :"+sol);
			System.out.println("************************************************************************************************\n");
//			
			BigInteger puz = getSHA256Hash(sol);
			messageDict.put("puz", puz);
			System.out.println("************************************************************************************************");
			System.out.println("Puz found to be :"+puz);
			System.out.println("************************************************************************************************\n");
//			
			BigInteger cTilda = c.xor(puz);
			messageDict.put("cTilda", cTilda);
			System.out.println("************************************************************************************************");
			System.out.println("cTilda :"+cTilda);
			System.out.println("************************************************************************************************\n");
//			
			BigInteger w = GlobalResources.env_vars.getW();
			BigInteger q = GlobalResources.env_vars.getQ();
			BigInteger mul = cTilda.multiply(w);
			System.out.println("CTilda*w :"+mul);
			//exp=exp.mod(q);
			//System.out.println("CTilda*w.Modq :"+exp);
			
			BigInteger rTilda= y.add(mul);
			rTilda  = rTilda.mod(q);
			
			messageDict.put("r", rTilda);
			System.out.println("************************************************************************************************");
			System.out.println("rTilda :"+rTilda);
			System.out.println("************************************************************************************************\n");
//			
			return messageDict;			
		}
		 catch (NoSuchAlgorithmException e) {
				throw e;
			}
		catch(Exception exp){
				throw exp;
		}
	}
	
	public static void main(String args[]){
		Knowledge know = new Knowledge();
		try {
			know.provideProof();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
