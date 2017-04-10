package nonInteractiveVerification;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import globalResources.Env_Variables;

public class Verification {
	private Double a, c, cTilda, r, puz, sol;
	private MessageDigest sha256digest;
	
	public Verification(HashMap<String, Double> message){
		a= message.get("a");
		c= message.get("c");
		cTilda = message.get("cTilda");
		r= message.get("r");
		puz= message.get("puz");
		sol = message.get("sol");
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
	private double getSHA256Hash(double ip) throws NoSuchAlgorithmException
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
	 * Verify if the Non-Interactive proof verifies
	 */
	public Boolean verify(){
		
		try {
			if(c==getSHA256Hash(a))
			{
				
				if(cTilda == Double.longBitsToDouble(Double.doubleToRawLongBits(c)^Double.doubleToRawLongBits(puz)))
				{
					double lhs = Math.pow(Env_Variables.g.doubleValue(), r);
					double xRaisedcTilda=Math.pow(Env_Variables.x.doubleValue(), cTilda);
					double rhs = xRaisedcTilda*a;
					if(lhs == rhs)
					{
						if(puz==getSHA256Hash(sol))
						{
							return true;
						}
					}	
				}
			}
			return false;
		}
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

}
