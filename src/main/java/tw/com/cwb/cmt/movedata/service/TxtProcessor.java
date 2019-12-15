package tw.com.cwb.cmt.movedata.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tw.com.cwb.cmt.movedata.model.Key;
import tw.com.cwb.cmt.movedata.util.DbTableName;
import tw.com.cwb.cmt.movedata.util.MyLogger;
import tw.com.cwb.cmt.movedata.util.TableType;

public class TxtProcessor {

	private static final Logger tokenRecordLogger = LogManager.getLogger(MyLogger.TOKEN_RECORD.value());
	
	private String txtFolder;
	private List<TxtToken> txtList = new ArrayList<>();

	public TxtProcessor(String txtFolder) {
		this.txtFolder = txtFolder;
	}

	public void processTxt() {
		List<String> txtFiles = getTxtFiles(txtFolder);
		if(!txtFiles.isEmpty()) {
			txtFiles.stream()
					.forEach( path -> {
						TxtToken txt = new TxtToken();
						txt.setFilename(Paths.get(path).getFileName().toString());;
						txt.setCount(readNumOfRecPerTxt(path));
						txt.setKeys(readTxtFile(path));
						txt.setTableType(TableType.getTableType(txt.getFilename()));
						txt.setTableName(DbTableName.getDbTableName(txt.getTableType()
								, isThisYear(txt.getKeys().stream().findAny().get().getObstime())));
						txtList.add(txt);});
		}
	}
	
	public List<TxtToken> getTxtList() {
		return txtList;
	}

	private List<Key> readTxtFile(String pathString) {
		
		try (Stream<String> stream = Files.lines(Paths.get(pathString))) {
			return stream.skip(1) // skip first line
						.map(line -> {
							String stno = line.split(";")[0].trim();
							String obsTime = line.split(";")[1].trim();
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
							return new Key(stno, LocalDateTime.parse(obsTime, formatter));})
						.collect(Collectors.toList());
		} catch (IOException e) {
			tokenRecordLogger.error("Reading " + pathString + " has IOException.");
			return Collections.emptyList();
		} 
	}

	private List<String> getTxtFiles(String txtFolderPath) {
		try {
			return Files.walk(Paths.get(txtFolderPath.trim()))
						.filter(Files::isRegularFile)
						.map(Path::toString)
						.collect(Collectors.toList());
		} catch (IOException ioException) {
			tokenRecordLogger.error("Reading txt folder has IOException.", ioException);
			return Collections.emptyList();
		}
	}


	private int readNumOfRecPerTxt(String txtFilePath){
		try (BufferedReader br = new BufferedReader(new FileReader(txtFilePath))) {
			return Integer.parseInt(br.readLine().split(";")[1].trim());
		} catch (NumberFormatException e) {
			tokenRecordLogger.error("Reading " + txtFilePath + " has NumberFormatException.");
			return -1;
		} catch (IOException e) {
			tokenRecordLogger.error("Reading " + txtFilePath + " has IOException.");
			return -1;
		}
	}
	
	protected boolean isThisYear(LocalDateTime time) {
		return time.getYear() == LocalDate.now().getYear();
	}



	public class TxtToken {
		
		private String filename;
		private List<Key> keys;
		private int count;
		private TableType tableType;
		private DbTableName tableName;
		
		public List<Key> getKeys() {
			return keys;
		}
		public void setKeys(List<Key> keys) {
			this.keys = keys;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public DbTableName getTableName() {
			return tableName;
		}
		public void setTableName(DbTableName tableName) {
			this.tableName = tableName;
		}
		public String getFilename() {
			return filename;
		}
		public void setFilename(String filename) {
			this.filename = filename;
		}
		public TableType getTableType() {
			return tableType;
		}
		public void setTableType(TableType tableType) {
			this.tableType = tableType;
		}
		
	}
	
}
