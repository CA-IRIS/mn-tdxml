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
package us.mn.state.dot.tdxml.cars;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import us.mn.state.dot.tdxml.AbstractXmlFactory;
import us.mn.state.dot.tdxml.EventTime;

/**
 * This class represents an EMF.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class CarsEventTime implements EventTime {

	/** Constant for the number of minutes per day */
	static protected final int MINUTES_PER_DAY = 1440;

	private static final String WEEKDAYS_KEY = "Weekdays";
	private static final String WEEKENDS_KEY = "Weekends";

	private static final CarsDay SUNDAY =
		new CarsDay("Sundays", (byte)1, Calendar.SUNDAY);
	private static final CarsDay MONDAY =
		new CarsDay("Mondays", (byte)2, Calendar.MONDAY);
	private static final CarsDay TUESDAY =
		new CarsDay("Tuesdays", (byte)4, Calendar.TUESDAY);
	private static final CarsDay WEDNESDAY =
		new CarsDay("Wednesdays", (byte)8, Calendar.WEDNESDAY);
	private static final CarsDay THURSDAY =
		new CarsDay("Thursdays", (byte)16, Calendar.THURSDAY);
	private static final CarsDay FRIDAY =
		new CarsDay("Fridays", (byte)32, Calendar.FRIDAY);
	private static final CarsDay SATURDAY =
		new CarsDay("Saturdays", (byte)64, Calendar.SATURDAY);

	private static final byte WEEKDAYS =
		(byte)(MONDAY.byteValue | TUESDAY.byteValue |
		WEDNESDAY.byteValue | THURSDAY.byteValue | FRIDAY.byteValue);

	private static final byte WEEKENDS =
		(byte)(SUNDAY.byteValue | SATURDAY.byteValue);

	private static final Map<Integer, Byte> CAL_TO_BYTE =
		new HashMap<Integer, Byte>(7);

	private static final DateFormat dateFormat = new CarsDateFormat();

	private static final DateFormat outDateFormat =
		new SimpleDateFormat("h:mm a', 'MM/dd/yy");

	/** Event start time */
	private final Date startTime;

	/** Event end time */
	private final Date endTime;

	/** Is the event recurrent? */
	private final boolean recurrent;

	/** Event duration (units?) */
	private final int duration;

	/** Weekdays for recurrent events */
	private final byte bDays;

	/** Start of scheduled event time */
	private final String scheduleStart;

	/** End of scheduled event time */
	private final String scheduleEnd;

	private String effectiveQualifier = null;

	static {
		CAL_TO_BYTE.put(Calendar.SUNDAY, SUNDAY.getByte());
		CAL_TO_BYTE.put(Calendar.MONDAY, MONDAY.getByte());
		CAL_TO_BYTE.put(Calendar.TUESDAY, TUESDAY.getByte());
		CAL_TO_BYTE.put(Calendar.WEDNESDAY, WEDNESDAY.getByte());
		CAL_TO_BYTE.put(Calendar.THURSDAY, THURSDAY.getByte());
		CAL_TO_BYTE.put(Calendar.FRIDAY, FRIDAY.getByte());
		CAL_TO_BYTE.put(Calendar.SATURDAY, SATURDAY.getByte());
	}

	static int parseDuration(Element validPeriod) {
		String d = AbstractXmlFactory.lookupChildText(validPeriod,
			"event-timeline-estimated-duration");
		if(d != null)
			return Integer.parseInt(d);
		else
			return 0;
	}

	static byte parseDaysOfWeek(Element eventPeriod) {
		String b = AbstractXmlFactory.lookupChildText(eventPeriod,
			"event-timeline-schedule-days-of-the-week");
		if(b != null)
			return Byte.parseByte(b);
		else
			return 0;
	}

	/**
	 * Create a new CARS event time.
	 */
	public CarsEventTime( Element element ) throws ParseException {
		super();
		startTime = readDate(AbstractXmlFactory.lookupChild(element,
			"start-time"));
		Element validPeriod = AbstractXmlFactory.lookupChild(element,
			"valid-period");
		endTime = readDate(AbstractXmlFactory.lookupChild(validPeriod,
			"expected-end-time"));
		duration = parseDuration(validPeriod);
		Element recurrentTimes = AbstractXmlFactory.lookupChild(element,
			"recurrent-times");
		if(recurrentTimes != null)
			recurrentTimes = AbstractXmlFactory.lookupChild(
				recurrentTimes, "eventRecurrentTimes");
		recurrent = recurrentTimes != null;
		if(recurrent) {
			Element eventPeriod = AbstractXmlFactory.lookupChild(
				recurrentTimes, "event-period");
			String scheduledTimes = AbstractXmlFactory.lookupChildText(
				recurrentTimes, "event-timeline-schedule-times");
			if(null != scheduledTimes && !"".equals(scheduledTimes)){
				scheduleStart = scheduledTimes.substring(0, 4);
				scheduleEnd = scheduledTimes.substring(4, 8);
			} else {
				scheduleStart = null;
				scheduleEnd = null;
			}
			effectiveQualifier = AbstractXmlFactory.lookupChildText(
				eventPeriod, "event-effective-period-qualifier");
			if("not-specified".equals(effectiveQualifier))
				effectiveQualifier = null;
			if(effectiveQualifier != null)
				effectiveQualifier =
					effectiveQualifier.replace('-', ' ');
			bDays = parseDaysOfWeek(eventPeriod);
		} else {
			scheduleStart = null;
			scheduleEnd = null;
			bDays = 0;
		}
	}

	private StringBuffer getWeekdays(byte b, boolean start){
		boolean comma = !start;
		StringBuffer buffer = new StringBuffer();
		comma = checkDay(comma, b, MONDAY, buffer) || comma;
		comma = checkDay(comma, b, TUESDAY, buffer) || comma;
		comma = checkDay(comma, b, WEDNESDAY, buffer) || comma;
		comma = checkDay(comma, b, THURSDAY, buffer) || comma;
		comma = checkDay(comma, b, FRIDAY, buffer) || comma;
		return buffer;
	}

	private boolean checkDay(boolean comma, byte b, CarsDay day,
		StringBuffer buffer)
	{
		boolean result = day.contains(b);
		if(result) {
			if(comma)
				buffer.append(", ");
			buffer.append(day.name);
		}
		return result;
	}

	/** Append a time interval to a string builder */
	static protected boolean appendInterval(StringBuilder buf, String units,
		long value, boolean comma)
	{
		if(comma && value > 0)
			buf.append(", ");
		if(value > 1) {
			buf.append(value);
			buf.append(" ");
		}
		if(value > 0)
			buf.append(units);
		if(value > 1)
			buf.append("s");
		return comma || value > 0;
	}

	/** Format the remaining time for an event (minutes) */
	static protected String formatRemaining(long remaining) {
		boolean comma = false;
		long days = remaining / MINUTES_PER_DAY;
		long hours = (remaining % MINUTES_PER_DAY) / 60;
		long minutes = remaining % 60;
		StringBuilder buf = new StringBuilder();

		buf.append(" for the next ");
		comma = appendInterval(buf, "day", days, comma);
		comma = appendInterval(buf, "hour", hours, comma);
		appendInterval(buf, "minute", minutes, comma);

		return buf.toString();
	}

	public String toString() {
		StringBuffer result = new StringBuffer( "since " );
		result.append( outDateFormat.format(startTime));
		if ( endTime != null ) {
			result.append( " until " ).append(outDateFormat.format(endTime));
		} else if ( duration == 2000000 ) {
			result.append( " until further notice" );
		} else if ( duration > 0 ) {
			Date now = new Date();
			long diff = now.getTime() - startTime.getTime();
			long past = diff / 60000;
			long remaining = duration - past;
			result.append(formatRemaining(remaining));
		}
		if(recurrent) {
			StringBuffer dayString = new StringBuffer(" on ");
			if((bDays & WEEKDAYS) == WEEKDAYS) {
				dayString.append(WEEKDAYS_KEY);
				byte weekends = (byte)(bDays & WEEKENDS);
				if(weekends == WEEKENDS)
					dayString = new StringBuffer( " daily");
				else if(SATURDAY.contains(weekends))
					dayString.append(" and ").append(SATURDAY.name);
				else if (SUNDAY.contains(weekends))
					dayString.append(" and ").append(SUNDAY.name);
			} else if((bDays & WEEKENDS) == WEEKENDS) {
				dayString.append(WEEKENDS_KEY);
				dayString.append(getWeekdays(bDays, false));
			} else {
				dayString.append(getWeekdays(bDays, true));
				if(SUNDAY.contains(bDays))
					dayString.append(SUNDAY.name);
				else if(SATURDAY.contains(bDays))
					dayString.append(SATURDAY.name);
			}
			result.append(dayString);
			if(effectiveQualifier != null)
				result.append(" ").append(effectiveQualifier);
			if ( null != scheduleStart){
				result.append(" from ").append(scheduleStart.substring(0,2))
					.append(':').append(scheduleStart.substring(2,4))
					.append(" to ").append(scheduleEnd.substring(0,2))
					.append(':').append(scheduleEnd.substring(2,4));
			}
		}
		return result.toString();
	}

	private Date readDate( Element element ) throws ParseException {
		if(element == null)
			return null;
		String date = element.getTextContent();
		return dateFormat.parse( date );
	}

	/** Is this CarsEventTime valid right now? */
	public boolean isValid() {
		Date now = new Date();
		Date start = startTime;
		Date end = endTime;
		if ( end == null ){ // No end time specified only duration.
			Calendar cal = Calendar.getInstance();
			cal.setTime( startTime );
			cal.add( Calendar.MINUTE, duration );
			end = cal.getTime();
		}
		if(recurrent){
			Calendar cal = Calendar.getInstance();
			int day = cal.get(Calendar.DAY_OF_WEEK);
			byte bValue = (CAL_TO_BYTE.get(day)).byteValue();
			if((bDays & bValue) == bValue) { //If true incident is active today.
				if (null == scheduleStart){
					return true; //There is no scheduled start and end time so
					// just return true;
				}
				Calendar startCal = Calendar.getInstance();
				setTime(startCal, scheduleStart);
				start = startCal.getTime();
				Calendar endCal = Calendar.getInstance();
				setTime(endCal, scheduleEnd);
				end = endCal.getTime();
			} else {
				return false;
			}
		}
		return now.after(start) && now.before(end);
	}

	private void setTime(Calendar cal, String time){
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0,1)));
		cal.set(Calendar.MINUTE, Integer.parseInt(time.substring(2,3)));
	}

	public long getDuration() {
		long result = 0;
		if ( endTime != null ){
			long diff = startTime.getTime() - endTime.getTime();
			result = diff / 60000;
		} else
			result = duration;
		return result;
	}

	public static class CarsDay {
		public int calandarIndex = 0;
		public byte byteValue = 0;
		public String name = null;

		public CarsDay(String name, byte b, int i){
			this.name = name;
			this.byteValue = b;
			this.calandarIndex = i;
		}

		public Byte getByte(){
			return new Byte(byteValue);
		}

		public boolean contains(byte source){
			return ((source & byteValue) == byteValue);
		}
	}
}