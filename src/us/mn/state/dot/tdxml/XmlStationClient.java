/*
 * TDXML -- Traffic Data XML Reader
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
package us.mn.state.dot.tdxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads the station XML document at a specified interval and notifies listeners
 * when there is new data available.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class XmlStationClient extends XmlClient {

	/** Entity declaration */
	static protected final String ENTITY_DECL =
		"<?xml version='1.0' encoding='UTF-8'?>";

	/** Strip the "S" prefix from a station ID */
	static protected String stripPrefix(String sensor) {
		if(sensor.startsWith("S"))
			return sensor.substring(1);
		else
			return sensor;
	}

	/** Parse an attribute as an integer value */
	static protected int parseInt(String v) {
		try {
			if(v != null)
				return Integer.parseInt(v);
		}
		catch(NumberFormatException e) {
			// Invalid value
		}
		return StationSample.MISSING_DATA;
	}

	/** SAX parser */
	protected final SAXParser parser;

	/** Time stamp from previous read */
	protected String last_stamp = "";

	/** Flag to indicate the time stamp changed since last time */
	protected boolean time_changed = false;

	/** Create a new XmlStationClient */
	public XmlStationClient(Properties props, Logger l)
		throws DdsException
	{
		super(props.getProperty("tdxml.station.url"), l);
		try {
			SAXParserFactory factory =
				SAXParserFactory.newInstance();
			parser = factory.newSAXParser();
		}
		catch(ParserConfigurationException e) {
			throw new DdsException(e);
		}
		catch(SAXException e) {
			throw new DdsException(e);
		}
	}

	/** Notify listeners of a station data sample */
	protected void notifySample(final StationSample s) {
		doNotify(new Notifier() {
			void notify(DdsListener l) {
				StationListener sl = (StationListener)l;
				sl.update(s);
			}
		});
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
		InputStream in = new GZIPInputStream(conn.getInputStream());
		logger.info("Parsing XML for " + location);
		parse(in);
		logger.info("Parse complete for " + location);
	}

	/** Parse the station.xml document and notify clients */
	protected void parse(InputStream in) throws IOException, SAXException {
		notifyStart();
		try {
			DefaultHandler h = new DefaultHandler() {
				public InputSource resolveEntity(
					String publicId, String systemId)
					throws IOException, SAXException
				{
					return new InputSource(
						new StringReader(ENTITY_DECL));
				}
				public void startElement(String uri,
					String localName, String qname,
					Attributes attrs)
				{
					if(qname.equals("traffic_sample"))
						handleTrafficSample(attrs);
					if(qname.equals("sample"))
						handleSample(attrs);
				}
			};
			parser.parse(in, h);
		}
		finally {
			notifyFinish();
		}
	}

	/** Handle a traffic_sample element */
	protected void handleTrafficSample(Attributes attrs) {
		String stamp = attrs.getValue("time_stamp");
		time_changed = !stamp.equals(last_stamp);
		last_stamp = stamp;
	}

	/** Notify listeners of one sensor sample */
	protected void notifySensorSample(String sensor, String f, String s) {
		String id = stripPrefix(sensor);
		int flow = parseInt(f);
		int speed = parseInt(s);
		if(flow >= 0 || speed >= 0)
			notifySample(new StationSample(id, flow, speed));
	}

	/** Handle one station sample element */
	protected void handleSample(Attributes attrs) {
		if(time_changed) {
			String sensor = attrs.getValue("sensor");
			String flow = attrs.getValue("flow");
			String speed = attrs.getValue("speed");
			if(sensor != null)
				notifySensorSample(sensor, flow, speed);
		}
	}
}
