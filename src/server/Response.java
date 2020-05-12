package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import conf.Property;

public class Response {
	private OutputStream out;
	private String HEAD = "HTTP/1.1 200 OK\r\n";
	private Map<String,String> status = new HashMap<String,String>();
	private StringBuffer body = new StringBuffer("");
	private Logger logger = Property.getLog();
	
	public Response(Socket socket){
		try{
			this.out = socket.getOutputStream();
		} catch(IOException e){
			e.printStackTrace();
		}
		
		status.put("Server","JerryMouse");
		status.put("Content-Type","text/html;charset=UTF-8");
		status.put("Connection","keep-alive");
	}
	/**
	 * 设置返回报文头<br>
	 * 有则修改，无则新增
	 * @param state 状态
	 * @param value 值
	 */
	public void setState(String state,String value){
		status.put(state,value);
	}
	
	public void changeHead(String head){
		HEAD = head;
	}
	
	public void sendFile(String path){
		try{
			File file = new File(path);
			DataInputStream din = new DataInputStream(new FileInputStream(file));
			byte[] bs = new byte[din.available()];
			din.read(bs);
			send(bs);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void send(byte[] bs){
		DataOutputStream dos =new DataOutputStream(out);
		try{
			//返回报文头
			dos.write(HEAD.getBytes());
			//返回报文状态
			for(Map.Entry<String, String>map : status.entrySet()){
				dos.write((map.getKey()+": "+map.getValue()+"\r\n").getBytes());
			}
			dos.write(("Content-Length: "+bs.length+"\r\n").getBytes());
			
			dos.write("\r\n".getBytes());
			//返回报文内容
			dos.write(bs);
			
			dos.write("\r\n\r\n".getBytes());
			
			dos.flush();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void send(String str){
		send(str.getBytes());
	}
	
	/**
	 * 将传入的数据放入流中，不做处理
	 */
	public void oriSend(byte[] bs){
		DataOutputStream dos = new DataOutputStream(out);
		try{
			dos.write(bs);
			dos.flush();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void oriSned(String str){
		oriSend(str.getBytes());
	}
}
