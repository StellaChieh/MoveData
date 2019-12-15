package tw.com.cwb.cmt.movedata.email;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class EmailMessageManager {

	private static String tokenXmlFailFolderPath;
	private static List<EmailMessage> msgs = new ArrayList<>();
	private static EmailMessageManager manager = null;;
	
	public enum ERROR_TYPE{
		SSL("此Token會等待下次排程執行時再處理"),
		COMMUNICATION_FAILURE("此Token會等待下次排程執行時再處理"),
		OTHER("此Token移置到 "+tokenXmlFailFolderPath+" 資料夾");
		private String msg;
		ERROR_TYPE(String s){
			this.msg = s;
		}
		String value() {
			return msg;
		}
	}
	
	
	// singleton
	private EmailMessageManager() {
	}
	
	public static EmailMessageManager getInstance() {
		if(manager == null) {
			manager = new EmailMessageManager();
		}
		return manager;
	}
	
	public void addMsg(EmailMessage msg) {
		msgs.add(msg);
	}
	
	public List<EmailMessage> getMsgs(){
		return msgs;
	}
	
	String produceMsg() {
		StringJoiner errRecords = new StringJoiner(" | ");
		msgs.forEach(e -> {
			errRecords.add(e.getTokenName());
			errRecords.add(String.valueOf(e.getNumOfRecordsInTokenFile()));
			errRecords.add(String.valueOf(e.getNumOfRowsUpdatedInDb()));
			errRecords.add(e.getErrorType().value() + "|\n");
		});
		String errMsg =  new StringBuilder("Hi 管理員:\n\n")
				.append("token筆數與寫入資料庫筆數不相符，請注意以下錯誤資訊：\n")
				.append("| token檔案 | token筆數 | 寫入資料庫的筆數| 附註 |\n| ")
				.append(errRecords.toString())
				.append("\n提醒您:\n可用updatetime功能手動將token再次寫入資料庫中。")
				.toString();
		return errMsg;
	}
	
	public static void setTokenXmlFailFolderPath(String tokenXmlFailFolderPath) {
		EmailMessageManager.tokenXmlFailFolderPath = tokenXmlFailFolderPath;
	}

	public static String getTokenXmlFailFolderPath() {
		return tokenXmlFailFolderPath;
	}
	
	

}
