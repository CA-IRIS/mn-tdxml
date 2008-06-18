/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tdxml;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * XmlClient reads an xml document at a specified interval and parses it to java
 * classes that can be used by listeners who are alerted when new data is
 * available.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class XmlClient implements Runnable {

	/** The location of the xml document */
	protected final String location;

	/** The thread the client runs in */
	private Thread thread = null;

	/** Logger to use */
	protected final Logger logger;

	/** Should this run as a daemon? */
	private boolean daemon = true;

	/** Time to wait till re-reading data */
	private int sleepTime = 30000;

	/** DOM document builder */
	protected final DocumentBuilder builder;

	/** List of listeners */
	protected List<TdxmlListener> listeners =
		new LinkedList<TdxmlListener>();

	/** Create a new XmlClient */
	protected XmlClient(String loc, Logger l) throws TdxmlException {
		super();
		location = loc;
		if(location == null)
			throw new NullPointerException();
		logger = l;
		try {
			DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		}
		catch(ParserConfigurationException e) {
			throw new TdxmlException(e);
		}
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(isRunning()) {
			readData();
			try {
				Thread.sleep(sleepTime);
			} catch(InterruptedException ie) {
				logger.info("Interrupted: no longer reading " + location);
				logger.fine(ie.getMessage());
				break;
			}
		}
	}

	/** Read and parse an XML file */
	abstract protected void readXmlFile() throws Exception;

	/** Read the data from the xml file */
	protected void readData() {
		logger.info("Reading data from " + location);
		try {
			readXmlFile();
		}
		catch(IOException ioe){
			logger.warning("IOException reading data from " + location);
			logger.warning(ioe.getMessage());
		}
		catch(Exception e) {
			logger.warning("Error reading xml from " + location +
				"("+e+"), " +
				"will retry in " + sleepTime / 1000 +
				" seconds.");
		}
		catch(Throwable t){
			logger.warning("Fatal exception.  Attempting to recover.");
			logger.severe(t.getStackTrace().toString());
		}
	}

	/**
	 * Starts a new thread running that reads the xml file and fires update
	 * events to registered listeners when new data arrives.
	 */
	public void start() {
		logger.info("start() " + location);
		synchronized(this) {
			if(!isRunning()) {
				thread = new Thread(this);
				thread.setDaemon(daemon);
				thread.start();
			}
		}
	}

	/**
	 * Stops the running thread
	 */
	public void stop() {
		logger.info("stop() " + location);
		synchronized(this) {
			Thread t = thread;
			thread = null;
			if(t != null) {
				try {
					t.interrupt();
					t.join();
				}
				catch(InterruptedException e) {
					logger.info("Join interrupted: " +
						e.getMessage());
				}
			}
		}
	}

	/**
	 * Will this client run in a daemon thread?
	 *
	 * @return true if this client runs as a daemon.
	 */
	public boolean isDaemon() {
		return daemon;
	}

	/**
	 * Set whether this client will run as a daemon thread.
	 *
	 * @param daemon if true the thread this client is run in will be set as
	 * a daemon thread.
	 */
	public void setDaemon( boolean daemon ) {
		this.daemon = daemon;
	}

	/**
	 * Returns the location.
	 * @return String
	 */
	public String getLocation() {
		return location;
	}

	public boolean isRunning() {
		return thread != null;
	}

	/**
	 * Returns the sleepTime.
	 * @return int
	 */
	public int getUpdateInterval() {
		return sleepTime;
	}

	/**
	 * Sets the sleepTime.
	 * @param sleepTime The sleepTime to set
	 */
	public void setUpdateInterval(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	/** Add a TDXML listener */
	public void addTdxmlListener(TdxmlListener l) {
		LinkedList<TdxmlListener> lsnr =
			new LinkedList<TdxmlListener>(listeners);
		lsnr.add(l);
		listeners = lsnr;
	}

	/** Remove a TDXML listener */
	public void removeTdxmlListener(TdxmlListener l) {
		LinkedList<TdxmlListener> lsnr =
			new LinkedList<TdxmlListener>(listeners);
		lsnr.remove(l);
		listeners = lsnr;
	}

	/** Remove all of the registered data listeners */
	public void removeAllTdxmlListeners() {
		listeners = new LinkedList<TdxmlListener>();
	}

	/** Notifier for TDXML listeners */
	abstract protected class Notifier {
		abstract void notify(TdxmlListener l);
	}

	/** Notify all listeners of an update */
	protected void doNotify(Notifier n) {
		for(TdxmlListener l: listeners)
			n.notify(l);
	}

	/** Notify listeners of the start of new data */
	protected void notifyStart() {
		doNotify(new Notifier() {
			void notify(TdxmlListener l) {
				l.update(false);
			}
		});
	}

	/** Notify listeners that new data is finished */
	protected void notifyFinish() {
		doNotify(new Notifier() {
			void notify(TdxmlListener l) {
				l.update(true);
			}
		});
	}
}
