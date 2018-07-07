package issiechain;
import java.security.*;
import java.security.spec.ECGenParameterSpec;


public class Wallet  {
	public PrivateKey privateKey;
	public PublicKey publicKey;

	
	public Wallet(){
		generateKeyPair();	
	}
		
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize key generator and generate KeyPair
			keyGen.initialize(ecSpec, random);   //256 bytes
	        	KeyPair keyPair = keyGen.generateKeyPair();
	        	// Set public and private keys
	        	privateKey = keyPair.getPrivate();
	        	publicKey = keyPair.getPublic();
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
