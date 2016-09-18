package com.topitworks.parser.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Range;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.topitworks.common.CandidateDocument;
import com.topitworks.common.DocumentHelper;
import com.topitworks.parser.Parser;

public class ResumeParser implements Parser {

	/**
	 * The main purpose of this method is to parse unstructured data (resume
	 * file) to structured data (JSON). This method accepts the resume file as
	 * java.io.File object, parse them and return a JSON string which represents
	 * resume information
	 *
	 * @param resume
	 *            resume file in DOC, DOCX or PDF extension
	 * @return JSON string represents resume information. Refer the suggested
	 *         structure here
	 * @throws IOException
	 */
	DocumentHelper documentHelper = new DocumentHelper();

	@SuppressWarnings("deprecation")
	public String parse(File resume, String language_properties) throws IOException {
		String strContent = null;
		FileInputStream fis = new FileInputStream(resume.getAbsolutePath());
		HWPFDocument document = new HWPFDocument(fis);
		WordExtractor extractor = new WordExtractor(document);

		String fileData = extractor.getText();
		extractor.close();
		strContent = fileData.replaceAll("[\\t\\r]", "");
		// System.out.println(strContent);

		String keyName = "";
		File file = new File("src/main/resources/candidate.json");
		String jsonData = FileUtils.readFileToString(file);

		Properties prop = new Properties();
		prop.load(getClass().getClassLoader().getResourceAsStream(language_properties));

		// InputStream inputStream =
		// getClass().getClassLoader().getResourceAsStream("candidatedocument_en.properties");
		Properties propJson = new Properties();
		propJson.load(getClass().getClassLoader().getResourceAsStream("jsonformat.properties"));

		ObjectMapper mapper = new ObjectMapper();		
		JsonNode rootNode = mapper.readTree(new StringReader(jsonData));
				
		JsonNode candidateNode = rootNode.get(CandidateDocument.CANDIDATE);
		JsonNode personalNode = rootNode.get(CandidateDocument.CANDIDATE).get(CandidateDocument.PERSONAL);
		JsonNode employmentNode = rootNode.get(CandidateDocument.CANDIDATE).get(CandidateDocument.EMPLOYMENT);
		JsonNode educationNode = rootNode.get(CandidateDocument.CANDIDATE).get(CandidateDocument.EDUCATION);
		
		JsonNode objectiveNode = personalNode.get(CandidateDocument.OBJECTIVE);
		JsonNode skillsNode = personalNode.get(CandidateDocument.SKILLS);
		JsonNode interestNode = personalNode.get(CandidateDocument.INTEREST);
		
		ObjectNode objPersonalNode = (ObjectNode) personalNode;

		// Update Personal information
		String value = "";
		for (String key : prop.stringPropertyNames()) {
			keyName = prop.getProperty(key);
			value = documentHelper.getValueInDocumentByKey(strContent, keyName);
			((ObjectNode) personalNode).put(propJson.getProperty(key), value);
//			objPersonalNode.put(propJson.getProperty(key), value);
		}

		List<String> mainWordList = getListMainWordFromWordResume(document, prop, propJson);
		List<List<String>> listValue = documentHelper.getListValueInDocumentByMainWordList(strContent, mainWordList);

		// Update Personal Objective, Skills, Interest	
		for (List<String> list : listValue) {
			switch (list.get(0)) {
				case CandidateDocument.OBJECTIVE:
					list.remove(0);
					for (String string : list) {
						((ArrayNode) objectiveNode).add(string);
					}
					break;
				case CandidateDocument.SKILLS:
					list.remove(0);
					for (String string : list) {
						((ArrayNode) skillsNode).add(string);
					}
					break;
				case CandidateDocument.INTEREST:
					list.remove(0);
					for (String string : list) {
						((ArrayNode) interestNode).add(string);
					}
					break;
				default:
					break;
			}
		}
		((ObjectNode) personalNode).put(CandidateDocument.OBJECTIVE, objectiveNode);
		((ObjectNode) personalNode).put(CandidateDocument.SKILLS, skillsNode);
		((ObjectNode) personalNode).put(CandidateDocument.INTEREST, interestNode);
		
		//Update Employment
		JsonNode emplNode = employmentNode.get(0);
		ObjectNode jNode = null;	
		ArrayNode descrtNode = null;
		ArrayNode arrEmplNode = mapper.createArrayNode();
		
		for (List<String> list : listValue) {
			if (list.get(0).equals(CandidateDocument.EXPERIENCES)) {
				list.remove(0);
				for (String string : list) {
					if (isValidDate(string, "MM/yyyy")) {		
						jNode.put(CandidateDocument.TIME, string);		
					} else if (!Pattern.matches(".*[a-zA-Z]+.*", String.valueOf(string.charAt(0)))) {
						System.out.println(String.valueOf(string.charAt(0)));
						descrtNode.add(string);
						jNode.put(CandidateDocument.DESCRIPTION, descrtNode);
					} else {
						jNode = mapper.createObjectNode();
						descrtNode = mapper.createArrayNode();
						jNode.putAll(((ObjectNode) emplNode));						
						jNode.put(CandidateDocument.EMPLOYER, string);
						arrEmplNode.add(jNode);
					}
				}
			}			
		}	
		if (arrEmplNode.size() > 0) {
			((ObjectNode) candidateNode).put(CandidateDocument.EMPLOYMENT, arrEmplNode);
		}		
		
		//Update Education
		JsonNode eduNode = educationNode.get(0);
		ObjectNode tempNode = null;	
		ArrayNode eduDescNode = null;
		ArrayNode arrEduNode = mapper.createArrayNode();
		
		for (List<String> list : listValue) {
			if (list.get(0).equals(CandidateDocument.EDUCATION)) {
				list.remove(0);
				for (String string : list) {
					if (isValidDate(string, "yyyy")) {	
						tempNode = mapper.createObjectNode();
						eduDescNode = mapper.createArrayNode();
						tempNode.putAll(((ObjectNode) eduNode));
						tempNode.put(CandidateDocument.TIME, string);
						arrEduNode.add(tempNode);
					} else if (!Pattern.matches(".*[a-zA-Z]+.*", String.valueOf(string.charAt(0)))) {
						System.out.println(String.valueOf(string.charAt(0)));
						eduDescNode.add(string);
						tempNode.put(CandidateDocument.DESCRIPTION, eduDescNode);
					} else if (documentHelper.compareField(string, prop.getProperty("education.gpa")) == true) {												
						tempNode.put(CandidateDocument.GPA, string);						
					} else if (documentHelper.compareField(string, prop.getProperty("education.degree")) == true) {
						tempNode.put(CandidateDocument.DEGREE, string);
					}
				}
			}			
		}
		if (arrEduNode.size() > 0) {
			((ObjectNode) candidateNode).put(CandidateDocument.EDUCATION, arrEduNode);
		}			
		
		jsonData = new ObjectMapper().writeValueAsString(rootNode);
		System.out.println(jsonData);

		File fileUpdate = new File("src/test/resources/candidate_updated.json");
		FileUtils.write(fileUpdate, jsonData);

		return jsonData;
	}

	public List<String> getListMainWordFromWordResume(HWPFDocument document, Properties prop, Properties propJson) throws UnsupportedEncodingException {

		Range r = document.getRange();
		String trimText = "";
		List<String> mainWordList = new ArrayList<>();
		String keyName = "";

		for (int i = 0; i < r.numCharacterRuns(); i++) {
			CharacterRun cr = r.getCharacterRun(i);
			String text = cr.text().trim();

			if (cr.isBold() || text.equals(text.toUpperCase())) {
				if (!cr.isSpecialCharacter() && !text.isEmpty()) {
					// System.out.println(cr.text());
					trimText = text.replaceAll("\\s", "");

					for (Map.Entry<Object, Object> entry : prop.entrySet()) {
//						documentHelper.compareField(trimText, ((String) entry.getValue())) == true;
						if (((String) entry.getValue()).equalsIgnoreCase(trimText)) {
							for (String key : propJson.stringPropertyNames()) {
								if (entry.getKey().equals(key)) {
									mainWordList.add(trimText);
								}
							}
						}
					}
				}
			}
		}
		System.out.println(mainWordList);
		return mainWordList;
	}

	public static boolean isValidDate(String inDate, String formatDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatDate);
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(inDate.trim());		
		} catch (ParseException pe) {
			return false;
		}
		return true;
	}

}
