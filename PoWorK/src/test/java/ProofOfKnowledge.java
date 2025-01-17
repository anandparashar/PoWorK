import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;

import nonInteractiveProof.Knowledge;
import nonInteractiveVerification.Verification;

public class ProofOfKnowledge {

	@Test
	public void test() {
		Knowledge know = new Knowledge();
		
		try {
			Verification verify = new Verification(know.provideProof());
			Assert.assertEquals(true, verify.verify());
		} catch (NoSuchAlgorithmException e) {
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
