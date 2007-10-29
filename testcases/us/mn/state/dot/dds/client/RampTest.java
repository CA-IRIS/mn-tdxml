/*
 * Created on Nov 13, 2003
 *
 */
package us.mn.state.dot.dds.client;

import junit.framework.TestCase;

/**
 * @author engs1eri
 * @version 
 */
public class RampTest extends TestCase {

	/**
	 * Constructor for RampTest.
	 * @param arg0
	 */
	public RampTest(String arg0) {
		super(arg0);
	}
	
	public void testClosures(){
		String[] phrases = {
			"ramp",
			"asdframpasdf",
			"asdfasdf",
			"reduced-to-two-lanes",
			"right-lane-closed"
		};
		assertTrue( phrases[0].matches(".*(ramp).*$"));
		assertTrue( phrases[1].matches(".*(ramp).*$"));
		assertFalse( phrases[2].matches(".*(ramp).*$"));
		assertFalse( phrases[3].matches(".*(ramp).*$"));
		assertFalse( phrases[4].matches(".*(ramp).*$"));
	}

}
