/**
 * Origionally from code found at http://symbianos.org/~mkotsbak/P800GPS/P800GPS.html
 * licensced under GPL.
 */
package us.mn.state.dot.dds.geo;

class LatLon {

	private double lat;
	private double lon;
	
	public LatLon(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public LatLon(byte latDeg, float latMin, short longDeg, float longMin) {
		this.lat = latDeg + (latMin / 60.0);
		this.lon = longDeg + (longMin / 60.0);
	}

	public double getLat() {
		return lat;
	}
	public byte getLatDeg() {
		return (byte) lat;
	}
	public float getLatMin() {
		return (float) ((lat - getLatDeg()) * 60.0);
	}

	public double getLong() {
		return lon;
	}
	public short getLongDeg() {
		return (short) lon;
	}
	public float getLongMin() {
		return (float) ((lon - getLongDeg()) * 60.0);
	}

	public String toString() {
		return "Latitude: "
			+ getLatDeg()
			+ " deg "
			+ getLatMin()
			+ "'"
			+ ", Longitude: "
			+ getLongDeg()
			+ " deg "
			+ getLongMin()
			+ "'";
	}

	//    private byte latDeg; // north is positive
	//private float latMin;
	//    private short latMinDec;

	//private short longDeg; // east is positive
	//private float longMin;
	//    private short longMinDec;
}
