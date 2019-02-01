package processor.memorysystem;

import configuration.Configuration;
import generic.Element;
import generic.Event;
import generic.Event.EventType;
import generic.MemoryReadEvent;
import generic.MemoryResponseEvent;
import generic.MemoryWriteEvent;
import generic.Simulator;
import processor.Clock;
import processor.Processor;
import processor.pipeline.MemoryAccess;

public class cache implements Element {
	Processor containingProcessor;
	public int latency ;
	int size ;
	int miss_addr;
	Element miss_requesting;
	int read;
	int write_data;
	cacheLine[] cach;
	public cache(Processor containingProcessor,int l,int s) {
		this.containingProcessor = containingProcessor;
		latency=l;
		size=s;
		cach=new cacheLine[s];
		for(int i=0;i<s;i++) {
			cach[i]=new cacheLine();
		}
	}
	public void handleCacheMiss(int addr ,Element miss_req) {
		Simulator.getEventQueue().addEvent(
				new MemoryReadEvent(
						Clock.getCurrentTime()+Configuration.mainMemoryLatency,
						this,containingProcessor.getMainMemory(),
						addr));
		miss_addr=addr;
		miss_requesting=miss_req;
		
	}
	
	public void handleResponse(int data) {
		String a=Integer.toBinaryString(miss_addr);
		int l=32-a.length();
		for (int i=0;i<l;i++){
			a ="0" +a  ;
		}
		int pos;
		switch((int)(Math.log(size)/Math.log(2)))
		{
		case 0:

			pos=0;
		break;
		default:
			pos = Integer.parseInt(a.substring(32-(int)(Math.log(size)/Math.log(2)),32),2);
			break;
		}	
	
		cach[pos].data = data;
		cach[pos].tag = miss_addr;
		switch(read) {
			case 1:
			Simulator. getEventQueue().addEvent(
					new MemoryResponseEvent (
					Clock.getCurrentTime () ,
					this ,miss_requesting ,data)) ;
			break;
			default:
			handlewrite(miss_addr,write_data,miss_requesting);
		break;}
		
	}
	
	public void handleRead(int addr,Element req) {
		//System.out.println("entered handleread"+addr);
		String a=Integer.toBinaryString(addr);
		int l=32-a.length();
		for (int i=0;i<l;i++){
			a = "0"+a  ;
		}
		int cacheaddr;
		switch((int)(Math.log(size)/Math.log(2))){
//		if(==0) {
		case 0:
			cacheaddr=0;
		break;
//		else {
		default:
			cacheaddr = Integer.parseInt(a.substring(32-(int)(Math.log(size)/Math.log(2)),32),2);
			break;
		}	
		//if(cach[cacheaddr] != null) {
		switch(cach[cacheaddr].tag) {
		case 0:
		default:
		if(cach[cacheaddr].tag == addr) {
				Simulator. getEventQueue().addEvent(
						new MemoryResponseEvent (
						Clock.getCurrentTime () ,
						this ,req ,cach[cacheaddr].data)) ;
				//System.out.println("-------------------------out");
				
		}
		//}
		else{
			read=1;
			handleCacheMiss( addr,req);
		}}
	}

	public void handlewrite(int addr, int value,Element req) {
		String a=Integer.toBinaryString(addr);
		int l=32-a.length();
		for (int i=0;i<l;i++){
			a ="0"+ a  ;
		}
		int cacheaddr;
		switch((int)(Math.log(size)/Math.log(2))) {
		case 1:
		case 2:
		default:
		if((int)(Math.log(size)/Math.log(2))==0) {
			cacheaddr=0;
		}
		else {
			cacheaddr = Integer.parseInt(a.substring(32-(int)(Math.log(size)/Math.log(2)),32),2);
		}	}
		switch(cach[cacheaddr].tag) {
		case 1:
		default:
		
		if(cach[cacheaddr].tag == addr) {
			cach[cacheaddr].data = value; 
			Simulator.getEventQueue().addEvent(
					new MemoryWriteEvent(
							Clock.getCurrentTime()+Configuration.mainMemoryLatency,
							this,containingProcessor.getMainMemory(),
							addr,value));
			((MemoryAccess)req).EX_MA_Latch.setMA_busy(false);
			((MemoryAccess)req).MA_RW_Latch.setRW_enable(true);
			//System.out.println("-------------------------out");
			
		}
		else {
			read=0;
			write_data=value;
			handleCacheMiss(addr,req);
		}}
	}

	@Override
	public void handleEvent(Event e) {
		// TODO Auto-generated method stub
//		System.out.println("hululululululu"+ e.getEventType().getClass().getName());
		if ( e.getEventType ( ) == EventType.MemoryRead )
		{	//System.out.println("-------------------------in");
			MemoryReadEvent event = ( MemoryReadEvent ) e ;
			handleRead(event.getAddressToReadFrom(), event.getRequestingElement());
		}
		else if(e.getEventType ( ) == EventType.MemoryResponse ) {
			MemoryResponseEvent event = ( MemoryResponseEvent ) e ;
			handleResponse(event.getValue());
		}
		else if(e.getEventType ( ) == EventType.MemoryWrite) {
			MemoryWriteEvent event = ( MemoryWriteEvent ) e ;
			handlewrite(event.getAddressToWriteTo(),event.getValue(),event.getRequestingElement());
			
		}
		
		
	}
	


}
