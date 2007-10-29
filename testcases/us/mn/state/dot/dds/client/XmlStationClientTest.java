/*
 * DDS Client -- Data Distribution Server Client
 * Copyright (C) 2000  Minnesota Department of Transportation
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
 
package us.mn.state.dot.dds.client;

import java.util.List;

import junit.framework.TestCase;

import org.apache.log.Hierarchy;

/**
 * 
 * 
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision$ $Date$
 */
public class XmlStationClientTest extends TestCase {

	private XmlStationClient client = null;
	
	/**
	 * Constructor for XmlStationClientTest.
	 * @param arg0
	 */
	public XmlStationClientTest(String arg0) {
		super(arg0);
	}

	public void testReadData() throws Exception {
		List list = client.readData();
		assertEquals("Size is not equal", 1121, list.size());
		Station station = (Station)list.get(20);
		assertEquals( "volume", 12.666667, station.getVolume(), 0);
		assertEquals( "occupancy", 12.055556, station.getOccupancy(), 0);
		assertEquals( "flow", 1520, station.getFlow() );
		assertEquals( "speed", 59, station.getSpeed() );
	}

	public void testStart() {
	}

	public void testStop() {
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		client = new XmlStationClient();		
		client.setLogger( Hierarchy.getDefaultHierarchy().getLoggerFor(
			 "XmlStationClient" ) );
		client.setLocation( "testdata/station.xml" );
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		client = null;
	}

}
