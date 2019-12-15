package tw.com.cwb.cmt.movedata.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.executor.BatchResult;

public interface Mapper {

	Map<String, Object> selectByPrimaryKey(Map<String, Object> map);
	
	List<Map<String, Object>> selectRecUpdTimeGreaterEqual(Map<String, Object> map);
	
	List<Map<String, Object>> selectRecUpdTimeBetween(Map<String, Object> map);
	
	LocalDateTime selectMaxRecUpdTime(Map<String, Object> map);

	int insertOrUpdate(Map<String, Object> columnValueMap);
	
	int countRecUpdTimeGreaterEqualThan(Map<String, Object> map);
	
	List<Map<String, Object>> selectRecUpdTimeGreaterThanWithPagination(Map<String, Object> map);
	
	
	@Flush
	List<BatchResult> flushBatchedStatements();
	
	
}
