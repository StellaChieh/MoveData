package tw.com.cwb.cmt.movedata.email;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tw.com.cwb.cmt.movedata.util.MyLogger;

public class SendMail {

	private static final Logger tokenRecordLogger = LogManager.getLogger(MyLogger.TOKEN_RECORD.value());
	private static final String FROM = "cmt@cwb.gov.tw";
	private static final String HOST = "ms.mic.cwb";
	private static final String RECEPIENTS_FILE = "config/mailList.txt";
	private static final String TITLE = "197 Token 寫入 195 失敗";
	private static final EmailMessageManager msgManager = EmailMessageManager.getInstance();
	
	public void sendErrorMessage() throws IOException {
		
		final String errMsg = msgManager.produceMsg();
		tokenRecordLogger.info(errMsg);
		
		List<String> userMail = new ArrayList<String>();

		// read recepient list
		if(Files.exists(Paths.get(RECEPIENTS_FILE))) {
			try (Stream<String> lines = Files.lines(Paths.get(RECEPIENTS_FILE))) {
				lines.forEachOrdered(line -> userMail.add(line));
			}
			if (userMail.isEmpty()) {
				tokenRecordLogger.warn("'mialList.txt' is empty. Please add recepients.");
				return;
			}
		} else {
			Files.createFile(Paths.get(RECEPIENTS_FILE));
			tokenRecordLogger.warn("'mialList.txt' is empty. Please add recepients.");
			return;
		}
		
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", HOST);
		Session session = Session.getDefaultInstance(properties);
		// debug use to print message to console
		// session.setDebug(true); 		
		for (int i = 0; i < userMail.size(); i++) {
			String to = (userMail.get(i)).trim();
			try {
				// create a default MimeMessage object.
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(FROM));
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
				message.setSubject(TITLE);
				message.setText(errMsg);
				// Send message
				Transport.send(message);
			} catch (MessagingException mex) {
				tokenRecordLogger.error(mex.getMessage());
			}
		}
	}
}