package us.mn.state.dot.tdxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import us.mn.state.dot.dds.client.Incident;
import us.mn.state.dot.dds.client.IncidentListener;
import us.mn.state.dot.dds.client.XmlIncidentClient;
import us.mn.state.dot.log.TmsLogFactory;

/**
 * @author John3Tim
 */
public class IncidentClientTest implements IncidentListener {

	Logger logger = null;
	int n_incidents;

	public IncidentClientTest(){
		logger = TmsLogFactory.createLogger("IncidentClientTest", null, null);
		Properties props = new Properties();
		File f = new File("client/sample.properties");
		System.out.println("Properties file: " + f.getAbsolutePath());
		try {
			InputStream in = new FileInputStream(f);
			props.load(in);
			XmlIncidentClient client = new XmlIncidentClient(props, logger);
			client.start();
			client.addDdsListener(this);
			while(true)
				Thread.sleep(1000);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void update(boolean finish) {
		if(finish)
			System.out.println("There are now " + n_incidents +
				" incidents.");
		else
			n_incidents = 0;
	}

	public void update(Incident i) {
		n_incidents++;
	}

	public static void main(String[] args) {
		new IncidentClientTest();
	}
}
