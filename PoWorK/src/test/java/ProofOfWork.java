import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;

import nonInteractiveProof.Work;
import nonInteractiveVerification.Verification;

public class ProofOfWork {

	@Test
	public void test() {
			Work work = new Work();
		
		
			Verification verify;
			try
			{
				verify = new Verification(work.provideProof());
				Assert.assertEquals(true, verify.verify());
			}
			catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Assert.fail(e.getMessage());
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Assert.fail(e.getMessage());
			}
	}

}