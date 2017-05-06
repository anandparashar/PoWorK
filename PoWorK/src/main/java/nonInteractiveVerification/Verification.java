package nonInteractiveVerification;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import globalResources.Env_Variables;
import globalResources.GlobalResources;
import nonInteractiveProof.Proof;

/**
 * @author anand
 *
 */
public class Verification {
	private BigInteger c, cTilda, r, puz, sol, throttle, g, x;
	private BigDecimal a;
	private MessageDigest sha256digest;
	
	public Verification(Proof proof){
		System.out.println("***********");
		a= proof.a;
		System.out.println("a: "+a);
		c= proof.c;
		System.out.println("c: "+c);
		cTilda = proof.cTilda;
		System.out.println("cTilda: "+cTilda);
		r= proof.r;
		System.out.println("r: "+r);
		puz= proof.puz;
		System.out.println("puz: "+puz);
		sol = proof.sol;
		System.out.println("sol: "+sol);
		g=proof.g;
		x=proof.x;
		System.out.println("***********");
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
	 * Finds SHA-256 Hash of supplied BigInteger input
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
	 * Verify if the Non-Interactive proof verifies
	 */
	/**
	 * @return
	 */
	public Boolean verify(){
		System.out.println("In Verification");
		try {
			if(c.equals(getSHA256Hash(a)))
			{
				System.out.println("C=H(a)");
				if(cTilda.equals(c.xor(puz)))
				{
					System.out.println("cTilda = c XOR puz");
					BigInteger puzThrottled = puz.divide(puz);
					if((puz.equals(getSHA256Hash(sol))) || (getSHA256Hash(sol).compareTo(puzThrottled)<=0))
					{
						System.out.println("puz=H(sol)");
						
						BigInteger lhs = GlobalResources.env_vars.ExponentiateMToN(GlobalResources.env_vars.convertToGroupElement(g), r); //g^r
						System.out.println("g^r :"+lhs);
						
						BigInteger xRaisedcTilda=GlobalResources.env_vars.ExponentiateMToN(GlobalResources.env_vars.convertToGroupElement(x), cTilda);
//						System.out.println("xraisedCTilda: "+ xRaisedcTilda);
//						System.out.println("xraisedCTilda Dec: "+ new BigDecimal(xRaisedcTilda));
						BigDecimal rhs = a.multiply(new BigDecimal(xRaisedcTilda));
						
//						System.out.println("a * x^cTilda: "+rhs);
						
						BigDecimal rhsrem=rhs.remainder(new BigDecimal(GlobalResources.env_vars.getP()));
//						System.out.println("(a * x^cTilda) rem:"+rhsrem);
						
						MathContext mc = new MathContext(10); 
						BigInteger rhsRounded = rhsrem.round(mc).toBigInteger();
						System.out.println("Rounded: "+rhsRounded);
						
						if(lhs.equals(rhsRounded))
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
