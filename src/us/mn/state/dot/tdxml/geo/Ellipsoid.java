/**
 * Origionally from code found at http://symbianos.org/~mkotsbak/P800GPS/P800GPS.html
 * licensced under GPL.
 */
package us.mn.state.dot.tdxml.geo;

final class Ellipsoid {

	int id;
	String ellipsoidName;
	double EquatorialRadius;
	double eccentricitySquared;
	
	public Ellipsoid() {
	}

	Ellipsoid(int Id, String name, int radius, double ecc) {
		id = Id;
		ellipsoidName = name;
		EquatorialRadius = radius;
		eccentricitySquared = ecc;
	}

}
