package nonInteractiveVerification;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import edu.biu.scapi.primitives.dlog.DlogGroup;
import edu.biu.scapi.primitives.dlog.GroupElement;
import edu.biu.scapi.primitives.dlog.ZpElementSendableData;
import edu.biu.scapi.primitives.dlog.openSSL.OpenSSLDlogZpSafePrime;
import edu.biu.scapi.securityLevel.Dlog;
import globalResources.Env_Variables;
import globalResources.GlobalResources;
import nonInteractiveProof.Proof;

/**
 * @author anand
 *
 */
public class Verification {
	private BigInteger c, cTilda, r, puz, sol, throttle, g, x, p, q;
	private BigDecimal a;
	private MessageDigest sha256digest;
	private DlogGroup dlog ;
	
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
		System.out.println("g: "+g);
		x=proof.x;
		System.out.println("x: "+x);
		p = proof.p;
		System.out.println("p: "+p);
		q = proof.q;
		throttle = GlobalResources.env_vars.getThrottle();
//		System.out.println("***********");
		dlog = new OpenSSLDlogZpSafePrime(q.toString(), g.toString(), p.toString());
	}
	
	private BigInteger getIntegerFromGroupELement(GroupElement gElement){
		try{
		return ((ZpElementSendableData)(gElement.generateSendableData())).getX();
			//return new BigInteger(dlog.decodeGroupElementToByteArray(gElement));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public BigInteger ExponentiateMToN(GroupElement x, BigInteger y){
		GroupElement exp= dlog.exponentiate(x, y);
		return getIntegerFromGroupELement(exp);
	}
	
	public GroupElement convertToGroupElement(BigInteger ele){
		return dlog.generateElement(false, ele);
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
					BigInteger puzThrottled = puz.divide(throttle);
					if((puz.equals(getSHA256Hash(sol))) || (getSHA256Hash(sol).compareTo(puzThrottled)<=0))
					{
						System.out.println("puz=H(sol)");
						
						BigInteger lhs = ExponentiateMToN(GlobalResources.env_vars.convertToGroupElement(g), r); //g^r
						System.out.println("g^r :"+lhs);
						
						BigInteger xRaisedcTilda=ExponentiateMToN(GlobalResources.env_vars.convertToGroupElement(x), cTilda);
						System.out.println("xraisedCTilda: "+ xRaisedcTilda);
						System.out.println("xraisedCTilda Dec: "+ new BigDecimal(xRaisedcTilda));
						BigDecimal rhs = a.multiply(new BigDecimal(xRaisedcTilda));
						
						System.out.println("a * x^cTilda: "+rhs);
						
						BigDecimal rhsrem=rhs.remainder(new BigDecimal(p));
						System.out.println("(a * x^cTilda) rem:"+rhsrem);
						
						MathContext mc = new MathContext(10); 
						BigInteger rhsRounded = rhsrem.round(mc).toBigInteger();
						System.out.println("Rounded: "+rhsRounded);
						
						if(lhs.equals(rhsRounded))
						{
//							System.out.println("lhs == rhs");
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
	
	
	public static Apfloat pow(Apfloat base, Apfloat exponent) {
		  
		  return ApfloatMath.pow(base, exponent);
		}
	
	public static void main (String args[])
	{
//		Apfloat p = pow(new Apfloat(new BigInteger("137140")), new Apfloat(new BigInteger("8279577052126148005872130454540812245912314573595538176583819678518404235512")));
//		System.out.println(p.toString());
		
		BigInteger a = BigInteger.TEN;
		System.out.println(a.toString());
	}


}
