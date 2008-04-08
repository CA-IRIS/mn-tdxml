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
public class CHPLocation implements Location {

    private double m_easting=0;             // UTM zone 10
    private double m_northing=0;            // UTM zone 10
	private double m_linear=0;
	private String m_name="";
	private Direction m_direction=Direction.UNKNOWN;
	private boolean m_metro=false;
    private boolean m_valid=false;

	/** Create a new CHP location */
	public CHPLocation() {
	}

	/** 
     *  Create a new CHP location from a Log element in the 
     *  CHP xml incident file.
     *
     *  @param e A Log element within the CHP xml file.
     */
	public CHPLocation(Element e) {

        // valid arg?
        if( !Contract.verify("CHPLocation.CHPLocation.1",e.getNodeName().equals("Log")) )
            return;

        // get coords (format "6736155:1629797"). May be empty (e.g. "")
		String coords=AbstractXmlFactory.lookupChildText(e,"TBXY");
        coords=SString.removeEnclosingQuotes(coords);
        if( !Contract.verify("CHPLocation.CHPLocation.2",coords!=null) )
            return;
        if( coords.length()>0 ) {
            int i=coords.indexOf(':');
            if( !Contract.verify("CHPLocation.CHPLocation.3:"+coords,i>=0) )
                return;
            this.m_easting=Double.parseDouble(coords.substring(0,i));
            this.m_northing=Double.parseDouble(coords.substring(i+1));
            this.setPositionWithTB(m_easting,m_northing);
        }

        // etc
		this.m_linear = 0;
		this.m_name = "Incident";
		this.m_direction = Direction.UNKNOWN;
		this.m_metro = true;

        // if we've made it this far the Location is valid.
        this.m_valid=true;
	}

    /** toString() return coordinates */
	public String toString() {
		return m_name+": "+Math.round(this.getEasting())+","+Math.round(this.getNorthing());
	}

	public double getEasting() {
		return m_easting;
	}

	public double getLinear() {
		return m_linear;
	}

	public double getNorthing() {
		return m_northing;
	}

	public Direction getDirection() {
		return m_direction;
	}

    /** is location valid? */
	public boolean isValid() {
        if(!m_valid)
            return(false);
        m_valid=m_valid && this.inD10();
		return m_valid;
	}

	public boolean isInMetro(){
		return m_metro;
	}

	/** 
     *  Set the current northing and easting position 
     *  in UTM Z10 using TB coordinates.
     *
     *  @param eastingTB Thomas Brothers easting coordinate.
     *  @param northingTB Thomas Brothers northing coordinate.
     */
	private void setPositionWithTB(double eastingTB,double northingTB) {
        // this is an estimate based on 2 points
        this.m_easting=(eastingTB-5689922)/1.661322645;
        this.m_northing=(northingTB+10469962)/2.909457478;
	}

	/** 
     *  Is the current location within D10?
     */
	public boolean inD10() {
        boolean in=(this.m_easting>579860 && this.m_easting<892322) &&
            (this.m_northing>3937659 && this.m_northing<4368894);
        return(in);
	}

}
