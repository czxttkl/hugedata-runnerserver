package com.czxttkl.selftest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SelfTest {
	public static final ResourceBundle resultBundle = ResourceBundle.getBundle(
			"ResultBundle", Locale.CHINA);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

	public static String getStringValue(String key) {
		String keyValue = null;
		try {
			keyValue = new String(resultBundle.getString(key).getBytes(
					"ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyValue;
	}

}
