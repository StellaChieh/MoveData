package tw.com.cwb.cmt.movedata.util;

public enum TableType {
	MONTH, DAY, HOUR;

	public static TableType getTableType(String fileName) {
		// fileName: CMT_his_DY_20180411182004_1.xml
		// fileName: CMT_DY_20180411182004_2.xml
		if (fileName.contains("MN")) {
			return MONTH;
		} else if (fileName.contains("DY")) {
			return DAY;
		} else {
			return HOUR;
		}
	}
}