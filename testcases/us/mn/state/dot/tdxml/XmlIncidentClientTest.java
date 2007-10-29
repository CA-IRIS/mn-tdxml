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
 */
package us.mn.state.dot.dds.client;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log.Hierarchy;

/**
 * 
 * 
 * @author Erik Engstrom
 */
public class XmlIncidentClientTest extends TestCase {

	private XmlIncidentClient client = null;
	
	/**
	 * Constructor for XmlIncidentClientTest.
	 * @param arg0
	 */
	public XmlIncidentClientTest(String arg0) {
		super(arg0);
		System.getProperties().put( "proxySet", "true" );
		System.getProperties().put( "proxyHost", "webproxy.dot.state.mn.us" );
		System.getProperties().put( "proxyPort", "3128" );
	}
	
	public void testReadData() throws Exception {
		List list = client.readData();
		Iterator it = list.iterator();
		while ( it.hasNext() ) {
			Incident incident = (Incident)it.next();
			System.out.println( incident.toString() );
			System.out.println( "x=" + incident.getX() + " y=" + incident.getY() );
			
		}
		assertEquals("Size is not equal", 15, list.size());
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Properties props = new Properties();
		client = new XmlIncidentClient(props,
			Hierarchy.getDefaultHierarchy().getLoggerFor( 
			"XmlIncidentClient"));		
		client.setLogger( Hierarchy.getDefaultHierarchy().getLoggerFor( 
			"XmlIncidentClient" ) );
		client.setLocation("http://www.carsprogram.org/MN/test.xml" );
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		client = null;
	}
}
