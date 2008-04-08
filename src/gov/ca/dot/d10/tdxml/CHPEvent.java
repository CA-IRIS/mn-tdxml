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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import us.mn.state.dot.tdxml.AbstractXmlFactory;

/**
 * An incident event.
 *
 * @author Erik Engstrom
 * @author Michael Darter
 */
public class CHPEvent {

    private String m_type="";       // either "details" or "units"
	private String m_time="";       // <DetailTime> e.g. "1:45PM" 
	private String m_detail="";     // <IncidentDetail> e.g. "DOG RUNNING ON RS"

    /**
     * construct a single event using the passed argument 
     * CHP xml element <details> or <units>.
     */
	public CHPEvent(Element element) {

        // preconds
        if( element==null )
            return;
        String eName=SString.removeEnclosingQuotes(element.getNodeName());
        if( eName==null )
            return;

        // correct xml tag?
        assert eName.equals("details") || eName.equals("units") : "incorrect XML tag";

        // parse <details>
        if( eName.equals("details")) {
            this.m_type="Details";
            this.m_time=SString.removeEnclosingQuotes(
                AbstractXmlFactory.lookupChildText(element,"DetailTime"));
            this.m_detail=SString.removeEnclosingQuotes(
                AbstractXmlFactory.lookupChildText(element,"IncidentDetail"));
        }

        // parse <units>
        else if( eName.equals("units")) {
            this.m_type="Units";
            this.m_time=SString.removeEnclosingQuotes(
                AbstractXmlFactory.lookupChildText(element,"DetailTime"));
            this.m_detail=SString.removeEnclosingQuotes(
                AbstractXmlFactory.lookupChildText(element,"IncidentDetail"));
        }

        else {
            System.err.println("CHPEvent.CHPEvent.1: unexpected XML structure: "+element.getNodeName());
        }
	}

    /** return event time */
	public String getTime() {
		return m_time;
	}

    /** return event type */
	public String getType() {
		return m_type;
	}

    /** return event detail */
	public String getDetail() {
		return m_detail;
	}

    /** artifact */
	public String getMessage() {
		return getDetail();
	}

    /**
     * Static method to return a container of all events in the given CHP "Log" element.
     */
	public static List<CHPEvent> parseEvents(Element element) {

        // return value
        List<CHPEvent> events=new ArrayList<CHPEvent>();

        // preconds
        if( element==null )
            return(events);

        // correct xml tag?
        assert element.getNodeName().equals("Log") : "CHPEvent.parseEvents.0";

        // get <LogDetails> element
        Element logDetails=AbstractXmlFactory.lookupChild(element,"LogDetails");
        if( logDetails==null )
            return(events);

        // for each <details> and <units> within <LogDetails>
    	NodeList nodes = logDetails.getChildNodes();
    	for(int c = 0; c < nodes.getLength(); c++) 
        {
            // get <details> or <units>
    		Node n = nodes.item(c);
            if( n.getNodeType()!=Node.ELEMENT_NODE )
                continue;

            // add new event to container
            CHPEvent ne=new CHPEvent((Element)n);
            events.add(ne);
            //Log.info("Added new CHPEvent:"+ne.toString());
    	}

        return(events);
	}

    /** toString */
	public String toString() {
        return(this.getType()+","+this.getTime()+","+this.getDetail());
    }

}
