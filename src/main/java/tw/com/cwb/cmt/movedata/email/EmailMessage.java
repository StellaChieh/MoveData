package tw.com.cwb.cmt.movedata.email;

import tw.com.cwb.cmt.movedata.email.EmailMessageManager.ERROR_TYPE;

public class EmailMessage {
	
	private String tokenName;
	private int numOfRecordsInTokenFile;
	private int numOfRowsUpdatedInDb;
	private ERROR_TYPE errorType;
	
	public EmailMessage(String tokenName, int numOfRecordsInTokenFile
					, int numOfRowsUpdatedInDb, ERROR_TYPE errorType) {
		super();
		this.tokenName = tokenName;
		this.numOfRecordsInTokenFile = numOfRecordsInTokenFile;
		this.numOfRowsUpdatedInDb = numOfRowsUpdatedInDb;
		this.errorType = errorType;
	}
	public String getTokenName() {
		return tokenName;
	}
	public int getNumOfRecordsInTokenFile() {
		return numOfRecordsInTokenFile;
	}
	public int getNumOfRowsUpdatedInDb() {
		return numOfRowsUpdatedInDb;
	}
	public ERROR_TYPE getErrorType() {
		return errorType;
	}

}
