/*
 * DDS Client -- Data Distribution Server Client
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.dds.client;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads an xml document at a specified interval and notifies listeners
 * when there is new data available.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class XmlIncidentClient extends XmlClient {

	protected final XmlIncidentFactory factory;

	/**
	 * Constructor for XmlIncidentClient that uses a Properties object to
	 * configure it.  Available properties are:
	 *  dds.incident.url which is the url of the incident stream.
	 */
	public XmlIncidentClient(Properties props, Logger l)
		throws DdsException
	{
		super(props.getProperty("dds.incident.url"), l);
		try {
			factory = new DOMXmlIncidentFactory(props, l);
		}
		catch(IOException e) {
			throw new DdsException(e);
		}
		catch(ParserConfigurationException e) {
			throw new DdsException(e);
		}
		catch(SAXException e) {
			throw new DdsException(e);
		}
	}

	/** Notify listeners of an incident */
	protected void notifyIncident(final Incident i) {
		doNotify(new Notifier() {
			void notify(DdsListener l) {
				IncidentListener il = (IncidentListener)l;
				il.update(i);
			}
		});
	}

	/** Parse the incidents in an XML document */
	protected void parseIncidents(Element root) throws IncidentException {
		NodeList nodes = root.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element)nodes.item(i);
			Incident inc = factory.createIncident(e);
			if(inc.isValid())
				notifyIncident(inc);
		}
	}

	/** Read and parse an XML file */
	protected void readXmlFile() throws Exception {
		logger.info("Creating URL for " + location);
		URL url = new URL(location);
		logger.info("Openning connection to " + location);
		URLConnection conn = url.openConnection();
		logger.info("Setting connect timeout on " + location);
		conn.setConnectTimeout(60000);
		logger.info("Setting read timeout on " + location);
		conn.setReadTimeout(60000);
		logger.info("Getting input stream from " + location);
		InputStream in = conn.getInputStream();
		logger.info("Reading data from " + location);
		Document doc = builder.parse(in);
		logger.info("Parsing document for " + location);
		parse(doc);
		logger.info("Parse complete for " + location);
	}

	/** Parse the XML document and notify clients */
	protected void parse(Document doc) throws IncidentException {
		logger.info("Parsing incident document");
		notifyStart();
		try {
			parseIncidents(doc.getDocumentElement());
		}
		catch(IncidentException e) {
			logger.info(e.getMessage());
		}
		finally {
			notifyFinish();
		}
	}
}
