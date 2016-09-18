package com.topitworks.parser;

import java.io.File;
import java.io.IOException;

public interface Parser {

    /**
     * The main purpose of this method is to parse unstructured data (resume file) to structured data (JSON).
     * This method accepts the resume file as java.io.File object, parse them and return a JSON string
     * which represents resume information
     *
     * @param resume resume file in DOC, DOCX or PDF extension
     * @param languageProperties 
     * @return JSON string represents resume information. Refer the suggested structure here
     * @throws IOException 
     */
    String parse(File resume, String languageProperties) throws IOException;
}
