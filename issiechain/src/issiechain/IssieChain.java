
package issiechain;

import java.util.ArrayList;
import java.security.Security;
import java.util.HashMap;




public class IssieChain {
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static int difficulty = 5;
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //list of all unspent transactions.
	public static Wallet JamesWallet;
	public static Wallet IssieWallet;
	public static float minimumTransaction = 0.1f;
	public static Transaction genesisTransaction;

	public static void main(String[] args) {	
		//Setup Bouncey castle as a Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
		//Create wallets
		JamesWallet = new Wallet();
		IssieWallet = new Wallet();
		//Create Coinbase Wallet
		Wallet coinbase = new Wallet();
		//Test Public and Private keys
		System.out.println("Printing Public AND Private Keys...:");
		System.out.println("James' Private Key = : " +
				StringUtil.getStringFromKey(JamesWallet.privateKey));
		System.out.println("James' Public Key = : " + 
				StringUtil.getStringFromKey(JamesWallet.publicKey));
		System.out.println("Issies Private Key = : " + 
				StringUtil.getStringFromKey(IssieWallet.privateKey));
		System.out.println("Issies Public Key = : " + 
				StringUtil.getStringFromKey(IssieWallet.publicKey));


		//create genesis transaction, which sends 5000 IssieCoin to JamesWallet
		genesisTransaction = new Transaction(coinbase.publicKey, JamesWallet.publicKey, 5000f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction	
		genesisTransaction.transactionId = "0"; //manually set the transaction id
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.
		
		System.out.println("\nCreating and Mining Genesis block... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);

		//testing
		Block block1 = new Block(genesis.hash);
		System.out.println("\nGiving JamesWallet a Balance of 5000 IssieCoin");
		System.out.println("James' Wallet's balance is: " + JamesWallet.getBalance());
		System.out.println("\n\nJamesWallet is Attempting to send (44) IssieCoin to IssieWallet...");
		block1.addTransaction(JamesWallet.sendFunds(IssieWallet.publicKey, 44f));
		addBlock(block1);
		System.out.println("JamesWallet's balance is: " + JamesWallet.getBalance());
		System.out.println("IssieWallet's balance is: " + IssieWallet.getBalance());
		
		Block block2 = new Block(block1.hash);
		System.out.println("\n\nJamesWallet is Attempting to send (6000) IssieCoin, which is more funds than it has...");
		block2.addTransaction(JamesWallet.sendFunds(IssieWallet.publicKey, 6000f));
		addBlock(block2);
		System.out.println("JamesWallet's balance is: " + JamesWallet.getBalance());
		System.out.println("IssieWallets's balance is: " + IssieWallet.getBalance());
		
		Block block3 = new Block(block2.hash);
		System.out.println("\n\nIssieWallet is Attempting to send (20) IssieCoin to JamesWallet...");
		block3.addTransaction(IssieWallet.sendFunds( JamesWallet.publicKey, 20));
		System.out.println("JamesWallet's balance is: " + JamesWallet.getBalance());
		System.out.println("IssieWallet's balance is: " + IssieWallet.getBalance());
		
		isChainValid();
}
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifiySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
		}
		System.out.println("\nVerifying Blockchain");
		System.out.println("Blockchain =  valid");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
}