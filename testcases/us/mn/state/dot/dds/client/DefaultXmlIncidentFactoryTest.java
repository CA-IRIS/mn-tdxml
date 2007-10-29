/*
 * Created on Dec 18, 2003
 *
 */
package us.mn.state.dot.dds.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author engs1eri
 * @version 
 */
public class DefaultXmlIncidentFactoryTest extends TestCase {

	/** file name containing test data */
	private static final String FILE = "testdata/incidentTest1.xml";
	
	private static final String FILE2 = "testdata/incidents.xml";
	
	private static final String FILE3 = "testdata/recurrentTimes.xml";
	
	private static final String FILE4 = "testdata/incidentslatest.xml";
	
	/**XML tag for all incident messages. */
	private static final String EVENT_REPORT_MESSAGE = "event-report-message";
	
	private static final String ROADWAY = "I-94";
	
	private Properties properties;
	
	/**
	 * Constructor for XmlIncidentTest.
	 * @param arg0
	 */
	public DefaultXmlIncidentFactoryTest(String arg0) {
		super(arg0);
	}
	
	public void setUp(){
		properties = new Properties();
		properties.setProperty(DefaultXmlIncidentFactory.DATABASE_USERNAME,
			"engs1eri");
		properties.setProperty(DefaultXmlIncidentFactory.DATABASE_PASSWORD,
			"anonymous");
		properties.setProperty(DefaultXmlIncidentFactory.DATABASE_URL,
			"jdbc:postgresql://tms-iris:5432/incidents");
		properties.setProperty(DefaultXmlIncidentFactory.DATABASE_DRIVER,
			"org.postgresql.Driver");
	}
	
	/**
	 * Test creating an incident from a test file.
	 * @throws IncidentException
	 * @throws JDOMException
	 * @throws IOException
	 */
	public void testCreateIncident() throws IncidentException, JDOMException,
			IOException, DdsException{
		SAXBuilder builder = new SAXBuilder(false);
		Document doc = builder.build( FILE );
		Element element = doc.getRootElement().getChild(EVENT_REPORT_MESSAGE);
		XmlIncidentFactory factory = new DefaultXmlIncidentFactory(properties);
		Incident incident = factory.createIncident(element);
		assertNotNull(incident);
		CarsIncident carsIncident = (CarsIncident) incident;
		String roadway = carsIncident.getRoadway();
		assertEquals("Roadways dont match", ROADWAY, roadway);
		System.out.println(carsIncident.toString());
	}
	
	public void testIncidentStream() throws IncidentException, JDOMException,
			IOException, DdsException{
		testFile(FILE2);
		testFile(FILE3);
		testFile(FILE4);
	}
	
	private void testFile(String filename) throws IncidentException, JDOMException,
			IOException, DdsException{
		Logger logger = Hierarchy.getDefaultHierarchy().getLoggerFor("DdsClient - XmlIncidentFilter");
		SAXBuilder builder = new SAXBuilder(false);
		Document doc = builder.build( filename );
		List list = doc.getRootElement().getChildren(EVENT_REPORT_MESSAGE);
		Iterator it = list.iterator();
		XmlIncidentFactory factory = new DefaultXmlIncidentFactory(properties);
		logger.info("testIncidentStream ----------------------------------");
		List incidents = new ArrayList();
		while (it.hasNext()){
			Element element = (Element)it.next();
			incidents.add(factory.createIncident(element));
		}
		XmlIncidentFilter filter = new XmlIncidentFilter(logger);
		incidents = filter.filter(incidents);
		it = incidents.iterator();
		while(it.hasNext()){
			System.out.println(it.next().toString());
		}
	}  
	
	

}
