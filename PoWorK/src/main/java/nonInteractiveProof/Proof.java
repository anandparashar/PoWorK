package nonInteractiveProof;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Proof {
	public BigInteger cTilda;
	public BigInteger r;
	public BigInteger c;
	public BigDecimal a;
	public BigInteger puz;
	public BigInteger sol;
	public BigInteger x;
	public BigInteger g;
//	BigInteger p;
	@Override
	public String toString() {
		return "Proof [cTilda=" + cTilda + ", r=" + r + ", c=" + c + ", a=" + a + ", puz=" + puz + ", sol=" + sol
				+ ", x=" + x + ", g=" + g + "]";
	}
	

}
