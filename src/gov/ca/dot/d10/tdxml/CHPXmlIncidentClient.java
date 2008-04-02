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

import java.util.logging.Logger;
import java.util.Properties;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;

import us.mn.state.dot.tdxml.XmlIncidentFactory;
import us.mn.state.dot.tdxml.IncidentException;
import us.mn.state.dot.tdxml.XmlIncidentClient;
import us.mn.state.dot.tdxml.TdxmlException;
import us.mn.state.dot.tdxml.Incident;

import gov.ca.dot.common.Log;

/**
 * A Caltrans CHP specific class for reading an xml document at a specified
 * interval and notifying listeners that new data is available.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class CHPXmlIncidentClient extends XmlIncidentClient {

	/**
	 * Constructor for CHPXmlIncidentClient.
	 */
	public CHPXmlIncidentClient(Properties props, Logger l)
		throws TdxmlException
	{
        super(props,l);
	}

	/** create incident factory, called by constructor, may be overridden by each agency. */
	protected XmlIncidentFactory createIncidentFactory(Properties props, Logger logger) 
		throws TdxmlException
    {
		try {
    	    return(new CHPXmlIncidentFactory(props, logger));
		}
		catch(IOException e) {
			throw new TdxmlException(e);
		}
		catch(ParserConfigurationException e) {
			throw new TdxmlException(e);
		}
		catch(SAXException e) {
			throw new TdxmlException(e);
		}
	}

	/** 
     * Parse the incidents in an XML document. Agency specific. 
     * The argument Element tag name must be "State".
     */
	protected void parseIncidents(Element root) throws IncidentException {

        //Log.info("CHPXmlIncidentClient.parseIncidents("+root.getTagName()+") called.");

        // preconds
        if( !Contract.verify("CHPXmlIncidentClint.parseIncidents.0",root.getTagName().equals("State")) )
            return;

        // for each Center within State
    	NodeList nodes = root.getChildNodes();
    	for(int c = 0; c < nodes.getLength(); c++) 
        {
            // get Center
    		Node n = nodes.item(c);
            if( n.getNodeType()!=Node.ELEMENT_NODE )
                continue;

            // parse Center
            this.parseXmlElementCenter((Element)n);
    	}
	}

	/** 
     * 
     * The argument Element tag name must be "Center".
     */
	private void parseXmlElementCenter(Element e) throws IncidentException {

        //Log.info("CHPXmlIncidentClient.parseXmlElementCenter("+e.getTagName()+") called.");

        // preconds
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementCenter.1",e.getNodeName().equals("Center")) )
            return;
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementCenter.2",e.hasAttributes()) )
            return;
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementCenter.3",e.hasChildNodes()) )
            return;

        // get attribute: ID
        NamedNodeMap attrs = e.getAttributes();
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementCenter.4",attrs.getLength()>=1) )
            return;
        Attr a=(Attr)attrs.item(0);
        String cid=a.getNodeValue();
        //Log.info(" " + a.getNodeName()+"="+cid);

        // for each Dispatch within Center
		NodeList nodes = e.getChildNodes();
		for(int d = 0; d < nodes.getLength(); d++) 
        {
            // get Dispatch
			Node n = nodes.item(d);
            if( n.getNodeType()!=Node.ELEMENT_NODE )
                continue;

            // parse Dispatch
            this.parseXmlElementDispatch(cid,(Element)n);
		}
	}

	/** 
     * 
     * The argument Element tag name must be "Dispatch".
     */
	private void parseXmlElementDispatch(String cid,Element e) throws IncidentException {

        //Log.info("CHPXmlIncidentClient.parseXmlElementDispatch("+e.getTagName()+") called.");

        // preconds
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementDispatch.1",e.getNodeName().equals("Dispatch")) )
            return;
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementDispatch.2",e.hasAttributes()) )
            return;
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementDispatch.3",e.hasChildNodes()) )
            return;

        // get attribute: ID
        NamedNodeMap attrs = e.getAttributes();
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementDispatch.4",attrs.getLength()>=1) )
            return;
        Attr a=(Attr)attrs.item(0);
        String did=a.getNodeValue();
        //Log.info(" " + a.getNodeName()+"="+did);

        // for each Log within Dispatch
		NodeList nodes = e.getChildNodes();
		for(int d = 0; d < nodes.getLength(); d++) 
        {
            // get Log
			Node n = nodes.item(d);
            if( n.getNodeType()!=Node.ELEMENT_NODE )
                continue;

            // parse Log
            this.parseXmlElementLog(cid,did,(Element)n);
		}
	}

	/** 
     * 
     * The argument Element tag name must be "Log".
     */
	private void parseXmlElementLog(String cid,String did,Element e) throws IncidentException {

        //Log.info("CHPXmlIncidentClient.parseXmlElementLog("+e.getTagName()+") called.");

        // preconds
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementLog.1",e.getNodeName().equals("Log")) )
            return;
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementLog.2",e.hasAttributes()) )
            return;
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementLog.3",e.hasChildNodes()) )
            return;

        // get attribute: ID
        NamedNodeMap attrs = e.getAttributes();
        if( !Contract.verify("CHPXmlIncidentClint.parseXmlElementLog.4",attrs.getLength()>=1) )
            return;
        Attr a=(Attr)attrs.item(0);
        String logid=a.getNodeValue();
        //Log.info("log id="+logid);

        // create incident
        this.createIncidentAndNotify(cid,did,logid,e);
	}

	/** 
     * Create an incident and notify listeners.
     */
    private void createIncidentAndNotify(String cid,String did,String logid,Element e) 
        throws IncidentException 
    {
        CHPXmlIncidentFactory chpf=(CHPXmlIncidentFactory)factory;
		Incident inc = chpf.createIncident(cid,did,e);

        // failure
		if( inc==null ) {
        //    Log.info("CHPXmlIncidentClient.parseXmlElementLog(): incident is NOT valid: cid="+
        //        cid+", did="+did+", log id="+logid);
        }

        // success and valid incident
        else if( inc.isValid() ) {
            //Log.info("CHPXmlIncidentClient.parseXmlElementLog(): notifying listeners of valid incident: cid="+
            //    cid+", did="+did+", log id="+logid+". toString="+inc.toString());
            this.notifyIncident(inc);
        }

        // success but invalid incident
        else {
            //Log.info("CHPXmlIncidentClient.parseXmlElementLog(): incident is NOT valid: cid="+
            //    cid+", did="+did+", log id="+logid+". toString="+inc.toString());
        }
    }
}
