package tw.com.cwb.cmt.movedata.util;

public enum MyLogger {

	TOKEN_RECORD("token_record"),
	UPDATETIME_RECORD("updatetime_record");
	
	private String name;
	
	MyLogger(String name){
		this.name = name;
	}
	
	public String value() {
		return name;
	}
}
