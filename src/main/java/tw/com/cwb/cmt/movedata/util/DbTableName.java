package tw.com.cwb.cmt.movedata.util;

public enum DbTableName {
	
	TABLE_MN("cwbmn"),
	TABLE_DY("cwbdy"),
	TABLE_HR("cwbhr"),
	TABLE_HIS_MN("his_cwbmn"),
	TABLE_HIS_DY("his_cwbdy"),
	TABLE_HIS_HR("his_cwbhr");	
	String name;
	DbTableName(String s){
		this.name = s;
	}
	public String value() {
		return name;
	}
	
	public static DbTableName getDbTableName(TableType tableType, boolean isThisYear) {
		if (isThisYear) {
			switch (tableType) {
				case MONTH:
					return DbTableName.TABLE_MN;
				case DAY:
					return DbTableName.TABLE_DY;
				default:
					return DbTableName.TABLE_HR;
			}
		} else {
			switch (tableType) {
				case MONTH:
					return DbTableName.TABLE_HIS_MN;
				case DAY:
					return DbTableName.TABLE_HIS_DY;
				default:
					return DbTableName.TABLE_HIS_HR;
			}
		}
	}

}