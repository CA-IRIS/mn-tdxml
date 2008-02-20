/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;
import us.mn.state.dot.util.HTTPProxySelector;

/**
 * This class was developed to test the installation of the tdxml package.
 *
 * @author Erik Engstrom
 */
public class Main {

	/** Name of the properties file. */
	private String propertyFile = "tdxml.properties";

	/** The xml stream to connect to */
	private final String stream = "xstation";

	private final Logger logger = Logger.getAnonymousLogger();

	/**
	 * The main method will print 30 second ramp meter data to system out.
	 *
	 * @param args   The command line arguments
	 */
	public static void main(String[] args) {
		try {
			Main main = new Main();
			Properties props = main.createProperties();
			ProxySelector.setDefault(new HTTPProxySelector(props));
			XmlClient client = main.createClient(props);
			client.setDaemon(false);
			client.start();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Create an IncidentClient from the provided Properties.
	 *
	 * @param props   User properties.
	 * @return        XmlClient initialized with the provided properties.
	 */
	protected XmlClient createXmlIncidentClient(Properties props)
		throws DdsException
	{
		logger.info("Creating XmlIncidentClient");
		XmlIncidentClient client = new XmlIncidentClient(props, logger);
		IncidentListener listener = new IncidentListener() {
			int n_incidents = 0;
			public void update(boolean finish) {
				if(finish) {
					System.out.println("There are " +
						n_incidents + " incidents");
				} else
					n_incidents = 0;
			}
			public void update(Incident i) {
				System.out.println(i.toString());
				n_incidents++;
			}
		};
		client.addTdxmlListener(listener);
		return client;
	}

	/**
	 * Creates an XmlStationClient from the provided properties.
	 *
	 * @param props   User properties.
	 * @return        XmlClient initialized with the provided properties.
	 */
	protected XmlClient createXmlStationClient(Properties props)
		throws DdsException
	{
		logger.info("Creating XmlStationClient");
		XmlStationClient client = new XmlStationClient(props, logger);
		client.addTdxmlListener(new StationListener() {
			public void update(boolean finish) {
				String sf = finish ? "finish": "start";
				System.out.println("Station data " + sf +
					" at " + new Date());
			}
			public void update(StationSample s) {
				System.out.println("Station " + s.id +
					", flow=" + s.flow +
					", speed=" + s.speed);
			}
		});
		return client;
	}

	/** Create an XmlClient using the provided properties object */
	public XmlClient createClient(Properties properties)
		throws DdsException
	{
		if(stream.equals("xincident"))
			return createXmlIncidentClient(properties);
		else
			return createXmlStationClient(properties);
	}

	/**
	 * Creates a Properties object from the commandline arguments.
	 *
	 * @return       a Properties object containing user parameters.
	 */
	private Properties createProperties() throws IOException {
		Properties props = new Properties();
		FileInputStream is = new FileInputStream(propertyFile);
		props.load(is);
		is.close();
		logger.info("Properties = " + props.toString());
		return props;
	}

}
