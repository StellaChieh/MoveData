package tw.com.cwb.cmt.movedata.service;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;

import tw.com.cwb.cmt.movedata.dao.DyMapper;
import tw.com.cwb.cmt.movedata.dao.HrMapper;
import tw.com.cwb.cmt.movedata.dao.Mapper;
import tw.com.cwb.cmt.movedata.dao.MnMapper;
import tw.com.cwb.cmt.movedata.util.MySessionFactory;
import tw.com.cwb.cmt.movedata.util.TableType;

public class Service {

	protected Mapper readMapper;
	protected Mapper writeMapper;
	protected SqlSession readSession;
	protected SqlSession writeSession;
	private Class<? extends Mapper> mapperClass;
	protected TableType tableType;
	protected static final int BATCH_FLUSH_NUMBER = 4000;
	
	public Service(TableType tableType) throws IOException {
		this.tableType = tableType;
		setMapper(tableType);
		readSession = MySessionFactory.getReadSqlSeesionFactory().openSession(false); // false to turn off auto commit
		writeSession = MySessionFactory.getWriteSqlSessionFactory().openSession(ExecutorType.BATCH, false);
		readMapper = readSession.getMapper(mapperClass);
		writeMapper = writeSession.getMapper(mapperClass);
	}
		
	private void setMapper(TableType tableType){
		switch (tableType) {
			case MONTH:
				this.mapperClass = MnMapper.class;
				break;
			case DAY:
				this.mapperClass = DyMapper.class;
				break;
			case HOUR:
				this.mapperClass = HrMapper.class;
				break;
		}		
	}
	
	Predicate<Map<String, Object>> selectResultNotNull = keyMap -> keyMap!=null && keyMap.size()>=2; // Stno, ObsTime must not be empty 
	
	Consumer<Map<String, Object>> insertOrUpdate = map -> writeMapper.insertOrUpdate(map);
	
	protected void flush() {
		writeMapper.flushBatchedStatements();
	}
	
	protected void commit() {
		writeSession.commit();
	}
	
	protected void closeSession() {
		readSession.close();
		writeSession.close();
	}
}
