package blockchain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.GsonBuilder;

import core.DTNHost;
import core.SimClock;

public class BCNode {
	 // ArrayList to store the blocks
    public ArrayList<Block> blockchain
        = new ArrayList<Block>();
	public int difficulty = 4;
	private PrivateKey privateKey;
	private PublicKey publicKey;

	private PrivateKey privateKey2;
	private PublicKey publicKey2;
	
	private int interactions = 0;
	private int messagesexchanged = 0;
	public int messages = 0;
	
	private ArrayList<PublicKey> publicKeys = new ArrayList<PublicKey>();
	private ArrayList<PublicKey> SignaturePK = new ArrayList<PublicKey>();
	public ArrayList<PublicKey> getSignaturePK() {
		return SignaturePK;
	}
	public void setSignaturePK(ArrayList<PublicKey> signaturePK) {
		SignaturePK = signaturePK;
	}

	String[][] classificationDescription = {
			{"Emergency Response", "Support for authorities", "Incident occured"},
			{"Danger of collision", "Accident prevention system", "Abnormal traffic and road conditions"},
			{"Driver Assistance Systems", "Navigation", "Traffic Flow Improvement"},
			{"Vehicle Maintenance", "Mobile Services", "E-Payment", "Enterprise Solutions"}
	};
	public int debuglevel = 0;
	
	private boolean interacting = false;
	private boolean connected = false;
	public long connectionStart= 0;
	
	
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	public PublicKey getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	public PrivateKey getPrivateKey2() {
		return privateKey2;
	}
	public void setPrivateKey2(PrivateKey privateKey2) {
		this.privateKey2 = privateKey2;
	}
	public PublicKey getPublicKey2() {
		return publicKey2;
	}
	public void setPublicKey2(PublicKey publicKey2) {
		this.publicKey2 = publicKey2;
	}
	public boolean isInteracting() {
		return interacting;
	}
    public BCNode()
	{	
    	if (debuglevel == 1)System.out.println("Generating Keys...");
    	generateKeyPair();
    	try {
			buildKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (debuglevel == 1)System.out.println("Keys generated!");  
    	if (debuglevel == 1)System.out.println("PublicKey: " + publicKey); 
    	if (debuglevel == 1)System.out.println("PrivateKey: " + privateKey);   
    	String s = Base64.getEncoder().encodeToString(publicKey.getEncoded());
    	String t = Base64.getEncoder().encodeToString(privateKey.getEncoded());
    	if (debuglevel == 1)System.out.println("PublicKey: " + s); 
    	if (debuglevel == 1)System.out.println("PrivateKey: " + t);   
    	
    	if (debuglevel == 1)System.out.println("Node created");
	}
    public ArrayList<BCMessage> getMessages(){
    	ArrayList<BCMessage> alM = new ArrayList<BCMessage>();
		for (Block b : blockchain) {
			if (b.getMessage().isMine(publicKey2)) {
				alM.add(b.getMessage());
			}
		}
    	return alM;
    	
    }
    public ArrayList<BCMessage> getAllMessages(ArrayList<Block> alB){
    	ArrayList<BCMessage> alM = new ArrayList<BCMessage>();
		for (Block b : alB) {
			alM.add(b.getMessage());
		}
    	return alM;
    	
    }
   
    public void addBlock(Block b) {
    	blockchain.add(b);
    	//blockchain.get(blockchain.size()-1).mineBlock(difficulty);
    	if (b.genesis) blockchain.get(blockchain.size()-1).mineBlock(difficulty);
    	//else blockchain.get(blockchain.size()-1).mineBlock(b.getMessage().getType()+1);
    	else blockchain.get(blockchain.size()-1).mineBlock(4);
    	/*if(!b.getHash().startsWith("0000")) {
    		System.out.println("fehler: " + b.getHash());
    	}
    	if(!b.getPreviousHash().startsWith("0000") && b.getPreviousHash()!= "0") {
    		System.out.println("fehler: " + b.getPreviousHash());
    	}*/
    }
	
	public void buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 1024;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);  
        KeyPair kp = keyPairGenerator.generateKeyPair();
        privateKey2 = kp.getPrivate();
        publicKey2 = kp.getPublic();
        getPublicKeys().add(publicKey2);
        //return keyPairGenerator.genKeyPair();
    }
	
	public void generateKeyPair() {
    	try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
	        	KeyPair keyPair = keyGen.generateKeyPair();
	        	// Set the public and private keys from the keyPair
	        	privateKey = keyPair.getPrivate();
	        	publicKey = keyPair.getPublic();
	        	SignaturePK.add(publicKey);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public  void interact(DTNHost n1, DTNHost n2) {
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				long startTime = System.nanoTime();
				interacting = true;
				n2.setInteracting(true);
				//int tempMsgEx = messagesexchanged;
				//interactions ++;
				ArrayList<Block> al = new ArrayList<Block>();
				if (debuglevel == 1) System.out.println("oldkeys: ");
				for (PublicKey pk : n1.getPublicKeys()) {
					if (debuglevel == 1) System.out.println(pk.toString());
				}
				ArrayList<PublicKey> als = exchangeKeys(n1.getPublicKeys(), n2.getPublicKeys());
				n1.setPublicKeys(new ArrayList<PublicKey>(als));
				n2.setPublicKeys(new ArrayList<PublicKey>(als));
				ArrayList<PublicKey> alsk = exchangeKeys(n1.getSignaturePK(), n2.getSignaturePK());
				n1.setSignaturePK(new ArrayList<PublicKey>(alsk));
				n2.setSignaturePK(new ArrayList<PublicKey>(alsk));
				if(n1.getPublicKeys().size()>=10) {
					if (debuglevel == 1) System.out.println("10!");
				}
				if (debuglevel == 1) System.out.println("newkeys: ");
				for (PublicKey pk : n1.getPublicKeys()) {
					if (debuglevel == 1) System.out.println(pk.toString());
				}
				//BCMessage bc = generateMessage();
				//n1.addBlock(new Block(bc, n1.lastHash()));
				/*if(connected) */al = mergeBlockchain(n1.blockchain,n2.blockchain); 
				/*else {
					System.out.println("disconnected at mergeBlockchain");
					return;
				}*/
				if(isChainValid(al)) {
					//System.out.println(true);
				}
				//else System.out.println(false);
				/*if(connected) */n1.updateBlockchain(al); 
				checkForMessages(n1.blockchain);
				/*else {
					System.out.println("disconnected at updateBlockchain1");
					return;
				}*/
				if (debuglevel == 1) printBlockchain(n1.blockchain);
					/*if(connected) */n2.updateBlockchain(al); 
				checkForMessages(n2.blockchain);
				/*else {
					System.out.println("disconnected at updateBlockchain2");
					return;
				}*/
				if (debuglevel == 1) printBlockchain(n1.blockchain);
				//if(tempMsgEx != messagesexchanged) interactions--;
				interacting = false;
				n2.setInteracting(false);
				long stopTime = System.nanoTime();
				if (debuglevel == 1) System.out.println("TIME: " + (stopTime - startTime));
			}
		});
		thread.start();
		
	}
	
	public void checkForMessages(ArrayList<Block> b) {
		for (Block bl : b) {
			//System.out.println(bl);
			if (!bl.genesis && bl.getMessage().isMine(publicKey2)) {
				bl.getMessage().setDelivered(true);
				bl.getMessage().setDeliveryTime(SimClock.getTime());
			}
		}
	}
	public Boolean isChainValid(ArrayList<Block> b) {
		Block currentBlock; 
		Block previousBlock;
		int dif = 4;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		//System.out.println("target: " + hashTarget);
		//loop through blockchain to check hashes:
		for(int i=1; i < b.size(); i++) {
			//System.out.println(i);
			currentBlock = b.get(i);
			previousBlock = b.get(i-1);
			dif = currentBlock.getMessage().getType();
			hashTarget = new String(new char[dif]).replace('\0', '0');
			//compare registered hash and calculated hash:
			if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
				System.out.println("Current Hashes not equal");	
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.getHash().equals(currentBlock.getPreviousHash()) ) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.getHash().substring( 0, dif).equals(hashTarget)) {
				//System.out.println(currentBlock.hash);
				//System.out.println(previousBlock.hash);
				System.out.println("This block hasn't been mined");
				return false;
			}
		}
		return true;
	}
	
	public String lastHash() {
		return blockchain.get(blockchain.size()-1).getHash();
	}
	public BCMessage generateMessage() {
		int randomNum = ThreadLocalRandom.current().nextInt(0, getPublicKeys().size());
		PublicKey pk = getPublicKeys().get(randomNum);
		int type = ThreadLocalRandom.current().nextInt(0, 4);
		String description;
		if(type!=3) {
			randomNum = ThreadLocalRandom.current().nextInt(0, 3);
			description = classificationDescription[type][randomNum];
		}
		else {
			randomNum = ThreadLocalRandom.current().nextInt(0, 4);
			description = classificationDescription[type][randomNum];
		}
		BCMessage bcm = new BCMessage(publicKey2, pk, description, messages, type, privateKey, publicKey);
		messages++;
		
		return bcm;
	}
	public ArrayList<PublicKey> exchangeKeys(ArrayList<PublicKey> keys1, ArrayList<PublicKey> keys2) {
		
		if (debuglevel == 1) System.out.println("key1: " + keys1.size()+ " key2: " + keys2.size());
		if (debuglevel == 1) System.out.println("key1: " + keys1+ " key2: " + keys2);
		//keys1.removeAll(keys2);
		//keys2.addAll(keys1);
		//keys1 = new ArrayList<PublicKey>(keys2);
		Set<PublicKey> fooSet = new LinkedHashSet<>(keys1);
		fooSet.addAll(keys2);
		return new ArrayList<PublicKey>(fooSet);
	}
	public boolean addPublicKey(PublicKey pk) {
    	if(!getPublicKeys().contains(pk)) {
    		getPublicKeys().add(pk);
    		if (debuglevel == 1) System.out.println("PublicKey added!");
    		return true;
    	}
    	else {
    		if (debuglevel == 1) System.out.println("PublicKey already known!");
    		return false;
    	}
    }
	
	public void updateBlockchain(ArrayList<Block> al) {
    	blockchain = new ArrayList<Block>(al);
    }
	
	public void printBlockchain(ArrayList<Block> blockchain) {

		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		if (debuglevel == 1) System.out.println("\nThe block chain: ");
		if (debuglevel == 1) System.out.println(blockchainJson);
	}
	
	public ArrayList<Block> mergeBlockchain(ArrayList<Block> bchain1, ArrayList<Block> bchain2) {
		ArrayList<Block> gChain;
		ArrayList<Block> sChain;
		if(bchain1.size()>1 && bchain2.size()>1) {
			int a = 0;
		}
		if(bchain1.size()>=bchain2.size()) {
			gChain = bchain1;
			sChain = bchain2;
		}
		else {
			if(bchain1.size()==bchain2.size()) {
				if(bchain1.get(bchain1.size()).getNonce() >= bchain2.get(bchain2.size()).getNonce()) {
					gChain = bchain1;
					sChain = bchain2;
				}
				else {
					gChain = bchain2;
					sChain = bchain1;
				}
				
			}
			else {
				gChain = bchain2;
				sChain = bchain1;
			}
			
		}
		int linkID= getLastSharedBlockID(gChain,sChain)+1;
		if (debuglevel == 1) System.out.println(linkID);
		ArrayList<BCMessage> alM = getAllMessages(gChain);/*
		ArrayList<BCMessage> alS = getAllMessages(sChain);
		ArrayList<BCMessage> alT = new ArrayList();
		alT.addAll(alM);
		alT.addAll(alS);
		alT = removeDuplicates(alT);*/
		int tmp = 0;
		for (int i = linkID; i<sChain.size();i++) {
			BCMessage bcm = sChain.get(i).getMessage();
			if(!alM.contains(bcm)) {
				PublicKey pk = bcm.getSigKey();
				if(SignaturePK.contains(pk)) {
					if (debuglevel>1)System.out.println("Signature verified: " + bcm.verifiySignature(pk));
				}
				else {
					if (debuglevel>1)System.out.println("Unknown Key");
					if (debuglevel>1)System.out.println("Signature verified: " + bcm.verifiySignature(pk));
				}
				Block nextBlock = new Block(bcm, gChain.get(gChain.size()-1).getHash());
				gChain.add(nextBlock);
				//gChain.get(gChain.size()-1).mineBlock(bcm.getType()+1);
				gChain.get(gChain.size()-1).mineBlock(4);
				tmp++;
			}
			else if (debuglevel>1) System.out.println("Blockchain contains Message already!");
			
		}/*
		if(alT.containsAll(alM)) {
			if (debuglevel>1)System.out.println("got all!");
		}
		else {
			if (debuglevel>1)System.out.println("some missing!");
		}*/
		if (tmp >0) {
			messagesexchanged +=tmp;
			interactions++;
		}
		else if (debuglevel>1) System.out.println("No new messages added!");
			
		
		return gChain;
	}
	public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {
        ArrayList<T> newList = new ArrayList<T>();
  
        for (T element : list) {
            if (!newList.contains(element)) {
  
                newList.add(element);
            }
        }
        return newList;
    }
	public int getLastSharedBlockID(ArrayList<Block> gChain, ArrayList<Block> sChain) {
		for(int i = 0; i<sChain.size(); i++) {
			if(gChain.get(i).getHash()==sChain.get(i).getHash()) {
				
			}
			else return i -1;
			
		}
		return 0;
	}
	public ArrayList<PublicKey> getPublicKeys() {
		return publicKeys;
	}
	public void setPublicKeys(ArrayList<PublicKey> publicKeys) {
		this.publicKeys = publicKeys;
	}
	

	public boolean Interacting() {
		return interacting;
	}
	
	public void setInteracting(boolean b) {
		interacting = b;
	}
	
	public void setConnected(boolean con) {
		connected = con;
	}
	
	public boolean getConnected() {
		return connected;
	}
	public int getInteractions() {
		return interactions;
	}
	public void setInteractions(int interactions) {
		this.interactions = interactions;
	}
	public int getMessagesexchanged() {
		return messagesexchanged;
	}
	public void setMessagesexchanged(int messagesexchanged) {
		this.messagesexchanged = messagesexchanged;
	}
}
