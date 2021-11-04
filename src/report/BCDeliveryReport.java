/* 
 * Copyright 2010-2012 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.util.ArrayList;
/** 
 * Records the average buffer occupancy and its variance with format:
 * <p>
 * [Simulation time] [average buffer occupancy % [0..100] ] [variance]
 * </p>
 * 
 * <p>
 * The occupancy is calculated as an instantaneous snapshot every nth second
 * as defined by the <code>occupancyInterval</code> setting, not as an
 * average over time.
 * </p>
 * 
 * @author	teemuk
 */
import java.util.List;

import blockchain.BCMessage;
import blockchain.Block;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;

public class BCDeliveryReport extends Report implements UpdateListener {

	/**
	 * Record occupancy every nth second -setting id ({@value}). 
	 * Defines the interval how often (seconds) a new snapshot of buffer
	 * occupancy is taken
	 */
	public static final String BUFFER_REPORT_INTERVAL = "occupancyInterval";
	/** Default value for the snapshot interval */
	public static final double DEFAULT_BUFFER_REPORT_INTERVAL = 0.95;
	
	private double lastRecord = Double.MIN_VALUE;
	private double interval;
	double time;
	int BCMessageCreated = 0;
	int BCMessageDelivered = 0;
	/**
	 * Creates a new BufferOccupancyReport instance.
	 */
	public BCDeliveryReport() {
		super();
		
		Settings settings = getSettings();
		if (settings.contains(BUFFER_REPORT_INTERVAL)) {
			interval = settings.getInt(BUFFER_REPORT_INTERVAL);
		} else {
			interval = -1; /* not found; use default */
		}
		
		if (interval < 0) { /* not found or invalid value -> use default */
			interval = DEFAULT_BUFFER_REPORT_INTERVAL;
		}
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
		time = SimClock.getTime();
		if (time - lastRecord >= interval) {
			
			lastRecord = SimClock.getTime();
			printLine(hosts);
		}
	}
	
	/**
	 * Prints a snapshot of the average buffer occupancy
	 * @param hosts The list of hosts in the simulation
	 */
	private void printLine(List<DTNHost> hosts) {
		ArrayList<BCMessage> alBCM = new ArrayList<BCMessage>();
		for (DTNHost h : hosts) {
			ArrayList<Block> alb = new ArrayList<Block>(h.blockchain);
			
			alBCM.addAll(h.getAllMessages(alb));
			
		}
		alBCM = removeDuplicates(alBCM);
		int tmpDel = BCMessageDelivered;
		int tmpCre = BCMessageCreated;
		BCMessageDelivered = 0;
		for (BCMessage bcm : alBCM) {
			if(bcm!= null && bcm.isDelivered()) BCMessageDelivered++;
		}
		BCMessageCreated = alBCM.size();
		if (tmpDel!=BCMessageDelivered || tmpCre != BCMessageCreated) {
			String output = "\n" + format(time) + " " + BCMessageCreated+ " " + BCMessageDelivered;
			write(output);
		}
		
	}

}
