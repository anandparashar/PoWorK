package globalResources;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.activemq.console.command.CreateCommand;
import org.bouncycastle.util.BigIntegers;

import edu.biu.scapi.primitives.*;
import edu.biu.scapi.primitives.dlog.*;
import edu.biu.scapi.primitives.dlog.groupParams.GroupParams;
import edu.biu.scapi.primitives.dlog.groupParams.ZpGroupParams;
import edu.biu.scapi.primitives.dlog.openSSL.OpenSSLDlogECF2m;
import edu.biu.scapi.primitives.dlog.openSSL.OpenSSLDlogZpSafePrime;
import edu.biu.scapi.primitives.dlog.openSSL.OpenSSLZpSafePrimeElement;

public class Env_Variables {
	/*
	 * A class to store all the global variables that would be required across various classes of the application
	 * @author Anand
	 * 
	 */
	
	private BigInteger g ;
	private BigInteger q ;
	private BigInteger p ;
	private BigInteger qMinusOne ;
	
	//Secret for PoK
	private BigInteger x;
	private BigInteger w ;
	
	private DlogGroup dlog;
	private GroupElement grpElement;
	private GroupElement xElement;
	
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
	
	public Env_Variables()
	{
		// initiate a discrete log group (in this case the OpenSSL implementation of the elliptic curve group K-233)
		
		try {
			dlog = new OpenSSLDlogZpSafePrime(20);
			ZpGroupParams grpParams = (ZpGroupParams) dlog.getGroupParams();
			System.out.println("Group Type: "+dlog.getGroupType());
			
			// get the group generator and order
			grpElement = dlog.getGenerator();
			this.g = getIntegerFromGroupELement(grpElement);
			System.out.println("g: "+g);
			
			this.q = dlog.getOrder();
			System.out.println("q: "+q);
			System.out.println("Is Prime Order? "+dlog.isPrimeOrder());
			this.qMinusOne = this.q.subtract(BigInteger.ONE);
			
			this.p = grpParams.getP();
			System.out.println("p :"+p);
			
			this.w = getIntegerFromGroupELement(dlog.createRandomElement());
			System.out.println("w: "+w);
			this.xElement = dlog.exponentiate(grpElement, w);
			this.x= getIntegerFromGroupELement(xElement);
			this.x = this.x.mod(this.p);
			this.xElement = dlog.generateElement(false, this.x);
			System.out.println("x: "+x);
			System.out.println("xElement :"+xElement);
			
			System.out.println("End of ENV_VARIABLES initalization");
//			// exponentiate g in r to receive a new group element
//			GroupElement g1 = dlog.exponentiate(grpElement, createRandomExponentInGroup());

			// create a random group element
//			GroupElement h = dlog.createRandomElement();

			// multiply elements
//			GroupElement gMult = dlog.multiplyGroupElements(g1, h);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public BigInteger createRandomElementInGroup()
	{
		SecureRandom random = new SecureRandom();
		// create a random exponent r
		return BigIntegers.createRandomInRange(BigInteger.ZERO, this.qMinusOne, random);
	}
	

//	public BigInteger createRandomElementInRange( BigInteger range)
//	{
//		SecureRandom random = new SecureRandom();
//		// create a random exponent r
//		return BigIntegers.createRandomInRange(BigInteger.ZERO, range, random);
//	}
//	
	
	public BigInteger createRandomGroupElement()
	{
		GroupElement h = dlog.createRandomElement();
		return getIntegerFromGroupELement(h);
	}
	
	public BigInteger Exponentiate(BigInteger y)
	{
		GroupElement exp = dlog.exponentiate(this.grpElement, y);
		//System.out.println("exp :"+exp);
		return getIntegerFromGroupELement(exp);
	}
	
	public BigInteger ExponentiateMToN(GroupElement x, BigInteger y){
		GroupElement exp= dlog.exponentiate(x, y);
		return getIntegerFromGroupELement(exp);
	}
	
	//************************************************Crude Test************************************************************************
	
	public static void main(String args[])
	{
		Env_Variables env = new Env_Variables();
		BigInteger y = env.createRandomGroupElement();
		env.Exponentiate(y);
	}
	
	//**********************************************************************************************************************************
	//Getters
	public BigInteger getG() {
		return g;
	}

	public BigInteger getQ() {
		return q;
	}

	public BigInteger getP() {
		return p;
	}

	public BigInteger getqMinusOne() {
		return qMinusOne;
	}

	public BigInteger getX() {
		return x;
	}

	public BigInteger getW() {
		return w;
	}
	
	public GroupElement getxElement() {
		return xElement;
	}
}

