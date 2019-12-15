package tw.com.cwb.cmt.movedata.util;

import java.io.IOException;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MySessionFactory {

	private static final String READ_ENV = "read";
	private static final String WRITE_ENV = "write";
	
	private static final String RESOURCE_PATH = "mybatis-config.xml";
	private static SqlSessionFactory sessionFactoryRead = null;
	private static SqlSessionFactory sessionFactoryWrite = null;
	
	private static final Logger tokenRecordLogger = LogManager.getLogger(MyLogger.TOKEN_RECORD.value());
	
	public static SqlSessionFactory getReadSqlSeesionFactory() throws IOException {
		if(sessionFactoryRead == null) {
			tokenRecordLogger.debug("Init reader SqlSessionFactory...");
			sessionFactoryRead = new SqlSessionFactoryBuilder()
									.build(Resources.getResourceAsReader(RESOURCE_PATH), READ_ENV);
			tokenRecordLogger.debug("Init reader SqlSessionFactory finished!");
		} 
		return sessionFactoryRead;
	} 
	
	public static SqlSessionFactory getWriteSqlSessionFactory() throws IOException {
		if(sessionFactoryWrite == null) {
			tokenRecordLogger.debug("Init writer SqlSessionFactory...");
			sessionFactoryWrite = new SqlSessionFactoryBuilder()
									.build(Resources.getResourceAsReader(RESOURCE_PATH), WRITE_ENV);
			tokenRecordLogger.debug("Init writer SqlSessionFactory finished!");
		} 
		return sessionFactoryWrite;
	} 
	
}
