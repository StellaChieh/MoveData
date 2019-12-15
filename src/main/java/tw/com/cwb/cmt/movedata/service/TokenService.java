package tw.com.cwb.cmt.movedata.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import tw.com.cwb.cmt.movedata.model.Key;
import tw.com.cwb.cmt.movedata.service.TxtProcessor.TxtToken;
import tw.com.cwb.cmt.movedata.util.MyLogger;
import tw.com.cwb.cmt.movedata.util.TableType;

public class TokenService extends Service {

	private String dbTableName;
	private static final Logger logger = LogManager.getLogger(MyLogger.TOKEN_RECORD.value());
	
	public TokenService(TableType tableType) throws IOException {
		super(tableType);
	}
	
	Function<Key, Map<String, Object>> createSelectByPrimaryParamMap = key -> {
		Map<String, Object> map = new HashMap<>();
		map.put("table", dbTableName);
		map.put("Stno", key.getStno());
		map.put("ObsTime", key.getObstime().toString());
		return map;
	};
	Function<Map<String, Object>, Map<String, Object>> selectByPrimaryKey 
		= parameterMap -> readMapper.selectByPrimaryKey(parameterMap);

	public int runTokenService(TxtToken txtToken) throws Exception {
		dbTableName = txtToken.getTableName().value();

		try {
			// 1. select record
			List<Map<String, Object>> selectByPrimaryResultMap = txtToken.getKeys().stream()
																					.map(createSelectByPrimaryParamMap)
																					.map(selectByPrimaryKey)
																					.filter(selectResultNotNull)
																					.collect(Collectors.toList());
			
			// 2. partition list
			List<List<Map<String, Object>>> subLists = Lists.partition(selectByPrimaryResultMap, BATCH_FLUSH_NUMBER);
			
			// 3. batch update
			subLists.forEach(subList -> {
				subList.forEach( map -> {
					map.put("table", dbTableName);
					insertOrUpdate.accept(map);});
				flush();
				logger.info(dbTableName + " | Session flushed.");
			});
			commit();
			logger.info(dbTableName + " | Session committed.");
			return selectByPrimaryResultMap.size();
			
		} finally {
			// 4. close session
			closeSession();
			logger.info(dbTableName + " | Session closed!");
		}
	}
}
