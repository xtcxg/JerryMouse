package conf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.*;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Property {
	private static Property prop = null;
	private static Logger logger = null;
	private static String  chromePath = null;
	private static Rsc rsc = null;
	private static String fpath = "./src/conf/conf.xml";
	private static String lpath = "./src/conf/log4j.xml";
	
	private Property(){}
	
	public static Property getProp(){
		if(prop == null){
			prop = new Property();
			if(logger == null) getLog();
			if(rsc != null) return prop;
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			try{
				builder = factory.newDocumentBuilder();
				File file = new File(fpath);
				Document doc = builder.parse(file);
				Element root = doc.getDocumentElement();
				NodeList nodes = root.getChildNodes();
				
				for(int i = 0;i<nodes.getLength();i++){
					Node node = nodes.item(i);
					if(node instanceof Element){
						logger.debug("����Ľڵ�Ϊ:"+node.getNodeName());
						if("Resource".equals(node.getNodeName())){
							logger.debug("����Resource�ڵ����ݿ�ʼ");
							rsc = prop.new Rsc(node);
							logger.debug("����Resource�ڵ����");
						}
						if("ChromePath".equals(node.getNodeName())){
							logger.info("����ChromePath�ڵ㿪ʼ");
							Text textnode = (Text) node.getFirstChild();
							chromePath = textnode.getData().trim();
							logger.debug("chromeλ��Ϊ:"+chromePath);
							logger.debug("����ChromePath�ڵ����");
						}
					}
				}
			} catch(ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
		}
		return prop;
		
	}
	
	/**
	 * ��ȡchrome.exe��·��
	 * @return chromePath
	 */
	public static String getChromePath(){
		if(chromePath == null){
			Property.getProp();
		}
		
		return chromePath;
	}
	
	public static Logger getLog(){
		if(logger==null){
			logger = Logger.getLogger(Property.class);
			DOMConfigurator.configure(lpath);
			logger.debug("logger ��ʼ�����");
		}
		return logger;
	}
	
	public static Rsc getRsc(){
		if(rsc == null){
			Property.getProp();
		}
		return rsc;
	}
	
	
	public class Rsc{
		private List<String> types = new ArrayList<String>();
		private String path = null;
		private String homePage = null;
		private Map<String,String> resFile = new HashMap<String, String>();
		
		public Rsc(Node res){
			NodeList nodes = res.getChildNodes();
			for(int i = 0;i<nodes.getLength();i++){
				Node node = nodes.item(i);
				if("type".equals(node.getNodeName())){
					logger.debug("��ȡ��Ҫ����ľ�̬��Դ����");
					setType(node);
				}
				if("path".equals(node.getNodeName())){
					logger.debug("��ȡ��̬��Դ���·��");
					Text textnode = (Text) node.getFirstChild();
					path = textnode.getData().trim();
					logger.debug("��ȡ��̬��Դ���·��Ϊ:"+path);
				}
				if("homePage".equals(node.getNodeName())){
					logger.debug("��ȡ��ҳ");
					Text textnode = (Text) node.getFirstChild();
					homePage = textnode.getData().trim();
					logger.info("��ҳΪ:"+homePage);
				}
			}
		}
		
		private void setType(Node ts){
			NodeList nodes = ts.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if(node instanceof Element){
					String value = ((Element)node).getAttribute("file");
					Text textnode = (Text) node.getFirstChild();
					String str = textnode.getData().trim();
					logger.debug("����ľ�̬��Դ����Ϊ:"+str+" | ��Դ·��Ϊ:"+value);
					resFile.put(value,str);
					types.add(str);
				}
				
			}
		}
		
		public List<String> getType(){
			return types;
		}
		
		public String getPath(){
			return path;
		}
		
		public String getHomePage(){
			return homePage;
		}
		public Map<String,String> getResfile(){
			return resFile;
		}
	}
	public static void main(String[] args) {
		Property prop = Property.getProp();
		System.out.println(Property.getRsc().getPath());
	}
}
