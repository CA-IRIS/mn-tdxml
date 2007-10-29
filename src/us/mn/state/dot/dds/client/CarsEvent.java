/*
 * DDS Client -- Data Distribution Server Client
 * Copyright (C) 2003-2007  Minnesota Department of Transportation
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

import org.w3c.dom.Element;

/**
 * @author Erik Engstrom
 */
public class CarsEvent {

	private final String type;
	private final String message;

	public CarsEvent(Element element) {
		type = element.getNodeName();
		message = element.getTextContent();
	}

	public String getMessage() {
		return message;
	}

	public String getType() {
		return type;
	}
}
