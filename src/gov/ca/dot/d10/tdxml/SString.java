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
 * Static String methods, provides static string convenience methods.
 *
 * @author Michael Darter
 */
public class SString
{
	/**
	 * Constructor. Should never be called.
	 */
	SString() {
		Contract.fail("Contract.Contract()");
	}

	/**
	 *  Given a filled field and string, return a string
	 *  containing the field with the string right justified.
	 *  e.g. ("0000","XY") returns "00XY".
	 */
	static public String toRightField(String f, String s) {
		if(!Contract.verify(
			"SString.toRightField: arg f or s is null.",
			(f != null) && (s != null)))
			return ("");
		if(!Contract.verify("SString.toRightField: arg length problem:"
				    + f + "," + s, f.length() >= s.length()))
			return (f);

		int end = f.length() - s.length();
		String ret = f.substring(0, end) + s;
		return (ret);
	}

	/**
	 *  test methods.
	 */
	static public boolean test() {
		boolean ok = true;

		// toRightField
		ok = ok && (new String("").compareTo(SString.toRightField(null,
			"")) == 0);
		ok = ok && (new String("").compareTo(SString.toRightField("",
			null)) == 0);
		ok = ok && (new String("").compareTo(SString.toRightField(null,
			null)) == 0);
		ok = ok && (new String("").compareTo(SString.toRightField("",
			"")) == 0);
		ok = ok && (new String("1234a").compareTo(
			SString.toRightField("12345", "a")) == 0);
		ok = ok && (new String("1abcd").compareTo(
			SString.toRightField("12345", "abcd")) == 0);
		ok = ok && (new String("12345").compareTo(
			SString.toRightField("12345", "")) == 0);
		ok = ok && (new String("abcdef").compareTo(
			SString.toRightField("123456", "abcdef")) == 0);
		ok = ok && (new String("12345").compareTo(
			SString.toRightField("12345", "abcdef")) == 0);

		// removeEnclosingQuotes
		ok = ok && (new String("abcd").compareTo(
			SString.removeEnclosingQuotes("abcd")) == 0);
		ok = ok && (new String("abcd").compareTo(
			SString.removeEnclosingQuotes("\"abcd\"")) == 0);
		ok = ok && (new String("").compareTo(
			SString.removeEnclosingQuotes("")) == 0);
		ok = ok && (null == SString.removeEnclosingQuotes(null));
		ok = ok && (new String("\"abcd\" ").compareTo(
			SString.removeEnclosingQuotes("\"abcd\" ")) == 0);
		ok = ok && (new String("x").compareTo(
			SString.removeEnclosingQuotes("\"x\"")) == 0);

		return (ok);
	}

	/**
	 *   return a hexstring given an integer. This method is like the Java
	 *   method but converts the string to upper case.
	 */
	static public String toHexString(int i) {
		String hex = Integer.toHexString(i);
		hex = hex.toUpperCase();
		return (hex);
	}

	/**
	 *  Return a string with the enclosing double quotes removed.
	 *  This method assumes the first and last chars are \" and
	 *  if not the string is returned unmodified.
	 */
	static public String removeEnclosingQuotes(String s) {
		if(s == null)
			return (null);
		if((s.length() >= 2) && (s.charAt(0) == '\"')
			&& (s.charAt(s.length() - 1) == '\"'))
			return (s.substring(1, s.length() - 1));
		return (s);
	}
}
