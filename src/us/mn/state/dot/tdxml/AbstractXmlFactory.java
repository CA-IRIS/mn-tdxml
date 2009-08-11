/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An abstract factory for parsing XML stuff.
 *
 * @author Douglas Lau
 */
abstract public class AbstractXmlFactory {

	/**
	 * Get the first child with the specified name.
	 * @param e The parent element
	 * @param name The name of the child element to return
	 * @return The child element.
	 */
	static public Element lookupChild(Element e, String name) {
		NodeList children = e.getChildNodes();
		for(int c = 0; c < children.getLength(); c++) {
			Node child = children.item(c);
			if(child instanceof Element &&
			   child.getNodeName().equalsIgnoreCase(name))
				return (Element)child;
		}
		return null;
	}

	/**
	 * Get the text value of the child with specified name.
	 * @param e The parent element.
	 * @param name The name of the child.
	 * @return The text value of the child.
	 */
	static public String lookupChildText(Element e, String name) {
		Element c = lookupChild(e, name);
		if(c != null)
			return c.getTextContent();
		else
			return null;
	}

	/**
	 * Lookup all child elements with the given name
	 * @param parent The parent element
	 * @param name The name of the children to return.
	 * @param cb An interface to callback for each matching child.
	 */
	static public void lookupChildren(Element parent, String name,
		ElementCallback cb)
	{
		NodeList nodes = parent.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if(n instanceof Element &&
			   n.getNodeName().equalsIgnoreCase(name))
				cb.processElement((Element)n);
		}
	}
}
