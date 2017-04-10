package nonInteractiveVerification;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import globalResources.Env_Variables;
import globalResources.GlobalResources;

public class Verification {
	private BigInteger a, c, cTilda, r, puz, sol;
	private MessageDigest sha256digest;
	
	public Verification(HashMap<String, BigInteger> message){
		a= message.get("a");
		System.out.println("a: "+a);
		c= message.get("c");
		System.out.println("c: "+c);
		cTilda = message.get("cTilda");
		System.out.println("cTilda: "+cTilda);
		r= message.get("r");
		System.out.println("r: "+r);
		puz= message.get("puz");
		System.out.println("puz: "+puz);
		sol = message.get("sol");
		System.out.println("sol: "+sol);
	}
	
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
	 * Finds SHA-256 Hash of supplied double input
	 */
	private BigInteger getSHA256Hash(BigInteger ip) throws NoSuchAlgorithmException
	{
		try {
			if(sha256digest==null)
			{
				sha256digest = MessageDigest.getInstance("SHA-256");
			}
			byte[] hash = sha256digest.digest(ip.toByteArray());
			return new BigInteger(hash); 
		}
		 catch (NoSuchAlgorithmException e) {
				throw e;
			}
			catch(Exception exp){
				throw exp;
			}
	}
	
	/*
	 * Verify if the Non-Interactive proof verifies
	 */
	public Boolean verify(){
		
		try {
			if(c.equals(getSHA256Hash(a)))
			{
				System.out.println("C=H(a)");
				if(cTilda.equals(c.xor(puz)))
				{
					System.out.println("cTilda = c XOR puz");
					
					if(puz.equals(getSHA256Hash(sol)))
					{
						System.out.println("puz=H(sol)");
						
						BigInteger lhs = GlobalResources.env_vars.Exponentiate(r); //g^r
						System.out.println("g^r :"+lhs);
						
						//BigInteger cTildaModQ= cTilda.mod(GlobalResources.env_vars.getQ());
						//System.out.println("CTildaModQ :"+cTildaModQ);
						
						BigInteger xRaisedcTilda=GlobalResources.env_vars.ExponentiateMToN(GlobalResources.env_vars.getxElement(), cTilda);
						
						BigInteger rhs = a.multiply(xRaisedcTilda);
						rhs=rhs.mod(GlobalResources.env_vars.getP());
						System.out.println("(a * x^cTilda) :"+rhs);
						
						if(lhs.equals(rhs))
						{
							System.out.println("lhs == rhs");
							System.out.println("True");
							return true;
						}
					}	
				}
			}
			System.out.println("False");
			return false;
		}
//		catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

}
