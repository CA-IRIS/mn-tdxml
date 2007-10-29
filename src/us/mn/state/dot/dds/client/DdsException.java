/*
 * DDS Client -- Data Distribution Server Client
 * Copyright (C) 2007  Minnesota Department of Transportation
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

/**
 * Exception to encapsulate other errors.
 *
 * @author Erik Engstrom
 */
public class DdsException extends Exception {

	/**
	 * Constructor for DdsException.
	 */
	public DdsException() {
		super();
	}

	/**
	 * Constructor for DdsException.
	 * @param message
	 */
	public DdsException(String message) {
		super(message);
	}

	/**
	 * Constructor for DdsException.
	 * @param message
	 * @param cause
	 */
	public DdsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for DdsException.
	 * @param cause
	 */
	public DdsException(Throwable cause) {
		super(cause);
	}
}
