/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2003-2009  Minnesota Department of Transportation
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

package us.mn.state.dot.tdxml.d10;

import java.awt.geom.Point2D;
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
 * @author Kin S Yen
 */
public class CHPLocation implements Location
{
	private double m_easting = 0;	// UTM zone 10
	private double m_linear = 0;
	private String m_name = "";
	private double m_northing = 0;	// UTM zone 10
	private boolean m_metro = false;
	private Direction m_direction = Direction.UNKNOWN;
	private boolean m_valid = false;
	private int m_chpzone = 0;	// chp dispatch zone
	private String m_chpCenterId=""; // chp center id

	// CHP dispatch zones
	private static final int ZONE_STCC=2;
	private static final int ZONE_FRCC=4;

	/** Create a new CHP location */
	public CHPLocation() {}

	/**
	 *  Create a new CHP location from a Log element in the
	 *  CHP xml incident file.
	 *
	 *  @param e A Log element within the CHP xml file.
	 */
	public CHPLocation(Element e,String centerId) {

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
			this.m_easting = Double.parseDouble(
				coords.substring(0, i));
			this.m_northing = Double.parseDouble(
				coords.substring(i + 1));
			this.setPositionWithTB(m_easting, m_northing);
		}

		// etc
		this.m_chpCenterId = centerId;
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

	/** get easting coordinate */
	public double getEasting() {
		return m_easting;
	}

	/** get linear */
	public double getLinear() {
		return m_linear;
	}

	/** get northing coordinate */
	public double getNorthing() {
		return m_northing;
	}

	/** get direction */
	public Direction getDirection() {
		return m_direction;
	}

	/** is location valid? */
	public boolean isValid() {
		if(!m_valid)
			return false;
		m_valid = m_valid && inD10();
		return m_valid;
	}

	/** get metro */
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
		int zone = CHPLocation.centerIdToZone(m_chpCenterId);
		Point2D.Double pnt = ConvertTBtoUTM10(eastingTB, northingTB, zone);
		m_easting = pnt.x;
		m_northing = pnt.y;
	}

	/** given center id, return known zone */
	static int centerIdToZone(String cid) {
		// have valid zone?
		if (cid==null || cid.length()<=0)
			return ZONE_STCC; //FIXME: shouldn't have to do this

		int zone=0;
		if (cid.equals("STCC"))
			return ZONE_STCC;
		else if (cid.equals("FRCC"))
			return ZONE_FRCC;
		String msg="Warning: bogus zone (" + cid + 
			") in centerIdToZone().";
		assert false : msg;
		System.err.println(msg);
		return ZONE_STCC;
	}

	/** 
	 * Coordinate transformation from Thomas Brother to UTM 10 in meters.
	 * Thomas Brother (TB) Maps (Lambert Conformal Conic Mapping) are 
	 * divided 6 zones for CA. Zone 1 is Northern CA (Redding), Zone 2 
	 * is Greater Sacramento, Zone 3 is Bay Area. Zone 4 is Central 
	 * Vallery, Zone 5 is LA, and Zone 6 is San Diego, Barstow, and 
	 * Riverside. D10 occupy across Zone 2 and 4. This function returns 
	 * true if Zone 2 and 4 is passed along, otherwise it return false.
	 * It only valid for coordinate in Zone 2 and 4 of Thomas Brother 
	 * in CA (May be modify later to work with other Zone in CA or 
	 * States).
	 * TB x, y units are in US survey feet (different from "regular" 
	 * or international feet). TB Map North and East axis are aligned 
	 * with that of UTM.  Thus, each TB zone map may be align with a 
	 * UTM area with a offset once the TB units are scaled to meter.
	 * The following offset value are determined by avaraging serveral 
	 * points using  CHP provided TB points (x,y) and description of 
	 * accident locations to determine the UTM10(x,y). The error 
	 * should be within couple hundreds meters.  The offset should be 
	 * recalculated one a TB map book is obtained.
	 * @param tbx Thomas brothers x coordinate.
	 * @param tby Thomas brothers y coordinate.
	 * @param zone Thomas brothers zone.
	 * @author Kin S Yen
	 */
	protected static Point2D.Double ConvertTBtoUTM10(double tbx, double tby, int zone) {

		// validate args
		if (zone!=ZONE_FRCC && zone!=ZONE_STCC) {
			String msg="Warning: bogus zone (" + zone + 
				") in ConvertTBtoUTM10().";
			assert false : msg;
			System.err.println(msg);
		}

		double m_utm10x = tbx;	//FIXME remove these
		double m_utm10y = tby;

		// Analyical solution would produce more accurate 
		// transformation. However, the formulae are more 
		// complex.
		final double US_FT2METER = 0.3048006096012192;
		final double TB_Zone2_X2UTM10X_Offset = -1412149;
		final double TB_Zone2_Y2UTM10Y_Offset = 3669675;
		final double TB_Zone4_X2UTM10X_Offset = -1146562;
		final double TB_Zone4_Y2UTM10Y_Offset = 3409603;
		m_utm10x = 0;
		m_utm10y = 0;

		// center id: STCC
		if(zone == ZONE_STCC) {
			m_utm10x = TB_Zone2_X2UTM10X_Offset + 
				US_FT2METER * tbx;
			m_utm10y = TB_Zone2_Y2UTM10Y_Offset + 
				US_FT2METER * tby;
			// System.err.println("zone2=,"+tbx+"," + tby + 
			//	","+m_utm10x+","+m_utm10y+","+zone);
			// Do a range check of the solution? The answer should 
			// be > zero, and smaller than some number (TBD).
		}

		// center id: FRCC
		else if(zone == ZONE_FRCC) {
			m_utm10x = TB_Zone4_X2UTM10X_Offset + 
				US_FT2METER * tbx;
			m_utm10y = TB_Zone4_Y2UTM10Y_Offset + 
				US_FT2METER * tby;
			// System.err.println("zone4,"+tbx+"," + tby + 
			// 	","+m_utm10x+","+m_utm10y+","+zone);
			// Do a range check of the solution? The answer should
			// be > zero, and smaller than some number (TBD).
		} else {
			assert false : "bogus zone in CHPLocation()";
		}

		Point2D.Double ret = new Point2D.Double(m_utm10x, m_utm10y);
		return ret;
	}

	/** Is the current location within D10? */
	public boolean inD10() {
		return inD10(m_easting, m_northing);
	}

	/**
	 *  Is the specified location within D10?
	 *
	 *  This method determine if a point (x,y in UTM10 meter) is 
	 *  within Caltrans D10 area. it returns true if the point is 
	 *  in D10, and returns false otherwise.
	 *  @author Kin S Yen 
	 */
	public static boolean inD10(double x, double y) {
		// CA D10 area is divided in four rectangle boxes.
		// If a point is within any of the four boxes, it is 
		// within CA D10.
		// D10_Boundary*_X1 define X coord of rect lower left corner
		// D10_Boundary*_Y1 define Y coord of rect lower left corner.
		// D10_Boundary*_X2 define X coord of rect upper right corner.
		// D10_Boundary*_Y2 define Y coord of rect upper right corner.
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

	/** 
	 *  This function determine if a point is within a box given the 
	 *  point(x,y) and the box lower left (X1, Y1) and upper right 
	 *  corner (X2, Y2). It return true if the point in locationed 
	 *  within the rectangular box and returns false otherwise. 
	 *  @author Kin S Yen 
	 */
	public static boolean inBox(double x, double y, double x1, double y1,
		double x2, double y2) 
	{
		// x, y the is coordinate of the point
		// x1 define X coordinate of the rectangle lower left corner
		// y1 define Y coordinate of the rectangle lower left corner
		// x2 define X coordinate of the rectangle upper right corner
		// y2 define Y coordinate of the rectangle upper right corner
		// System.err.println("x,y,x1,y1,x2,y2,"+x+","+y+
		//	","+x1+","+y1+","+x2+","+y2);
		if((x >= x1) && (x <= x2) && (y >= y1) && (y <= y2)) {
			// System.err.println("In box");
			return (true);
		} else {
			// System.err.println("not in box");
			return (false);
		}
	}
}
