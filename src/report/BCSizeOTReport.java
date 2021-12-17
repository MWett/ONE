/* 
 * Copyright 2010-2012 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.util.ArrayList;
import java.util.Collections;
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

import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;

public class BCSizeOTReport extends Report implements UpdateListener {

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
	/**
	 * Creates a new BufferOccupancyReport instance.
	 */
	public BCSizeOTReport() {
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
		double bufferOccupancy = 0.0;
		double bo2 = 0.0;
		int avgBCLength=0;
		int maxBCLength=0;
		ArrayList<Integer> medianL = new ArrayList();
		int median = 0;
		for (DTNHost h : hosts) {
			avgBCLength += h.blockchain.size();
			medianL.add(h.blockchain.size());
			if (h.blockchain.size()>maxBCLength) maxBCLength = h.blockchain.size();

		}
		Collections.sort(medianL);
		avgBCLength/=hosts.size();
		if(isEven(medianL)) {
			median = medianL.get((hosts.size()/2));
		}
		else {
			int m = hosts.size()/2;
			median = (medianL.get(m)+medianL.get(m-1))/2;
		}
		 
		/*for (DTNHost h : hosts) {
			double tmp = h.getBufferOccupancy();
			tmp = (tmp<=100.0)?(tmp):(100.0);
			bufferOccupancy += tmp;
			bo2 += (tmp*tmp)/100.0;
		}
		
		double E_X = bufferOccupancy / hosts.size();
		double Var_X = bo2 / hosts.size() - (E_X*E_X)/100.0;
		
		String output = format(SimClock.getTime()) + " " + format(E_X) + " " +
			format(Var_X);*/
		System.out.println("Median: " + median);
		System.out.println("Average: " + avgBCLength);
		System.out.println("Max: " + maxBCLength);
		String output = "\n" + format(time) + " " + avgBCLength+ " " + maxBCLength+ " " + median;
		write(output);
	}
	private boolean isEven(ArrayList al) {
		if ((al.size()%2) ==0) {
			return true;
		}
		else return false;
	}
}
