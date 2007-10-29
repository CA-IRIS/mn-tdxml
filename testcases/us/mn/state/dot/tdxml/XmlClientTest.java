/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2007  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package us.mn.state.dot.tdxml;

import junit.framework.TestCase;

import org.apache.log.Hierarchy;

/**
 * 
 * 
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision$ $Date$
 */
public class XmlClientTest extends TestCase {
	
	private XmlClient client = null;
	
	private static final String location = 
		"http://data.dot.state.mn.us:8080/dds/station.xml";

	/**
	 * Constructor for XmlClientTest.
	 * @param arg0
	 */
	public XmlClientTest(String arg0){
		super(arg0);
		System.getProperties().put( "proxySet", "true" );
		System.getProperties().put( "proxyHost", "webproxy.dot.state.mn.us" );
		System.getProperties().put( "proxyPort", "3128" );

	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(XmlClientTest.class);
	}

	public void testReadData() throws Exception {
		client.readData();
	}

	public void testStart_Stop() throws InterruptedException {
		int threads = Thread.activeCount();
		assertTrue( "Client already running.", !client.isRunning() );
		client.start();
		assertTrue( "Thread didnt start.", Thread.activeCount() == threads + 1);
		assertTrue( "Client didnt start.", client.isRunning() );
		Thread.sleep( 1000 );
		client.stop();
		assertTrue( "Client failed to stop.", !client.isRunning() );
		assertTrue( "Thread didnt stop.", Thread.activeCount() == threads );
	}

	public void testSetLogger() {
		client.setLogger( Hierarchy.getDefaultHierarchy().getLoggerFor(
			"XmlClient" ) );
	}
	
	public void testSetLocation() throws Exception{
		String testLoc = "http://www.dot.state.mn.us";
		client.setLocation( testLoc );
		assertEquals( "Location not changed.", testLoc, client.getLocation());
	}

	public void testSetDaemon() {
		boolean daemon = client.isDaemon();
		assertTrue( "Daemon not set to true.", daemon );
		client.setDaemon( !daemon );
		assertTrue( "setDaemon failed.", daemon != client.isDaemon() );
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		client = new XmlClient();		
		client.setLogger( Hierarchy.getDefaultHierarchy().getLoggerFor( "XmlClient" ) );
		client.setLocation( location );
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		client = null;
	}

}
