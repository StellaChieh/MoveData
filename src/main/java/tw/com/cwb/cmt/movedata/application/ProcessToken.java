package tw.com.cwb.cmt.movedata.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import tw.com.cwb.cmt.movedata.email.EmailMessage;
import tw.com.cwb.cmt.movedata.email.EmailMessageManager;
import tw.com.cwb.cmt.movedata.email.EmailMessageManager.ERROR_TYPE;
import tw.com.cwb.cmt.movedata.email.SendMail;
import tw.com.cwb.cmt.movedata.model.Key;
import tw.com.cwb.cmt.movedata.service.TokenService;
import tw.com.cwb.cmt.movedata.service.TxtProcessor;
import tw.com.cwb.cmt.movedata.service.TxtProcessor.TxtToken;
import tw.com.cwb.cmt.movedata.util.MyLogger;

public class ProcessToken {

	private static final Logger logger = LogManager.getLogger(MyLogger.TOKEN_RECORD.value());
	
	private String tokenXmlFolderPath;
	private String tokenXmlDoneFolderPath;
	private String tokenXmlFailFolderPath;
	private String txtFolderPath;

	private int totalRowsAffected = 0;
	
	private boolean sendEmail = false;
	private final static EmailMessageManager msgManager = EmailMessageManager.getInstance();
	
	void setXmlFolderPath(String source, String done, String fail) {
		this.tokenXmlFolderPath = source;
		this.tokenXmlDoneFolderPath = done;
		this.tokenXmlFailFolderPath = fail;
	}
	
	void setTxtFolderPath(String txtFolderPath) {
		this.txtFolderPath = txtFolderPath;
	}
	
	void run() {
		
		logger.info("Reading txt files...");
		
		if(Paths.get(txtFolderPath).toFile().list().length==0) {
			logger.warn("Empty txt folder.");
			return;
		}
				
		// take all txt file in txt folder
		TxtProcessor txtProcessor = new TxtProcessor(txtFolderPath);
		txtProcessor.processTxt();
		List<TxtToken> txtFiles = txtProcessor.getTxtList();
		
		for (TxtToken txtToken : txtFiles) {
			logger.info("Start to process " + txtToken.getFilename());
			int numOfRecPerToken = 0;
			int rowsAffectedPerToken = 0;
			boolean sslExceptionFlag = false;
			boolean communicationExceptionFlag = false;
			
			try {
				// read number of records
				numOfRecPerToken = txtToken.getCount();

				// read tokenTxt file and get key
				List<Key> keyList = txtToken.getKeys();

				if (!keyList.isEmpty()) {
					// init SqlSessionFactory and open new sqlSession
					TokenService service = new TokenService(txtToken.getTableType());
					// run service
					rowsAffectedPerToken = service.runTokenService(txtToken);
					totalRowsAffected += rowsAffectedPerToken;
				} else {
					logger.warn(txtToken.getFilename() + " file is empty.");
				}
				
			} catch (CommunicationsException ce) {	
				logger.error(ce.getMessage());
				rowsAffectedPerToken = 0;
				communicationExceptionFlag = true;		
				
			} catch (SQLServerException sslException) {
				// when SQLServerException happens, we remain the token in the original folder,
				// the token will be processed next time when the app is executing.
				logger.error(sslException.getMessage());
				rowsAffectedPerToken = 0;
				sslExceptionFlag = true;
				
			} catch (Exception e) {
				logger.error(e.getMessage());
				rowsAffectedPerToken = 0;
				
			} finally {
				String tokenName = txtToken.getFilename().substring(0, txtToken.getFilename().length()-4) + ".xml";
				// send alert email and move processed xml 
				if (numOfRecPerToken == 0 || rowsAffectedPerToken != numOfRecPerToken) { // not success
					if(EmailMessageManager.getTokenXmlFailFolderPath() == null) {
						EmailMessageManager.setTokenXmlFailFolderPath(tokenXmlFailFolderPath);
					}
					EmailMessage msg;
					if (communicationExceptionFlag) {
						msg= new EmailMessage(tokenName, numOfRecPerToken, rowsAffectedPerToken, ERROR_TYPE.COMMUNICATION_FAILURE);
					} else if(sslExceptionFlag) {
						msg= new EmailMessage(tokenName, numOfRecPerToken, rowsAffectedPerToken, ERROR_TYPE.SSL);
					} else {
						msg= new EmailMessage(tokenName, numOfRecPerToken, rowsAffectedPerToken, ERROR_TYPE.OTHER);
						moveToken(tokenName, tokenXmlFolderPath, tokenXmlFailFolderPath);
						logger.info(tokenName + " move to " + tokenXmlFailFolderPath + " folder.");
					}
					msgManager.addMsg(msg);
					sendEmail = true;
				} else {
					moveToken(tokenName, tokenXmlFolderPath, tokenXmlDoneFolderPath);
					logger.info(tokenName + " move to " + tokenXmlDoneFolderPath + " folder.");
				}
			}
		}

		// sending alert email
		if (sendEmail) {
			try {
				new SendMail().sendErrorMessage();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		logger.info("Total " + totalRowsAffected + " row(s) are affected.");
	}
	
	private static void moveToken(String tokenName, String sourceFolder, String targetFolder) {
		try {
			Files.move(Paths.get(sourceFolder, tokenName), Paths.get(targetFolder, tokenName));
		} catch (IOException e) {
			logger.error("Can not move " + tokenName + " to " + targetFolder + ".", e);
		}	
	}

}
