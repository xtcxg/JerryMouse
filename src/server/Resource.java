package server;

import java.util.Map;

import conf.Property;

public class Resource extends Process{
	public void process(Request req,Response res){
		String url = req.getURL();
		String path = Property.getRsc().getPath();
		logger.info("请求的资源为:"+url);
		Map<String, String> resFile = Property.getRsc().getResfile();
		
		boolean flag = false;
		for(String key : resFile.keySet()){
			if(url.matches(resFile.get(key))){
				flag = true;
				path = path+key+"/"+url;
			}
		}
		logger.info("资源路径为:"+path);
		if(!flag && url.matches(".*\\html")){
			
		}
		res.sendFile(path);
	}
}
