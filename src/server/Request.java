package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.SysexMessage;

import org.apache.log4j.Logger;
import conf.Property;

public class Request {
	private volatile String url;
	
	private Map<String,String> values = new HashMap<String, String>();
	
	private Map<String,String> status = new HashMap<String, String>();
	private Logger logger = Property.getLog();
	
	private DataInputStream din = null;
	
	public Request(Socket socket){
		InputStream in = null;
		
		try{
			in = socket.getInputStream();
			din = new DataInputStream(in);
			disposal(din);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public String getURL(){
		return this.url;
	}
	public String getState(String state){
		return status.get(state);
	}
	public void setValues(String data){
		try{
			String[] arr1 = data.split("&");
			for(String ar:arr1){
				String[] arr2 = ar.split("=");
				values.put(arr2[0],arr2[1]);
			}
		}catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}
	}
	public Map<String,String> getAllValu(){
		return values;
	}
	
	public void disposal(DataInputStream din){
		logger.info("开始处理请求");
		List<Integer> list = new ArrayList<Integer>();
		
		//sum为流中总数据量
		int sum = 0;
		//flag为获取的数据量
		int flag = 1;
		//val为从流中获取的值
		int val = 1;
		
		//先获取一个字节，避免din.available()不为空但读不出数据的情况
		try {
			list.add(din.read());
			sum = din.available();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
//		try{
//			while(sum==0){
//				Thread.sleep(100);
//				sum = din.available();
//			}
//		}catch(IOException | InterruptedException e){
//			e.printStackTrace();
//		}

		while(flag<=sum){
			try{
				val = din.read();
//				System.err.println(val);
				list.add(val);
				flag++;
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		getMessage(list);
	}
	
	private void getMessage(List<Integer> list){
		String[] lines =null;
		byte[] bs = new byte[list.size()];
		
		for (int i = 0; i < bs.length; i++) {
			bs[i] = (byte)(int)  list.get(i);
		}
		String msgs = new String(bs);
		logger.debug("请求的完整内容为:"+msgs);
		lines = msgs.split("\r\n");
		
		/*****************************处理报文头与数据开始*****************************/
		String[] hand = lines[0].split(" ");
		
		//如果为get请求
		if("GET".equalsIgnoreCase(hand[0])){
			//如果get请求中有数据
			if(hand[1].indexOf("?")>=0){
				String[] s = hand[1].split("\\?");
				//处理url
				this.url = s[0];
				logger.info("请求地址为:"+url);
				//处理get请求中的数据
				String data = null;
				try{
					data = URLDecoder.decode(s[1],"UTF-8");
					setValues(data);
					logger.info("请求内容为:"+data);
				} catch(UnsupportedEncodingException e){
					e.printStackTrace();
				}
			}else{
				this.url = hand[1];
				logger.info("请求地址为:"+url);
			}
			lines[0]="";
		}
		
		
		if("POST".equalsIgnoreCase(hand[0])){
			this.url = hand[1];
			logger.info("请求地址为:" + url);
			String data = lines[lines.length-1];
			try{
				data = URLDecoder.decode(data,"UTF-8");
				setValues(data);
			} catch(UnsupportedEncodingException e){
				e.printStackTrace();
			}
			logger.info("请求的内容为:"+data);
			
			//将非状态内容置空
			lines[lines.length-1] = "";
			lines[0]="";
		}
		//非POST/GET请求
		if(!("POST".contentEquals(hand[0]) || "GET".equalsIgnoreCase(hand[0]))){
			
		}
		
		if("".equals(url) || "/".equals(url)){
			url = Property.getRsc().getHomePage();
		}
		if(null == url){
			url = "undefind";
		}
		/*****************************处理报文头与数据结束*****************************/
		
		
		logger.info("开始处理请求状态");
		for(int i = 0;i<lines.length;i++){
			int idx = 0;
			if(! "".equals(lines[i])){
				logger.debug(lines[i]);
				idx = lines[i].indexOf(":");
				status.put(lines[i].substring(0,idx),lines[i].substring(++idx,lines[i].length()).trim());
			}
		}
		logger.info("处理请求状态结束");
	}
	
	/**
	 * 将byte 转为 整型表示的bit[8]
	 */
	public int[] getBit(byte b){
		int[] ins = new int[8];
		for(int i = 0;i<8;i++){
			ins[7-i] = (int)((b>>i) & 0x1);
		}
		return ins;
	}
	
	/**
	 * 将整型bit[] 转为 byte
	 * 例：int[]{0,1,1,1,1,1,1,1} -> (byte)127
	 */
	public byte getByte(int[] ins){
		int n = 0;
		int len = ins.length;
		for(int i=0;i<len;i++){
			if(1==ins[i]){
				n = n+(int)Math.pow(2,len-1-i);
			}
		}
		return (byte) n;
	}
	
	public int getInt(int[] ins){
		int n = 0;
		int len = ins.length;
		for(int i=0;i<len;i++){
			if(1==ins[i]){
				n = n+(int)Math.pow(2,len-1-i);
			}
		}
		return n;
	}
	
	public String getWsData(){
		logger.info("开始处理websocket数据");
		
		List<Integer> list = new ArrayList<Integer>();
		
		int flag = 1;
		int sum = 0;
		
		//先获取一个字节，避免din.available()不为空但读不出数据的情况
		try {
			list.add(din.read());
			sum = din.available();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
//		try{
//			sum = din.available();
//		} catch(IOException e){
//			e.printStackTrace();
//		}
		int val = 1;
		while(flag<=sum){
			try{
				val = din.read();
				list.add(val);
				flag++;
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		byte[] ins = new byte[list.size()];
		for(int i = 0;i<ins.length;i++){
			ins[i] = (byte)(int)list.get(i);
		}
		
		int[] H1 = getBit(ins[0]);
		int FIN = H1[0];
		if(1==FIN){
			//数据完整
		}else{
			//数据不完整
			logger.info("FIN为0");
		}
		int[] H2 = getBit(ins[1]);
		int[] len = new int[7];
		int dataLen = 0;
		if(1==H2[0]){
			logger.info("数据有掩码");
		}else{
			logger.info("数据无掩码");
		}
		System.arraycopy(H2, 1, len, 0, 7);
		dataLen = getByte(len);
		//掩码
		byte[] mask = new byte[4];
		//数据
		byte[] dataOrg = null;
		
		if(dataLen<126){
			logger.info("数据长度为:"+dataLen);
			System.arraycopy(ins, 2, mask, 0, 4);
			dataOrg = new byte[ins.length-6];
			System.arraycopy(ins, 5, dataOrg, 0, dataOrg.length);
			
		}else{
			int lenm = 0;
			if(126==dataLen){
				len = new int[16];
				System.arraycopy(getBit((byte)(int)ins[2]), 0, len, 0, 8);
				System.arraycopy(getBit((byte)(int)ins[3]), 0, len, 8, 8);
				lenm = getInt(len);
				System.arraycopy(ins, 8, dataOrg, 0, dataOrg.length);
				//获取掩码
				System.arraycopy(ins, 4, mask, 0, 4);
				//获取原始数据
				dataOrg = new byte[ins.length-8];
				System.arraycopy(ins, 8, dataOrg, 0, dataOrg.length);
			}
			
			if(127==dataLen){
				len = new int[32];
				System.arraycopy(getBit((byte)(int)ins[2]), 0, len, 0, 8);
				System.arraycopy(getBit((byte)(int)ins[3]), 0, len, 8, 8);
				System.arraycopy(getBit((byte)(int)ins[4]), 0, len, 16, 8);
				System.arraycopy(getBit((byte)(int)ins[5]), 0, len, 24, 8);
				lenm = getInt(len);
				//获取掩码
				System.arraycopy(ins, 6, mask, 0, 4);
				//获取原始数据
				dataOrg = new byte[ins.length-10];
				System.arraycopy(ins, 10, dataOrg, 0, dataOrg.length);
			}
			dataLen = lenm;
		}
		//处理数据
		byte[] data = new byte[dataOrg.length];
		for(int i=0;i<dataOrg.length;i++){
			data[i] = (byte)(dataOrg[i] ^ mask[i%4]);
		}
		logger.info("请求的内容为"+new String(data));
		return new String(data);
	}
}