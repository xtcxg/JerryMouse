import java.util.Map;

import conf.Property;
import server.Servers;

public class Start {

	public static void main(String[] args) {
		Property.getRsc();
		
		Servers servers = new Servers();
		Map<String,String> ua = servers.getUA();
		
		servers.start();
		
	}

}
