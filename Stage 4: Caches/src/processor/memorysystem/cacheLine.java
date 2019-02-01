package processor.memorysystem;

public class cacheLine {
	int tag;
	int data;
	public cacheLine() {
		tag=-1;
		data=-1;
	}
	public void setdata(int value) {
		data = value ;
	}
	public int getdata() {
		return data;
	}
	
	public void settag(int addr) {
		tag = addr;
	}
	public int gettag() {
		return tag;
	}
}