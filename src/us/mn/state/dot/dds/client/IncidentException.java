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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Signals that an error has been reached unexpectedly in creating an incident.
 *
 * @author Erik Engstrom
 */
public class IncidentException extends Exception {

	/** the nested exception */
	private Throwable nestedException;

	/** Creates new IncidentException */
	public IncidentException() {
		super();
	}

	public IncidentException( String message ) {
		super( message );
	}

	public IncidentException(Throwable nestedException) {
		this.nestedException = nestedException;
	}

	public IncidentException(String message, Throwable nestedException) {
		this(message);
		this.nestedException = nestedException;
	}

	/** Get the nested exception */
	public Throwable getNestedException() {
		return nestedException;
	}

	public String getMessage() {
		String result = super.getMessage();
		Throwable nested = getNestedException();
		if(nested != null) {
			String nestedMsg = nested.getMessage();
			if(result == null)
				result = nestedMsg;
			else
				result += ": " + nestedMsg;
		}
		return result;
	}

	public String toString() {
		String result = super.toString();
		if(getNestedException() != null) {
			result += "; \n\t nested exception = " +
				getNestedException().toString();
		}
		return result;
	}

	public void printStackTrace() {
		printStackTrace(System.err);
	}

	public void printStackTrace(PrintWriter writer) {
		super.printStackTrace(writer);
		Throwable nested = getNestedException();
		if(nested != null) {
			writer.write("Nested Exception = \n");
			nested.printStackTrace(writer);
		}
		writer.flush();
	}

	public void printStackTrace(PrintStream stream) {
		printStackTrace(new PrintWriter(stream));
	}
}
