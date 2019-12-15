package tw.com.cwb.cmt.movedata.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tw.com.cwb.cmt.movedata.application.ProcessToken;
import tw.com.cwb.cmt.movedata.application.ProcessUpdatetime;
import tw.com.cwb.cmt.movedata.util.MyLogger;

/*
 * jvm argument:
 * -Dlog4j.configurationFile=conifg/log4j2.xml 
 * 
 * Token service args: (5 parameters) 
 * [String serviceType, String xmlFolder, String xmlDoneFolder, String xmlFailFolder, String txtFileFolder]
 * ex, ["token", "TokenXml", "TokenXmlDone", "TokenXmlFail", "txtTmp"] 
 * the first parameter is fixed to "token"
 *
 * Updatetime service args: (3 parameters) 
 * [String serviceType, String date, String hourMin] 
 * ex, ["updatetime", "2018-01-01", "00:00"] 
 * the first parameter is fixed to "updatetime"
 */

public class Main {
	
	private static final String TYPE_TOKEN = "token";
	private static final String TYPE_UPDATETIME = "updatetime";
	private static final Logger tokenLogger = LogManager.getLogger(MyLogger.TOKEN_RECORD.value());
	private static final Logger updatetimeLogger = LogManager.getLogger(MyLogger.UPDATETIME_RECORD.value());
	
	public static void main(String[] args) throws Exception {
		
		tokenLogger.info("Java program starts!");
		
		String serviceType = args[0];
		
		// token
		if (serviceType.equalsIgnoreCase(TYPE_TOKEN)) {
			String tokenXmlFolderPath = args[1];
			String tokenXmlDoneFolderPath = args[2];
			String tokenXmlFailFolderPath = args[3];
			String txtFolderPath = args[4];
			
			ProcessToken token = new ProcessToken();
			token.setXmlFolderPath(tokenXmlFolderPath, tokenXmlDoneFolderPath, tokenXmlFailFolderPath);
			token.setTxtFolderPath(txtFolderPath);
			token.run();
		
		// updatetime
		} else if (serviceType.equalsIgnoreCase(TYPE_UPDATETIME)) {
			String date = args[1];
			String time = args[2];
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			LocalDateTime updatetime = LocalDateTime.parse(date + " " + time, formatter);
			if(updatetime.isAfter(LocalDateTime.now())) {
				updatetimeLogger.warn("updatetime is after now. Please modify your input updatetime.");
				return;
			}
			ProcessUpdatetime pUp = new ProcessUpdatetime();
			pUp.setUpdatetime(updatetime);
			pUp.run();
			
		} else {
			throw new Exception("args[0] service type is invalid" + ", please check your spell "
					+ ", it should be 'token' or 'updatetime'");
		}
	}

}
