package com.topitworks.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Range;

public class DocumentHelper {

  public String getValueInDocumentByKey(String content, String keyName) throws IOException {

    BufferedReader br;
    String line = "";
    InputStream is = new ByteArrayInputStream(content.getBytes());
    String value = "";
    br = new BufferedReader(new InputStreamReader(is));

    while ((line = br.readLine()) != null) {
      value = getValueByColon(line, keyName, value);
    }
    br.close();

    return value;
  }

  public List<List<String>> getListValueInDocumentByMainWordList(String content, List<String> mainWordList)
      throws IOException {

    BufferedReader br;
    String line = "";
    InputStream is = new ByteArrayInputStream(content.getBytes());
//		Map<String, List<String>> hashMap = new HashMap<String, List<String>>();
    List<String> list = new ArrayList<String>();
    List<List<String>> valueList = new ArrayList<List<String>>();
    String trimLine = "";
    br = new BufferedReader(new InputStreamReader(is));

    while ((line = br.readLine()) != null) {
      trimLine = line.replaceAll("\\s", "");
      for (int i = 0; i < mainWordList.size(); i++) {
        if (trimLine.equalsIgnoreCase(mainWordList.get(i))) {
          list = new ArrayList<String>();
          valueList.add(list);
        }
      }
      if (!trimLine.isEmpty()) {
        list.add(line);
      }
    }
    // hashMap.put(mainWordList.get(i), valueList);
    br.close();

    return valueList;
  }

  private String getValueByColon(String line, String keyName, String value) throws UnsupportedEncodingException {
    if (line.contains(":")) {
      String[] words = line.split(":");
      if (words.length > 1 && compareField(words[0], keyName) == true) {
        // value = line.substring(line.indexOf(":")+1);
        value = words[1].trim();
        System.out.println(keyName + " : " + value);
      }
    }
    return value;
  }

  public boolean compareField(String content, String fieldName) throws UnsupportedEncodingException {
    String trimContent = content.replaceAll("\\s", "");
    String trimFieldName = fieldName.replaceAll("\\s", "");
    String lwcContent = trimContent.toLowerCase();
    String lwcFieldname = trimFieldName.toLowerCase();
//		byte[] myBytes = fieldName.getBytes("UTF-8");
//		String utf8FieldName = StringUtils.newStringUtf8(myBytes);
//		System.out.println(StringUtils.newStringUtf8(myBytes));
    String fieldArray[] = lwcFieldname.split(",");
    for (String field : fieldArray) {
      if (lwcContent.contains(field)) {
        return true;
      }
    }
    return false;
  }
}
