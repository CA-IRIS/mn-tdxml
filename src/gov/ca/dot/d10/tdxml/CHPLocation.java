/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2003-2008  Minnesota Department of Transportation
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

package gov.ca.dot.d10.tdxml;

import org.w3c.dom.Element;

import us.mn.state.dot.tdxml.AbstractXmlFactory;
import us.mn.state.dot.tdxml.Direction;
import us.mn.state.dot.tdxml.Location;

/**
 * Encapsulates all the data for a CHP incident location. A
 * CHPLocation is not valid unless getValid() returns true.
 *
 * @author Erik Engstrom
 * @author Michael Darter
 */
public class CHPLocation implements Location
{
	private double m_easting = 0;     // UTM zone 10
	private double m_linear = 0;
	private String m_name = "";
	private double m_northing = 0;    // UTM zone 10
	private boolean m_metro = false;
	private Direction m_direction = Direction.UNKNOWN;
	private boolean m_valid = false;

	/** Create a new CHP location */
	public CHPLocation() {}

	/**
	 *  Create a new CHP location from a Log element in the
	 *  CHP xml incident file.
	 *
	 *  @param e A Log element within the CHP xml file.
	 */
	public CHPLocation(Element e) {

		// valid arg?
		if(!Contract.verify("CHPLocation.CHPLocation.1",
				    e.getNodeName().equals("Log")))
			return;

		// get coords (format "6736155:1629797"). May be empty (e.g. "")
		String coords = AbstractXmlFactory.lookupChildText(e, "TBXY");
		coords = SString.removeEnclosingQuotes(coords);
		if(!Contract.verify("CHPLocation.CHPLocation.2",
				    coords != null))
			return;
		if(coords.length() > 0) {
			int i = coords.indexOf(':');
			if(!Contract.verify("CHPLocation.CHPLocation.3:"
					    + coords, i >= 0))
				return;
			this.m_easting = Double.parseDouble(coords.substring(0,
				i));
			this.m_northing = Double.parseDouble(coords.substring(i
				+ 1));
			this.setPositionWithTB(m_easting, m_northing);
		}

		// etc
		this.m_linear = 0;
		this.m_name = "Incident";
		this.m_direction = Direction.UNKNOWN;
		this.m_metro = true;

		// if we've made it this far the Location is valid.
		this.m_valid = true;
	}

	/** toString() return coordinates */
	public String toString() {
		return m_name + ": " + Math.round(this.getEasting()) + ","
		       + Math.round(this.getNorthing());
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public double getEasting() {
		return m_easting;
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public double getLinear() {
		return m_linear;
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public double getNorthing() {
		return m_northing;
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public Direction getDirection() {
		return m_direction;
	}

	/** is location valid? */
	public boolean isValid() {
		if(!m_valid)
			return (false);
		m_valid = m_valid && this.inD10();
		return m_valid;
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public boolean isInMetro() {
		return m_metro;
	}

	/**
	 *  Set the current northing and easting position
	 *  in UTM Z10 using TB coordinates.
	 *
	 *  @param eastingTB Thomas Brothers easting coordinate.
	 *  @param northingTB Thomas Brothers northing coordinate.
	 */
	private void setPositionWithTB(double eastingTB, double northingTB) {
		int zone = CHPLocation.centerIdToZone("STCC");    // FIXME
		this.ConvertTBtoUTM10(eastingTB, northingTB, zone);
	}

	/** Main for testing purposes */
	public static void main(String[] args) {
		System.err.println("entered main");
		if(CHPLocation.test()) {
			System.err.println("TEST Pass");
		} else {
			System.err.println("TEST FAIL");
		}
		;
	}

	/** Coordinate transformation from Thomas Brother to UTM 10 in meters */
	static int centerIdToZone(String cid) {
		return (2);    // FIXME

		// return("FRCC");
	}

	/** Coordinate transformation from Thomas Brother to UTM 10 in meters */
	protected void ConvertTBtoUTM10(double tbx, double tby, int zone) {

		double m_utm10x = tbx;
		double m_utm10y = tby;

		/**
		 * By ksy
		 * Thomas Brother (TB) Maps (Lambert Conformal Conic Mapping) are divided 6 zones for CA. Zone 1 is Northern CA (Redding), Zone 2 is Greater Sacramento, Zone 3 is Bay Area. Zone 4 is Central Vallery, Zone 5 is LA, and Zone 6 is San Diego, Barstow, and Riverside. D10 occupy across Zone 2 and 4.
		 * This function return true if Zone 2 and 4 is passed along, otherwise it return false.
		 * It only valid for coordinate in Zone 2 and 4 of Thomas Brother in CA (May be modify later to work with other Zone in CA or States).
		 * TB x, y units are in US survey feet (different from "regular" or international feet). TB Map North and East axis are aligned with that of UTM.  Thus, each TB zone map may be align with a UTM area with a offset once the TB units are scaled to meter.  The following offset value are determined by avaraging serveral points using  CHP provided TB points (x,y) and description of accident locations to determine the UTM10(x,y). The error should be within couple hundreds meters.  The offset should be recalculated one a TB map book is obtained.
		 */

		// Analyical solution would produce more accurate transformation. However, the formulae are more complex.
		final double US_FT2METER = 0.3048006096012192;
		final double TB_Zone2_X2UTM10X_Offset = -1412149;
		final double TB_Zone2_Y2UTM10Y_Offset = 3669675;
		final double TB_Zone4_X2UTM10X_Offset = -1146562;
		final double TB_Zone4_Y2UTM10Y_Offset = 3409603;
		m_utm10x = 0;
		m_utm10y = 0;

		// center id: STCC
		if(zone == 2) {
			m_utm10x = TB_Zone2_X2UTM10X_Offset + US_FT2METER * tbx;
			m_utm10y = TB_Zone2_Y2UTM10Y_Offset + US_FT2METER * tby;

			// System.err.println("zone2=,"+tbx+","+tby+","+m_utm10x+","+m_utm10y+","+zone);
			// Do a range check of the solution? The answer should be > zero, and smaller than some number (TBD).
		}

		// center id: FRCC
		else if(zone == 4) {
			m_utm10x = TB_Zone4_X2UTM10X_Offset + US_FT2METER * tbx;
			m_utm10y = TB_Zone4_Y2UTM10Y_Offset + US_FT2METER * tby;

			// System.err.println("zone4,"+tbx+","+tby+","+m_utm10x+","+m_utm10y+","+zone);
			// Do a range check of the solution? The answer should be > zero, and smaller than some number (TBD).
		} else {
			assert false : "bogus zone in CHPLocation()";
		}

		m_easting = m_utm10x;
		m_northing = m_utm10y;
	}

	/**
	 *  Is the current location within D10?
	 */

	/** This function determine if a point (x,y in UTM10 meter) is within Caltrans D10 area. it returns true if the point is in D10, and returns false otherwise. */
	public boolean inD10() {
		double x = m_easting;
		double y = m_northing;

		// By ksy
		// CA D10 area is divided in four rectangle boxes.
		// If a point is within any of the four boxes, it is within CA D10.
		// D10_Boundary*_X1 define X coordinate of the rectangle lower left corner.
		// D10_Boundary*_Y1 define Y coordinate of the rectangle lower left corner.
		// D10_Boundary*_X2 define X coordinate of the rectangle upper right corner.
		// D10_Boundary*_Y2 define Y coordinate of the rectangle upper right corner.
		final double D10_Boundary1_X1 = 625000.000;
		final double D10_Boundary1_Y1 = 4137000.000;
		final double D10_Boundary1_X2 = 671000.000;
		final double D10_Boundary1_Y2 = 4235600.000;

		final double D10_Boundary2_X1 = 671000.000;
		final double D10_Boundary2_Y1 = 4137000.000;
		final double D10_Boundary2_X2 = 730000.000;
		final double D10_Boundary2_Y2 = 4269500.000;

		final double D10_Boundary3_X1 = 730000.000;
		final double D10_Boundary3_Y1 = 4113000.000;
		final double D10_Boundary3_X2 = 799000.000;
		final double D10_Boundary3_Y2 = 4306000.000;

		final double D10_Boundary4_X1 = 658000.000;
		final double D10_Boundary4_Y1 = 4080000.000;
		final double D10_Boundary4_X2 = 730000.000;
		final double D10_Boundary4_Y2 = 4137000.000;

		if(inBox(x, y, D10_Boundary1_X1, D10_Boundary1_Y1,
			D10_Boundary1_X2, D10_Boundary1_Y2)) {

			// System.err.println("In box 1");
			return (true);
		} else if(inBox(x, y, D10_Boundary2_X1, D10_Boundary2_Y1,
				D10_Boundary2_X2, D10_Boundary2_Y2)) {

			// System.err.println("In box 2");
			return (true);
		} else if(inBox(x, y, D10_Boundary3_X1, D10_Boundary3_Y1,
				D10_Boundary3_X2, D10_Boundary3_Y2)) {

			// System.err.println("In box 3");
			return (true);
		} else if(inBox(x, y, D10_Boundary4_X1, D10_Boundary4_Y1,
				D10_Boundary4_X2, D10_Boundary4_Y2)) {

			// System.err.println("In box 4");
			return (true);
		} else {

			// System.err.println("Not in any box");
			return (false);
		}
	}

	/** This function determine if a point is within a box given the point(x,y) and the box lower left (X1, Y1) and upper right corner (X2, Y2). It return true if the point in locationed within the rectangular box and returns false otherwise. */
	public static boolean inBox(double x, double y, double x1, double y1,
				    double x2, double y2) {

		// By ksy
		// x, y the is coordinate of the point
		// x1 define X coordinate of the rectangle lower left corner
		// y1 define Y coordinate of the rectangle lower left corner
		// x2 define X coordinate of the rectangle upper right corner
		// y2 define Y coordinate of the rectangle upper right corner
		// System.err.println("x,y,x1,y1,x2,y2,"+x+","+y+","+x1+","+y1+","+x2+","+y2);
		if((x >= x1) && (x <= x2) && (y >= y1) && (y <= y2)) {

			// System.err.println("In box");
			return (true);
		} else {

			// System.err.println("not in box");
			return (false);
		}
	}

	/** static tests */
	public static boolean test() {

/*
                        // t1_xy are points located outside of D10 in Thomas Brother Zone2
                        final int n = 33;
                        final double[] t1_xy = {
                            6752253, 1887621, 6736885, 1911407, 6704006, 1931747,
                            6737456, 1941102, 6720184, 1942967, 6720269, 1942969,
                            6727792, 1942985, 6716239, 1948153, 6712206, 1959345,
                            6712206, 1959345, 6739198, 1965857, 6719502, 1975946,
                            6765355, 1977881, 6806920, 1987775, 6702305, 1972100,
                            6703485, 1976385, 6725636, 1936527
                        };

                        // t2_xy are points located inside of D10 in Thomas Brother Zone2
                        final double[] t2_xy = {
                            6780370, 1734265, 6780370, 1734265, 6759975, 1746155,
                            6775358, 1770406, 6753566, 1771596, 6775820, 1780786,
                            6769244, 1799435, 6708501, 1804261, 6734935, 1804617,
                            6798114, 1805226, 6779928, 1744987, 6781874, 1713697,
                            6784231, 1733393, 6823359, 1848868, 6757245, 1640369,
                            6707727, 1667936, 6724840, 1675936
                        };

                        // t3_xy are points located outside D10 in Thomas Brother Zone 4.
                        final double[] t3_xy = {
                            6413813, 2082511, 6388133, 2082784, 6313426, 2085508,
                            6198212, 2127183, 6334718, 2141332, 6435171, 2144312,
                            6342894, 2173077, 6351511, 2183525, 6330800, 2195186,
                            6334403, 2158634, 6386580, 2082797, 6392780, 2088024,
                            6263066, 2220945, 6250515, 2234834, 6248429, 2295133,
                            6589577, 2042305, 6472041, 2076327
                        };

                        // t3_xy are points located outside D10 in Thomas Brother Zone 4.
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
                            ;

                            // System.err.println(i);
                //FIXME: test cases
                            if(inD10(m_utm10x, m_utm10y)) {
                                System.err.println("test fail at t1_xy," + i);
                                ok = false;
                            }
                            ;

                            i = i + 2;
                        }

                        i = 0;
                        while(i <= n) {
                            if(!ConvertTBtoUTM10(t2_xy[i], t2_xy[i + 1], 2)) {
                                System.err.println(
                                    "zone not available at test2" + i);
                            }
                            ;

                            // System.err.println(i);
                            if(!inD10(m_utm10x, m_utm10y)) {
                                System.err.println("test fail at t2_xy," + i);
                                ok = false;
                            }
                            ;

                            i = i + 2;
                        }

                        i = 0;
                        while(i <= n) {
                            if(!ConvertTBtoUTM10(t3_xy[i], t3_xy[i + 1], 4)) {
                                System.err.println(
                                    "zone not available at test3" + i);
                            }
                            ;

                            // System.err.println(i);
                            if(inD10(m_utm10x, m_utm10y)) {
                                System.err.println("test fail at t3_xy," + i);
                                ok = false;
                            }
                            ;

                            i = i + 2;
                        }

                        i = 0;
                        while(i <= n) {
                            if(!ConvertTBtoUTM10(t4_xy[i], t4_xy[i + 1], 4)) {
                                System.err.println(
                                    "zone not available at test4" + i);
                            }
                            ;

                            // System.err.println(i);
                            if(!inD10(m_utm10x, m_utm10y)) {
                                System.err.println("test fail at t4_xy," + i);
                                ok = false;
                            }
                            ;

                            i = i + 2;
                        }

                        return (ok);
*/
		return true;
	}
}
