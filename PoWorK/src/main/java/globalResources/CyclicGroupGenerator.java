package globalResources;

import java.math.*;
import java.security.SecureRandom;
import java.util.*;

public class CyclicGroupGenerator {
	
	public void generateKey()
	{
	    SecureRandom r = new SecureRandom();
	    BigInteger i ;
	    boolean safePrime;
	    ArrayList<BigInteger> numbers = new ArrayList();

	    do{
		    i = new BigInteger(256, 90, r);
		    System.out.println("Number :"+ i);
		    //i = new BigInteger("11");
		    safePrime = isSafePrime(i);
		    System.out.println("Is a safe prime? "+ safePrime);
	    }
	    while(!safePrime);
	    numbers = zGenerator(i, safePrime, 10);
	    System.out.println(numbers);
	}

	public static boolean isSafePrime(BigInteger i)
	{
	    return i.subtract(BigInteger.ONE).divide(new BigInteger("2")).isProbablePrime(90);            
	}

	public static ArrayList<BigInteger> zGenerator(BigInteger i, boolean safePrime, int count)
	{
	    Set<BigInteger> numbers = new TreeSet<BigInteger>();

	    if(safePrime)
	    {
	        BigInteger num = new BigInteger("2");
	        BigInteger exp = i.subtract(BigInteger.ONE).divide(new BigInteger("2"));
	        do
	        {
	            if(num.modPow(exp, i).compareTo(BigInteger.ONE) > 0)
	                numbers.add(num);
	            else
	                numbers.add(num.negate().mod(i));
	            num = num.add(BigInteger.ONE);
	        }while((--count > 0) && (num.compareTo(i) < 0));
	    }

	    return (new ArrayList<BigInteger>(numbers));
	}
	

}
