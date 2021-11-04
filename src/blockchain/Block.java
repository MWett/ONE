package blockchain;

import java.security.PublicKey;
import java.util.Date;
public class Block {
	  
    // Every block contains
    // a hash, previous hash and
    // data of the transaction made
    private String hash;
    private String previousHash;
    //private String data;
    private BCMessage message;
    //public Transaction transactions;
    public long timeStamp;
    private int nonce;
    public int debuglevel = 0;
    private long minetime= 0;
    public boolean genesis = false;
  
    // Constructor for the block
    public Block(String data,
                 String previousHash)
    {
        //this.data = data;
    	genesis=true;
        this.previousHash
            = previousHash;
        this.timeStamp
            = new Date().getTime();
        this.hash
            = calculateHash();
    }
    public Block(BCMessage m,
    			 String previousHash) {
    	//this.transactions = transaction;
    	setMessage(m);
    	this.previousHash
        	= previousHash;
	    this.timeStamp
	        = new Date().getTime();
	    this.hash
	        = calculateHash();
    }
    
    public String calculateHash() {
		String calculatedhash = StringUtil.applySha256( 
				previousHash +
				Long.toString(timeStamp) +
				Integer.toString(nonce) + 
				getMessage() 
				);
		return calculatedhash;
	}
    
    public boolean verifiySignature(PublicKey pk) {
		boolean b = message.verifiySignature(pk);
		return b;
	}
    
    /*public String getData() {
    	return data;
    }*/
    public void mineBlock(int difficulty) {
		String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0" 
		long startTime = System.nanoTime();
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++;
			//System.out.println("nonce: " + nonce);
			hash = calculateHash();
		}
		if (debuglevel == 1) System.out.println("Block Mined!!! : " + hash);
		long stopTime = System.nanoTime();
		minetime = stopTime-startTime;
	}
	public BCMessage getMessage() {
		return message;
	}
	public void setMessage(BCMessage message) {
		this.message = message;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getPreviousHash() {
		return previousHash;
	}
	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}
	public int getNonce() {
		return nonce;
	}
	public void setNonce(int nonce) {
		this.nonce = nonce;
	}
	public long getMinetime() {
		return minetime;
	}
	public void setMinetime(long minetime) {
		this.minetime = minetime;
	}
}