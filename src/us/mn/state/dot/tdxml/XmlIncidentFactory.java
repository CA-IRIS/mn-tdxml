/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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

import org.w3c.dom.Element;

/**
 * The <code>IncidentFactory</code> is responsible for translating the ITS
 * encoded <code>EncodedIncident</code>s data into incident objects.  The 
 * appropriate lookup tables need to be available from a database, and the
 * appropriate <code>ConnectionInfo</code> for that database passed in the
 * constructor.
 *
 * @author Erik Engstrom
 */
public interface XmlIncidentFactory {

	/**
	 * Create a new <code>Incident</code> from an event-report-message
	 * <code>Element</code>
	 * @return a new Incident.
	 */
	public Incident createIncident( Element eventReportMessage ) 
		throws IncidentException;
}
