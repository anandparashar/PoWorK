package globalResources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.activemq.console.command.CreateCommand;
import org.bouncycastle.util.BigIntegers;

import blockchain.Block;
import blockchain.BlockChain;
import edu.biu.scapi.primitives.*;
import edu.biu.scapi.primitives.dlog.*;
import edu.biu.scapi.primitives.dlog.groupParams.GroupParams;
import edu.biu.scapi.primitives.dlog.groupParams.ZpGroupParams;
import edu.biu.scapi.primitives.dlog.openSSL.OpenSSLDlogECF2m;
import edu.biu.scapi.primitives.dlog.openSSL.OpenSSLDlogZpSafePrime;
import edu.biu.scapi.primitives.dlog.openSSL.OpenSSLZpSafePrimeElement;


/**
 * A class to store all the global variables that would be required across various classes of the application
 * @author Anand
 * 
 */
public class Env_Variables {
	
	/**
	 * BlockChain File path
	 */
	private String filepath = null;
	private String filename = "BlockChain.ser";
	public BlockChain mainChain = new BlockChain();
	
	//Proof
	private BigInteger throttle = new BigInteger("100000");
	
	//DLOG 
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
	
	public GroupElement convertToGroupElement(BigInteger ele){
		return dlog.generateElement(false, ele);
	}
	
	
	/**
	 * 
	 * DO NOT USE FOR NOW. Java.math.BigDecimal class throwing deserialization error
	 * 
	 * Tries to find {@link BlockChain} file at the specified path.
	 * If not found, checks default location {@link "/resources/BlockChain/"},
	 * else, returns false indicating a request to peers for it.
	 * If file is found, Blocks from the file are added to the {@link mainChain}
	 * @param filePath
	 * @return true if file is found, false if not. If file is found at default location,  {@link filePath} is updated.
	 */
	public boolean checkBlockChain(String filePath){
		Boolean chainFound = false;
		try{
			
			File file = new File(filePath+this.filename);
			
			if(file.exists())
			{
				FileInputStream fr = new FileInputStream(file);
				ObjectInputStream br = new ObjectInputStream(fr);
				
				Block blck1 = (Block) br.readObject();
				Block blck2 = (Block) br.readObject();
				System.out.println("Read: "+blck1.getIndex());
				System.out.println("Read: "+blck2.getIndex());
//				String sCurrentLine;
//				
//				if((sCurrentLine = br.readLine())==null){
//					return false;
//				}
//				else{
//					mainChain.addBlock(sCurrentLine.getBytes());
//				}
//
//				while ((sCurrentLine = br.readLine()) != null) {
//					mainChain.addBlock(sCurrentLine.getBytes());
//				}
				return true;
			}
			else
			{
				throw new FileNotFoundException();
			}	
		}
		catch(FileNotFoundException fe)
		{
			if(!filePath.equalsIgnoreCase("./resources/BlockChain/")){
				System.out.println("BlockChain not found at file path"+filePath);
				filePath = "./resources/BlockChain/";
				System.out.println("Checking at default location"+filePath);
				chainFound = checkBlockChain(filePath);
				if(chainFound){
					this.filepath="./resources/BlockChain/";
				}
			}
			else
			{
				System.out.println("BlockChain Not found at default location, checking with peers");
				return false;
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return chainFound;
	}
	
	
	//************************************************Crude Test************************************************************************
	
	public static void main(String args[])
	{
		Env_Variables env = new Env_Variables();
		env.setFilepath("./resources/BlockChain/"+env.filename);
		System.out.println(env.checkBlockChain(env.getFilepath()));
		env.mainChain.toString();
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

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public BigInteger getThrottle() {
		return throttle;
	}

	public void setThrottle(BigInteger throttle) {
		this.throttle = throttle;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}

