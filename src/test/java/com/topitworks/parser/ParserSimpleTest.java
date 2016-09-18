package com.topitworks.parser;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.topitworks.parser.impl.ResumeParser;

public class ParserSimpleTest {

    @Before
    public void setUp() throws Exception {
    	
    }
    
    Parser parser = new ResumeParser();
    
    @Test
    public void test_cv_en() throws Exception {  	
    	File file = new File("src/test/resources/sample_cv_en.doc");    
    	String languageProperties = "candidatedocument_en.properties";
    	parser.parse(file, languageProperties);
    	System.out.println("Done");
    }
    
    @Test
    public void test_cv_vi() throws Exception {  	   	
    	File file = new File("src/test/resources/sample_cv_vi.doc");  
    	String languageProperties = "candidatedocument_vi.properties";
    	parser.parse(file, languageProperties);
    	System.out.println("Done");
    }
}