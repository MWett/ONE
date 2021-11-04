/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import blockchain.BCMessage;
import blockchain.Block;
import routing.util.RoutingInfo;
import core.DTNHost;
import core.SimClock;

/**
 * A window for displaying routing information
 */
public class BlockchainInfoWindow extends JFrame implements ActionListener {
	private DTNHost host;
	private JButton refreshButton;
	private JButton printButton;
	private JCheckBox autoRefresh;
	private JScrollPane treePane;
	private JTree tree;
	private Timer refreshTimer;
	/** how often auto refresh is performed */
	private static final int AUTO_REFRESH_DELAY = 1000;
	
	public BlockchainInfoWindow(DTNHost host) {
		Container cp = this.getContentPane();
		JPanel refreshPanel = new JPanel();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		this.host = host;
		this.setLayout(new BorderLayout());
		refreshPanel.setLayout(new BorderLayout());
		this.autoRefresh = new JCheckBox("Auto refresh");
		this.autoRefresh.addActionListener(this);
		this.treePane = new JScrollPane();
		updateTree();
		
		cp.add(treePane, BorderLayout.CENTER);
		cp.add(refreshPanel, BorderLayout.SOUTH);

		this.printButton = new JButton("print");
		this.printButton.addActionListener(this);
		this.refreshButton = new JButton("refresh");
		this.refreshButton.addActionListener(this);
		refreshPanel.add(refreshButton, BorderLayout.EAST);
		refreshPanel.add(printButton, BorderLayout.EAST);
		refreshPanel.add(autoRefresh, BorderLayout.WEST);
		
		this.pack();		
		this.setVisible(true);
	}

	
	private void updateTree() {	
		super.setTitle("Routing Info of " + host + " at " + 
				SimClock.getFormattedTime(2));
		RoutingInfo ri = host.getRoutingInfo();
		ArrayList<Block> blockchain = host.getBCInfo();
		//DefaultMutableTreeNode top = new DefaultMutableTreeNode(ri);
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Blockchain");
		Vector<Integer> expanded = new Vector<Integer>();
		
		addChildren(top, blockchain);

		if (this.tree != null) { /* store expanded state */
			for (int i=0; i < this.tree.getRowCount(); i++) {
				if (this.tree.isExpanded(i)) {
					expanded.add(i);
				}
			}
		}
		
		this.tree = new JTree(top);
		
		for (int i=0; i < this.tree.getRowCount(); i++) { /* restore expanded */
			if (expanded.size() > 0 && expanded.firstElement() == i) {
				this.tree.expandRow(i);
				expanded.remove(0);
			}
		}
		
		this.treePane.setViewportView(this.tree);
		this.treePane.revalidate();
	}
	
	
	private void addChildren(DefaultMutableTreeNode node, RoutingInfo info) {
		for (RoutingInfo ri : info.getMoreInfo()) {
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(ri);
			node.add(child);
			// recursively add children of this info
			addChildren(child, ri);
		}
	}
	
	private void addChildren(DefaultMutableTreeNode node, ArrayList<Block> blockchain) {
		for (Block bc : blockchain) {
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(bc);
			node.add(child);
			addBlockInfo(child, bc);
			// recursively add children of this info
			//addChildren(child, bc);
		}
	}
	
	private void addBlockInfo(DefaultMutableTreeNode node, Block bc) {
		node.add(new DefaultMutableTreeNode("Hash: " + bc.getHash()));
		node.add(new DefaultMutableTreeNode("Previous-Hash: " + bc.getPreviousHash()));
		node.add(new DefaultMutableTreeNode("Timestamp: " + bc.timeStamp));
		node.add(new DefaultMutableTreeNode("Nonce: " +  bc.getNonce()));
		node.add(new DefaultMutableTreeNode("Minetime: " +  bc.getMinetime()));
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(bc.getMessage());
		node.add(child);
		//addMessageInfo(child, bc.getMessage());
		if(bc.getMessage() == null) {
			node.add(new DefaultMutableTreeNode("GenesisBlock"));
		}
		else {
			addMessageInfo(child, bc.getMessage());
		}
	}
	
	private void addMessageInfo(DefaultMutableTreeNode node, BCMessage bcm) {
		if(bcm == null) {
			node.add(new DefaultMutableTreeNode("GenesisBlock"));
		}
		else {
			node.add(new DefaultMutableTreeNode("Cipher: " + bcm.getCipher()));
			node.add(new DefaultMutableTreeNode("Message: " + bcm.getMessage()));
			node.add(new DefaultMutableTreeNode("MessageID: " + bcm.messageID));
			node.add(new DefaultMutableTreeNode("Recipient: " +  bcm.getRecipient()));
			node.add(new DefaultMutableTreeNode("Sender: " +  bcm.getSender()));
			node.add(new DefaultMutableTreeNode("Signature: " +  bcm.getSignature()));
			node.add(new DefaultMutableTreeNode("Delivered: " +  bcm.isDelivered()));
		}
		
	}
	public void serialize(final JTree tree) {
		try {
			XMLEncoder o = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(
					(System.getProperty("user.dir") + "testFile.xml"))));
			System.out.println(tree.getModel());
			o.writeObject(tree.getModel());
			o.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (s == this.refreshButton || s == this.refreshTimer) {
			updateTree();
		}
		if (e.getSource() == this.autoRefresh) {
			if (this.autoRefresh.isSelected()) {
				this.refreshTimer = new Timer(AUTO_REFRESH_DELAY, this);
				this.refreshTimer.start();
			} else {
				this.refreshTimer.stop();
			}
		}
		if (s == this.printButton){
			test(tree);
		}
	}
	private void test(JTree tree) {
		
		TreeModel model = tree.getModel();
	
	    try {
	
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        Document doc = factory.newDocumentBuilder().newDocument();
	        Element rootElement = doc.createElement("treeModel");
	
	        doc.appendChild(rootElement);
	
	        // Get tree root...
	        TreeNode root = (TreeNode) model.getRoot();
	
	        parseTreeNode(root, rootElement);
	
	        // Save the document to disk...
	
	        Transformer tf = TransformerFactory.newInstance().newTransformer();
	        tf.setOutputProperty(OutputKeys.INDENT, "yes");
	        tf.setOutputProperty(OutputKeys.METHOD, "xml");
	        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	
	        DOMSource domSource = new DOMSource(doc);
	        StreamResult sr = new StreamResult(new File("TreeModel.xml"));
	        tf.transform(domSource, sr);
	
	    } catch (ParserConfigurationException | TransformerException ex) {
	        ex.printStackTrace();
	    }
	}

	private void parseTreeNode(TreeNode treeNode, Node doc) {
	
	    Element parentElement = doc.getOwnerDocument().createElement("node");
	    doc.appendChild(parentElement);
	
	    // Apply properties to root element...
	    Attr attrName = doc.getOwnerDocument().createAttribute("name");
	    attrName.setNodeValue(treeNode.toString());
	    parentElement.getAttributes().setNamedItem(attrName);
	
	    Enumeration kiddies = treeNode.children();
	    while (kiddies.hasMoreElements()) {
	        TreeNode child = (TreeNode) kiddies.nextElement();
	        parseTreeNode(child, parentElement);
	    }
	
	}

	
}