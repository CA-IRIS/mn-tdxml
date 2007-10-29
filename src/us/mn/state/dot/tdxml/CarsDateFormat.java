/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 *
 * @author Erik Engstrom
 */
public class CarsDateFormat extends DateFormat {

	static private final DecimalFormat DEC_2 = new DecimalFormat("00");

	/** Create a CarsDateFormat */
	public CarsDateFormat() {
		super();
	}

	/**
	 * @see java.text.DateFormat#format(Date, StringBuffer, FieldPosition)
	 */
	public StringBuffer format(Date date, StringBuffer buffer,
		FieldPosition fieldPosition)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		buffer.append(c.get(Calendar.YEAR));
		buffer.append('-');
		buffer.append(DEC_2.format(c.get(Calendar.MONTH) + 1));
		buffer.append('-');
		buffer.append(DEC_2.format(c.get(Calendar.DAY_OF_MONTH)));
		buffer.append('T');
		buffer.append(DEC_2.format(c.get(Calendar.HOUR_OF_DAY)));
		buffer.append(':');
		buffer.append(DEC_2.format(c.get(Calendar.MINUTE)));
		buffer.append(':');
		buffer.append(DEC_2.format(c.get(Calendar.SECOND)));
		int offset = c.get(Calendar.ZONE_OFFSET);
		int offsetHours = offset / 3600000;
		buffer.append('Z');
		buffer.append(DEC_2.format(offsetHours));
		buffer.append(':');
		int offsetMins = (offset % 3600000) / 60000;
		buffer.append(DEC_2.format(offsetMins));
		return buffer;
	}

	/**
	 * @see java.text.DateFormat#parse(String, ParsePosition)
	 */
	public Date parse(String source, ParsePosition pos) {
		int i = pos.getIndex();
		int year = Integer.parseInt(source.substring(i, i + 4));
		int month = Integer.parseInt(source.substring(i + 5, i + 7)) -1;
		int date = Integer.parseInt(source.substring(i + 8, i + 10));
		int hour = Integer.parseInt(source.substring(i + 11, i + 13));
		int minute = Integer.parseInt(source.substring(i + 14, i + 16));
		int second = Integer.parseInt(source.substring(i + 17, i + 19));
		String zoneString = source.substring(i + 20, i + 26);
		TimeZone zone = TimeZone.getTimeZone("GMT" + zoneString);
		Calendar calendar = Calendar.getInstance(zone);
		calendar.set(year, month, date, hour, minute, second);
		pos.setIndex(i + 26);
		return calendar.getTime();
	}
}
