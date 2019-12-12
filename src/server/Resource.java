package server;

import java.util.Map;

import conf.Property;

public class Resource extends Process{
	public void process(Request req,Response res){
		String url = req.getURL();
		String path = Property.getRsc().getPath();
		logger.info("�������ԴΪ:"+url);
		Map<String, String> resFile = Property.getRsc().getResfile();
		
		boolean flag = false;
		for(String key : resFile.keySet()){
			if(url.matches(resFile.get(key))){
				flag = true;
				path = path+key+"/"+url;
			}
		}
		logger.info("��Դ·��Ϊ:"+path);
		if(!flag && url.matches(".*\\html")){
			
		}
		res.sendFile(path);
	}
}
