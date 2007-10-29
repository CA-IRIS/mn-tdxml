/*
 * Created on Apr 5, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package us.mn.state.dot.tdxml;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * @author engs1eri
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TimeZoneTest extends TestCase {

	/**
	 * Constructor for TimeZoneTest.
	 * @param arg0
	 */
	public TimeZoneTest(String arg0) {
		super(arg0);
	}
	
	public void testTimeZone(){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone( "GMT-5"), Locale.getDefault());
		int year = 2004;
		int month = 2;
		int date = 5;
		int hour = 8;
		int minute = 0;
		int second = 0;
		//String zoneString = source.substring( index + 20, index + 26);
		//TimeZone zone = TimeZone.getTimeZone( "GMT-5");
		//System.out.println(zone.toString());
		calendar.set( year, month, date, hour, minute, second );
		//calendar.setTimeZone( zone );
		//System.out.println(TimeZone.getDefault().getRawOffset());
		//System.out.println(TimeZone.getDefault().useDaylightTime());
		//calendar.setTimeZone(TimeZone.getDefault());
		//zone = TimeZone.getDefault();
		//System.out.println(zone.toString());
		System.out.println(calendar.getTime().toLocaleString());
	}
}
