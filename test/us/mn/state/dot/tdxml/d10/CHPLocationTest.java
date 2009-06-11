/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import junit.framework.TestCase;
import us.mn.state.dot.tdxml.d10.CHPLocation;

/** 
 * CHPLocation test cases
 * @author Michael Darter, AHMCT
 * @created 06/10/09
 */
public class CHPLocationTest extends TestCase {

	/** constructor */
	public CHPLocationTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// inD10
		assertTrue(CHPLocation.inD10(651000, 4203000));
		assertFalse(CHPLocation.inD10(628000, 4310714));

		// ConvertTBtoUTM10
		//Point2D.Double p = CHPLocation.ConvertTBtoUTM10(double tbx, double tby, int zone);

		assertTrue(true);
	}

	/* FIXME: create junit tests for these...which were moved from CHPLocation.

		// t1_xy are points located outside of D10 in 
		// Thomas Brother Zone2
		final int n = 33;
		final double[] t1_xy = {
		    6752253, 1887621, 6736885, 1911407, 6704006, 1931747,
		    6737456, 1941102, 6720184, 1942967, 6720269, 1942969,
		    6727792, 1942985, 6716239, 1948153, 6712206, 1959345,
		    6712206, 1959345, 6739198, 1965857, 6719502, 1975946,
		    6765355, 1977881, 6806920, 1987775, 6702305, 1972100,
		    6703485, 1976385, 6725636, 1936527
		};

		// t2_xy are points located inside of D10 in TB Zone2
		final double[] t2_xy = {
		    6780370, 1734265, 6780370, 1734265, 6759975, 1746155,
		    6775358, 1770406, 6753566, 1771596, 6775820, 1780786,
		    6769244, 1799435, 6708501, 1804261, 6734935, 1804617,
		    6798114, 1805226, 6779928, 1744987, 6781874, 1713697,
		    6784231, 1733393, 6823359, 1848868, 6757245, 1640369,
		    6707727, 1667936, 6724840, 1675936
		};

		// t3_xy are points located outside D10 in TB Zone 4.
		final double[] t3_xy = {
		    6413813, 2082511, 6388133, 2082784, 6313426, 2085508,
		    6198212, 2127183, 6334718, 2141332, 6435171, 2144312,
		    6342894, 2173077, 6351511, 2183525, 6330800, 2195186,
		    6334403, 2158634, 6386580, 2082797, 6392780, 2088024,
		    6263066, 2220945, 6250515, 2234834, 6248429, 2295133,
		    6589577, 2042305, 6472041, 2076327
		};

		// t3_xy are points located outside D10 in TB Zone 4.
		final double[] t4_xy = {
		    6372490, 2368383, 6086889, 2369073, 6117561, 2371663,
		    6099666, 2395359, 6063719, 2391488, 6103453, 2372479,
		    5974704, 2387964, 5924047, 2429583, 5903868, 2451150,
		    5993060, 2469958, 5983445, 2475705, 5986093, 2479037,
		    5935820, 2486686, 5976721, 2489140, 6135285, 2501592,
		    5959853, 2511235, 5955603, 2513312
		};

		boolean ok = true;
		int i = 0;
		while(i <= n) {
		    if(!ConvertTBtoUTM10(t1_xy[i], t1_xy[i + 1], 2)) {
		        System.err.println(
		            "zone not available at test1" + i);
		    }
		    if(inD10(m_utm10x, m_utm10y)) {
		        System.err.println("test fail at t1_xy," + i);
		        ok = false;
		    }
		    i = i + 2;
		}

		i = 0;
		while(i <= n) {
		    if(!ConvertTBtoUTM10(t2_xy[i], t2_xy[i + 1], 2)) {
		        System.err.println(
		            "zone not available at test2" + i);
		    }
		    // System.err.println(i);
		    if(!inD10(m_utm10x, m_utm10y)) {
		        System.err.println("test fail at t2_xy," + i);
		        ok = false;
		    }
		    i = i + 2;
		}

		i = 0;
		while(i <= n) {
		    if(!ConvertTBtoUTM10(t3_xy[i], t3_xy[i + 1], 4)) {
		        System.err.println(
		            "zone not available at test3" + i);
		    }
		    // System.err.println(i);
		    if(inD10(m_utm10x, m_utm10y)) {
		        System.err.println("test fail at t3_xy," + i);
		        ok = false;
		    }
		    i = i + 2;
		}

		i = 0;
		while(i <= n) {
		    if(!ConvertTBtoUTM10(t4_xy[i], t4_xy[i + 1], 4)) {
		        System.err.println(
		            "zone not available at test4" + i);
		    }
		    // System.err.println(i);
		    if(!inD10(m_utm10x, m_utm10y)) {
		        System.err.println("test fail at t4_xy," + i);
		        ok = false;
		    }
		    i = i + 2;
		}
		*/
}
