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

/**
 * Contract related class.
 *
 * @author      Michael Darter, AHMCT
 * @version     1.0, 12/03/07
 * @since       1.0
 * @see
 */
public class Contract
{
	/**
	 *  Test an assertion. true is returned if ok, else false.
	 */
	public static boolean verify(String tag, boolean ok) {
		if(!ok)
			Contract.fail(tag);
		return (ok);
	}

	/**
	 *  Explicit contract failure.
	 */
	public static void fail(String tag, String msg) {
		Contract.fail(tag + "," + msg);
	}

	/**
	 *  Explicit contract failure, root fail.
	 */
	public static void fail(String msg) {
		System.err.println(msg);
	}
}
