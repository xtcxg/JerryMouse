package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import conf.Property;

public class Servers {
	public Logger logger = Property.getLog();
	
	/**
	 * �������˿ں�
	 */
	public static int PORT = 777;
	
	private ThreadPoolExecutor pool = null;
	
	private Map<String,String> ua = new HashMap<String, String>();
	
	/**
	 * ��������ķ�ʽ
	 */
	private static int RTYPE = 6;
	
	
	public void start(){
		ServerSocket server = null;
		pool = new ThreadPoolExecutor(20, 30, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>() );
		
		try{
			server = new ServerSocket(PORT);
			logger.info("********************SERVERS START********************");
			logger.info("��������ַ:127.0.0.1:"+PORT+"/");
//			ProcessBuilder proc = new ProcessBuilder(Property.getChromePath(),"127.0.0.1:"+PORT+"/");
//			proc.start();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		while(true){
			Socket socket = null;
			try{
				socket = server.accept();
				logger.info("**********�µ�����:"+socket.getLocalPort()+"**********");
				Request req = new Request(socket);
				Response res = new Response(socket);
				String url = req.getURL();
				logger.info("���յ�URLΪ:"+url);
				int flag = 0;
				
				if(Property.getRsc().getType().stream().parallel().anyMatch(e -> url.matches(e))){
					logger.info("����̬��Դ����");
					Process proc = new Resource();
					proc.set(req,res);
					pool.execute(proc);
					flag = 1;
				}
				
				if(flag==0 && ((RTYPE & 2) > 0)){
					logger.info("����ӳ������");
					String cname = "action.";
					for(String key : ua.keySet()){
						if(key.equals(url)){
							cname = cname + ua.get(key);
							flag = 2;
						}
					}
					if(flag == 2){
						Process proc = (Process) Class.forName(cname).newInstance();
						proc.set(req,res);
						pool.execute(proc);
					}else{
						logger.info("δ�ҵ�ӳ����Դ:"+url);
					}
					
				}
				
				if(flag == 0 && ((RTYPE & 1) > 0)){
					logger.info("��Ĭ�Ϸ�ʽ��������");
					String cname = url.replace("/", "");
					cname= cname.substring(0,1).toUpperCase()+cname.substring(1,cname.length());
					cname = "action."+cname;
					Process proc = (Process) Class.forName(cname).newInstance();
					proc.set(req,res);
					pool.execute(proc);
				}
				
				flag = 0;
			}catch(IOException e){
				e.printStackTrace();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Map<String,String> getUA(){
		return this.ua;
	}
}
















