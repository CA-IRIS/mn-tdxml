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
import us.mn.state.dot.tdxml.EventTime;
import us.mn.state.dot.tdxml.Location;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * An extended version of EventTime that is agency specific
 * to Caltrans District 10 and the CHP XML incident format.
 *
 * @see us.mn.state.dot.tdxml.EventTime
 *
 * @author Erik Engstrom
 * @author Michael Darter
 */
public class CHPEventTime implements EventTime
{
	private static final DateFormat m_dateFormat =
		new SimpleDateFormat("M/d/y h:m:s a");

	// attributes
	private Date m_date = new Date();
	private boolean m_valid = false;

	/**
	 * Constructor.
	 */
	public CHPEventTime() {}

	/**
	 *  Create a new CHPEventTime from a Log element in the
	 *  CHP xml incident file.
	 *
	 *  @param e A Log element within the CHP xml file.
	 */
	public CHPEventTime(Element e) throws ParseException {

		// correct xml tag?
		assert e.getNodeName().equals("Log") :
		       "CHPEventTime.CHPEventTime.1";

		// parse date
		m_date = this.readDate(e);

		m_valid = true;
	}

	/** toString */
	public String toString() {
		return (this.m_date.toString()
			+ (this.isValid() ? "" : " Invalid."));
	}

	/** Return true if EventTime is valid */
	public boolean isValid() {
		return (m_valid);
	}

	/** artifact of superclass. Should never be called */
	public long getDuration() {
		assert false :
		       "CHPEventTime.getDuration() should never be called.";
		return (0);
	}

	/** Read the date from the CHP xml file given the "Log" element */
	private Date readDate(Element e) throws ParseException {

		// preconds
		if(!Contract.verify("CHPEventTime.readDate.1", e != null))
			return (new Date());
		if(!Contract.verify("CHPEventTime.readDate.2",
			e.getNodeName().equals("Log")))
			return (new Date());

		try {
			// get date (format "2/8/2008 8:21:08 AM")
			String dt1 = AbstractXmlFactory.lookupChildText(e, "LogTime");
			String dt2 = SString.removeEnclosingQuotes(dt1);
			if (dt2==null || dt2.length()<=0)
				throw new ParseException("zero length date string for CHP LogTime",0);

			// Log.info("date string="+dt2);
			Date d = m_dateFormat.parse(dt2);  // throws NullPointerException

			// Log.info("parsed date="+d.toString());
			return (d);

		// handle all errors as parse errors
		} catch (Exception ex) {
			throw new ParseException(ex.toString(),0); 
		}
	}
}

