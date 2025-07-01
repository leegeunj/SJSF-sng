package com.creative.sng.app.retrofit;

public class MapResponseData {
	private String x;
	private String y;

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "MapResponseData{" +
				"x='" + x + '\'' +
				", y='" + y + '\'' +
				'}';
	}
}
