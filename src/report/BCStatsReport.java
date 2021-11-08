/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blockchain.BCMessage;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimClock;
import core.UpdateListener;

/**
 * Report for generating different kind of total statistics about message
 * relaying performance. Messages that were created during the warm up period
 * are ignored.
 * <P><strong>Note:</strong> if some statistics could not be created (e.g.
 * overhead ratio if no messages were delivered) "NaN" is reported for
 * double values and zero for integer median(s).
 */
public class BCStatsReport extends Report implements UpdateListener {
	
	
	private List<DTNHost> hosts;
	/**
	 * Constructor.
	 */
	public BCStatsReport() {
		init();
	}

	@Override
	protected void init() {
		super.init(true);
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
	public void updated(List<DTNHost> hosts) {
		this.hosts = hosts;
	}
	@Override
	public void done() {
		//write("Message stats for scenario " + getScenari oName() +  
		//		"\nsim_time: " + format(getSimTime()));  
		int nrofBCMessages = 0;
		int nrofDelivered = 0;
		int nrofInteractions = 0;
		int nrofExchanges = 0;
		ArrayList<BCMessage> alBCM = new ArrayList<BCMessage>();
		System.out.println("creating Messagelist ");
		for (DTNHost h : hosts) {
			alBCM.addAll(h.getAllMessages(h.blockchain));
			nrofInteractions += h.getInteractions();
			nrofExchanges += h.getMessagesexchanged();
			
		}
		System.out.println("Messagelist created " + alBCM.size());
		alBCM = removeDuplicates(alBCM);
		System.out.println("Duplicates removed " + alBCM.size());
		//alBCM.remove(0);
		BCMessage bcr = null;
		for (BCMessage bcm : alBCM) {
			if(bcm == null) bcr = bcm;
			else if(bcm.isDelivered())nrofDelivered++;
		}
		alBCM.remove(bcr);
		nrofBCMessages = alBCM.size();
		System.out.println("Delivered Messages calculated");
		String statsText = "\ncreated: " + nrofBCMessages + 
			"\ndelivered: " + nrofDelivered + 
			"\ninteractions: " + nrofInteractions + 
			"\nExchanges: " + nrofExchanges
			;
		
		write(statsText);
		super.done();
	}
	
}
