
package issiechain;

import java.util.ArrayList;
import java.security.Security;



public class IssieChain {
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static int difficulty = 5;
	public static Wallet JamesWallet;
	public static Wallet IssieWallet;

	public static void main(String[] args) {	
		//Setup Bouncey castle as a Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
		//Create wallets
		JamesWallet = new Wallet();
		IssieWallet = new Wallet();
		//Test Public and Private keys
		System.out.println("James' Private Key = : " +
				StringUtil.getStringFromKey(JamesWallet.privateKey));
		System.out.println("James' Public Key = : " + 
				StringUtil.getStringFromKey(JamesWallet.publicKey));
		System.out.println("Issies Private Key = : " + 
				StringUtil.getStringFromKey(IssieWallet.privateKey));
		System.out.println("Issies Public Key = : " + 
				StringUtil.getStringFromKey(IssieWallet.publicKey));
		
		//Create a test transaction from JamesWallet to IssieWallet
		Transaction transaction = new Transaction(JamesWallet.publicKey, IssieWallet.publicKey, 5, null);
		transaction.generateSignature(JamesWallet.privateKey);
		//Verify signature works from public key
		System.out.println("Signature Verified: = " + transaction.verifiySignature());

		
}
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("Current Hashes do not equal");			
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("Previous Hashes do not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("This block has not been mined");
				return false;
			}
		}
		return true;
	}
}