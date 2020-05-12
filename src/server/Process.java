package server;

import org.apache.log4j.Logger;

import conf.Property;

public abstract class Process implements Runnable{
	public Request req = null;
	public Response res = null;
	public Logger logger = Property.getLog();
	
	public void run(){
		process(req,res);
	}
	public void set(Request req,Response res){
		this.req = req;
		this.res = res;
	}
	abstract public void process(Request req,Response res);

}
