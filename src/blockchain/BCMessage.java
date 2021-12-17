package blockchain;
import java.security.*;
import javax.crypto.Cipher;

import core.DTNHost;

import java.util.ArrayList;
import java.util.Arrays;

public class BCMessage {
	
	public int messageID;
	private transient PublicKey sender;
	private transient PublicKey recipient;
	private transient PublicKey sigKey;
	
	public DTNHost host;
	private String message;
	private transient byte[] cipher;
	private double creationTime;
	private double deliveryTime;
	private byte[] signature; 
	
	public ArrayList<String> alTypes = new ArrayList<String>(Arrays.asList(
			"Emergency",
			"Road Safety",
			"Improved Driving",
			"Business/Entertainment"));
	private int type;
	private String description;
	private boolean delivered = false;

	
	private static int sequenceShift = 0;
	
	public boolean isMine(PublicKey pk) {
		if(recipient == pk) {
			return true;
		}
		else return false;
	}
	public PublicKey getSigKey() {
		return sigKey;
	}
	// Constructor: 
	public BCMessage() {
		
	}
	public BCMessage(PublicKey from, PublicKey to, String message, int id, int type, String description) {//,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.message = message;
		this.messageID = id;
		this.type=type;
		this.description = description;
		try {
			encrypt(to, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.inputs = inputs;
	}
	public BCMessage(PublicKey from, PublicKey to, String message, int id, int type, String description, PrivateKey pk) {//,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.message = message;
		this.messageID = id;
		this.type=type;
		this.description = description;
		try {
			encrypt(to, message);
			generateSignature(pk);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.inputs = inputs;
	}
	// Constructor: 
	public BCMessage(PublicKey from, PublicKey to, String message, int id, int type) {//,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.message = message;
		this.messageID = id;
		this.type=type;
		try {
			encrypt(to, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.inputs = inputs;
	}
	public BCMessage(PublicKey from, PublicKey to, String message, int id, int type, PrivateKey sigPriv, PublicKey sigPub) {//,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.message = message;
		this.messageID = id;
		this.type=type;
		this.sigKey = sigPub;
		try {
			generateSignature(sigPriv);
			encrypt(to, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private String calulateHash() {
		sequenceShift++;
		return SignatureBuilder.applySha256(
				SignatureBuilder.getStringFromKey(sender) +
				SignatureBuilder.getStringFromKey(recipient) +
				message + sequenceShift
				);
	}

	public void generateSignature(PrivateKey privateKey) {
		String data = SignatureBuilder.getStringFromKey(sigKey) + SignatureBuilder.getStringFromKey(recipient) + message	;
		signature = SignatureBuilder.applyECDSASig(privateKey,data);	
		//System.out.println("verified: " + verifiySignature());
	}

	public boolean verifiySignature() {
		String data = SignatureBuilder.getStringFromKey(sigKey) + SignatureBuilder.getStringFromKey(recipient) + message	;
		return SignatureBuilder.verifyECDSASig(sigKey, data, signature);
	}
	public boolean verifiySignature(PublicKey pk) {
		String data = SignatureBuilder.getStringFromKey(pk) + SignatureBuilder.getStringFromKey(recipient) + message	;
		return SignatureBuilder.verifyECDSASig(pk, data, signature);
	}
	public void encrypt(PublicKey publicKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
        /*System.out.println("string");
        System.out.println(message);
        System.out.println("byte");
        System.out.println(message.getBytes());*/
        this.cipher = cipher.doFinal(message.getBytes());
        //return cipher.doFinal(message.getBytes());  
    }
    public static String decrypt(PrivateKey privateKey, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        return new String(cipher.doFinal(encrypted));
    }
	public PublicKey getSender() {
		return sender;
	}
	public void setSender(PublicKey sender) {
		this.sender = sender;
	}
	public PublicKey getRecipient() {
		return recipient;
	}
	public void setRecipient(PublicKey recipient) {
		this.recipient = recipient;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public byte[] getCipher() {
		return cipher;
	}
	public void setCipher(byte[] cipher) {
		this.cipher = cipher;
	}
	public byte[] getSignature() {
		return signature;
	}
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isDelivered() {
		return delivered;
	}
	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}
	public double getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(double creationTime) {
		this.creationTime = creationTime;
	}
	public double getDeliveryTime() {
		return deliveryTime;
	}
	public void setDeliveryTime(double deliveryTime) {
		this.deliveryTime = deliveryTime;
	}
}
