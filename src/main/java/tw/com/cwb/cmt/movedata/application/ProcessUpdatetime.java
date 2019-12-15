package tw.com.cwb.cmt.movedata.application;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tw.com.cwb.cmt.movedata.service.UpdatetimeService;
import tw.com.cwb.cmt.movedata.util.DbTableName;
import tw.com.cwb.cmt.movedata.util.MyLogger;
import tw.com.cwb.cmt.movedata.util.TableType;

public class ProcessUpdatetime {

	private static final Logger logger = LogManager.getLogger(MyLogger.UPDATETIME_RECORD.value());
	private LocalDateTime updatetime;
	private final static String LATEST_UPT_TIME_FILE = "config/latestUptTime.txt";
	
	public void setUpdatetime(LocalDateTime updatetime) {
		this.updatetime = updatetime;
	}

	void run() {
		logger.info("Start to updatetime records' recupdtime after " + updatetime);
		int totalRowsAffected = 0;
		try {
			logger.info("Update " + DbTableName.TABLE_MN.value() + " (and " + DbTableName.TABLE_HIS_MN.value() + ") table.");
			UpdatetimeService mnService = new UpdatetimeService(TableType.MONTH);
			totalRowsAffected += mnService.run(updatetime);
			
			logger.info("Update " + DbTableName.TABLE_DY.value() + " (and " + DbTableName.TABLE_HIS_DY.value() + ") table.");
			UpdatetimeService dyService = new UpdatetimeService(TableType.DAY);
			totalRowsAffected += dyService.run(updatetime);
			
			logger.info("Update " + DbTableName.TABLE_HR.value() + " (and " + DbTableName.TABLE_HIS_HR.value() + ") table.");
			UpdatetimeService hrService = new UpdatetimeService(TableType.HOUR);
			totalRowsAffected += hrService.run(updatetime);
			
			if (totalRowsAffected != 0) {
				if(!Files.exists(Paths.get(LATEST_UPT_TIME_FILE))) {
					Files.createFile(Paths.get(LATEST_UPT_TIME_FILE));
				}
				try (PrintWriter out = new PrintWriter(LATEST_UPT_TIME_FILE)) {
					out.println("Newest RecupdTime: " + UpdatetimeService.getLatestTime());
					logger.info("Newest RecUpdTime: " + UpdatetimeService.getLatestTime());
				} 	
				
				logger.info("Total " + totalRowsAffected + " rows are affected. "
						+ "Updatetime service finished.");		
			} else {
				logger.warn("No record is updated.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
