/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import us.mn.state.dot.tdxml.cars.CarsIncidentFactory;

/**
 * Reads an xml document at a specified interval and notifies listeners
 * when there is new data available.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class XmlIncidentClient extends XmlClient {

	/** Factory for creating incidents from an XML stream */
	protected final XmlIncidentFactory factory;

	/** Create a new XML incident client */
	public XmlIncidentClient(URL u, Logger l) throws TdxmlException {
		super(u, l);
		factory = createIncidentFactory(l);
	}

	/** Create incident factory, called by constructor.
	 * May be overridden by each agency. */
	protected XmlIncidentFactory createIncidentFactory(Logger logger)
		throws TdxmlException
	{
		try {
			return new CarsIncidentFactory(logger);
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

	/** Notify listeners of an incident */
	protected void notifyIncident(final Incident i) {
		doNotify(new Notifier() {
			void notify(TdxmlListener l) {
				IncidentListener il = (IncidentListener)l;
				il.update(i);
			}
		});
	}

	/** Parse the incidents in an XML document.
	 * May be overridden by each agency. */
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
		final int TIMEOUT_MS=60000;

		// sanity checks
		if (builder==null)
			throw new IllegalStateException("builder is null in readXmlFile().");

		logger.info("Opening connection to " + url);
		URLConnection conn = url.openConnection();
		if (conn==null) 
			throw new IllegalStateException("conn is null in readXmlFile().");
		logger.info("Setting connect timeout on " + url);
		conn.setConnectTimeout(TIMEOUT_MS);
		logger.info("Setting read timeout on " + url);
		conn.setReadTimeout(TIMEOUT_MS);
		logger.info("Getting input stream from " + url);
		InputStream in = conn.getInputStream();
		logger.info("Reading data from " + url);
		Document doc = builder.parse(in);
		logger.info("Parsing document for " + url);
		parse(doc);
		logger.info("Parse complete for " + url);
	}

	/** Parse the XML document and notify clients.
	 * May be overridden by each agency. */
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
