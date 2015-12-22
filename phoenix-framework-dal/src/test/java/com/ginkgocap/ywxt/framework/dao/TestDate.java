package com.ginkgocap.ywxt.framework.dao;

import java.util.Date;

public class TestDate {
	public static void main(String[] args) {
		Long currentLong = System.currentTimeMillis();
		Date date = new Date(currentLong);
		System.out.println(date.getTime());
	}
}
