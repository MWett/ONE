package blockchain;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageClassification {
	public ArrayList<String> alTypes = new ArrayList<String>(Arrays.asList(
			"Emergency",
			"Road Safety",
			"Improved Driving",
			"Business/Entertainment"));
	public int type;
	public String description;
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
	public ArrayList<String> getAlTypes() {
		return alTypes;
	}
	public MessageClassification(int type, String description) {
		this.type=type;
		this.description=description;
	}
	
}
