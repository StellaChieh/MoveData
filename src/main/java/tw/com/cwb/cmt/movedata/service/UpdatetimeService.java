package tw.com.cwb.cmt.movedata.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tw.com.cwb.cmt.movedata.util.DbTableName;
import tw.com.cwb.cmt.movedata.util.MyLogger;
import tw.com.cwb.cmt.movedata.util.TableType;

public class UpdatetimeService extends Service {

	private static final Logger logger = LogManager.getLogger(MyLogger.UPDATETIME_RECORD.value());
	private static LocalDateTime latestTime = LocalDateTime.MAX;
	
	public UpdatetimeService(TableType tableType) throws IOException {
		super(tableType);
	}

	private int updateRecUpdTimeOffset(String table, LocalDateTime beginTime, int offset) {

		// 1. create sql parameter
		Map<String, Object> selectParam = new HashMap<>();
		selectParam.put("table", table);
		selectParam.put("RecUpdTime", beginTime.toString());
		selectParam.put("offset", offset);
		selectParam.put("range", BATCH_FLUSH_NUMBER);

		// 2. select records with pagination
		logger.info(table + " | Select records from source db...");
		List<Map<String, Object>> recordsWaitToWrite = readMapper.selectRecUpdTimeGreaterThanWithPagination(selectParam);

		// 3. batch update
		logger.info(table + " | Session preparing statements...");
		if (recordsWaitToWrite != null && !recordsWaitToWrite.isEmpty()) {
			recordsWaitToWrite.forEach(map -> {
				map.put("table", table);
				insertOrUpdate.accept(map);
			});
			flush();
			logger.info(table + " | Session flushed.");
			
			commit();
			logger.info(table + " | Session committed.");
		}
		
		return ( recordsWaitToWrite == null || recordsWaitToWrite.isEmpty())
				? Integer.MIN_VALUE : recordsWaitToWrite.size();
	}
		
	private List<Integer> getOffsetList(int totalCount){
		return IntStream.range(0, totalCount/BATCH_FLUSH_NUMBER+1)
						.map(i->i*BATCH_FLUSH_NUMBER)
						.boxed()
						.collect(Collectors.toList());
	}
	
	private int countRecUpdTimeGreaterEqualThan(String dbTableName, LocalDateTime beginTime) {
		Map<String, Object> countParam = new HashMap<>();
        countParam.put("table", dbTableName);
        countParam.put("RecUpdTime", beginTime);
        return readMapper.countRecUpdTimeGreaterEqualThan(countParam);
	}
	
	
	public int run(LocalDateTime updateTime) {
		boolean processThisYearTable = true;
		int numOfUpdRec = 0;
		
		// 1. query this year's record and save
		String dbTableName = DbTableName.getDbTableName(tableType, true).value();
		LocalDateTime beginTime = (updateTime.getYear() == LocalDateTime.now().getYear()) ?
				  updateTime : LocalDateTime.of(LocalDate.now().getYear()-1, 12, 31, 23, 59, 59); 
        int recordTotalCount = countRecUpdTimeGreaterEqualThan(dbTableName, beginTime);
        List<Integer> offsetList = getOffsetList(recordTotalCount);
        logger.info(dbTableName + " | Record total count: " + recordTotalCount);
        logger.info(dbTableName + " | Offsets: " + offsetList);
        
		try {	
			offsetList.stream()
						.forEach(offset -> updateRecUpdTimeOffset(dbTableName, beginTime, offset));			
			LocalDateTime tmpTime = writeMapper.selectMaxRecUpdTime(Collections.singletonMap("table", dbTableName));
			logger.info(dbTableName + " | Latest RecUpdTime is " + tmpTime.toString());
			latestTime = latestTime.isBefore(tmpTime) ? latestTime : tmpTime;
			numOfUpdRec += recordTotalCount;
			
			
			// 2. query history record and save
			if (updateTime.getYear() < LocalDateTime.now().getYear()) {
				processThisYearTable = false;
				String hisDbTableName = DbTableName.getDbTableName(tableType, false).value();
				LocalDateTime hisBeginTime = updateTime;
				recordTotalCount = countRecUpdTimeGreaterEqualThan(hisDbTableName, hisBeginTime);
		        offsetList = getOffsetList(recordTotalCount);
		        logger.info(hisDbTableName + " | Record count: " + recordTotalCount);
		        logger.info(hisDbTableName + " | Offsets: " + offsetList);
		        offsetList.stream()
							.forEach(offset -> updateRecUpdTimeOffset(hisDbTableName, hisBeginTime, offset));
				tmpTime = writeMapper.selectMaxRecUpdTime(Collections.singletonMap("table", hisDbTableName));
				logger.info(hisDbTableName + " | Latest RecUpdTime is " + tmpTime.toString());
				latestTime = latestTime.isBefore(tmpTime) ? latestTime : tmpTime;
				numOfUpdRec += recordTotalCount;
			}
		} catch (Exception e) {
			numOfUpdRec = 0;
			String tableName = processThisYearTable ? dbTableName : "his_" + dbTableName; 
			logger.error( tableName + " | " + e.getMessage());
		} finally {
			// 3. close session
			closeSession();
		}		
		return numOfUpdRec;
	}

	public static LocalDateTime getLatestTime() {
		return latestTime;
	}
	
}
