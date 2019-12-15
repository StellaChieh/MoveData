package tw.com.cwb.cmt.movedata.model;

import java.time.LocalDateTime;

public class Key {

	
	private String stno;

	private LocalDateTime obstime;

	public Key(String stno, LocalDateTime obstime) {
        this.stno = stno;
        this.obstime = obstime;
    }

	public Key() {
        super();
    }

	public String getStno() {
		return stno;
	}

	public void setStno(String stno) {
		this.stno = stno == null ? null : stno.trim();
	}

	public LocalDateTime getObstime() {
		return obstime;
	}

	public void setObstime(LocalDateTime obstime) {
		this.obstime = obstime;
	}
	
	

}
