/**
 * Origionally from code found at http://symbianos.org/~mkotsbak/P800GPS/P800GPS.html
 * licensced under GPL.
 */
package us.mn.state.dot.tdxml.geo;

public final class UTM {
	
	private double northing;
	private double easting;
	String zone;

	public UTM(double northing, double easting, String zone) {
		this.northing = northing;
		this.easting = easting;
		this.zone = zone;
	}

	public double getNorthing() {
		return northing;
	}

	public double getEasting() {
		return easting;
	}

	public String getZone() {
		return zone;
	}
}
