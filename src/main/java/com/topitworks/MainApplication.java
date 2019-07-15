package com.topitworks;

import com.topitworks.parser.Parser;
import com.topitworks.parser.impl.ResumeParser;
import java.io.File;
import java.io.IOException;

public class MainApplication {

  public static void main(String[] args) throws IOException {
    Parser parser = new ResumeParser();
    System.out.println("Start read file: src/test/resources/sample_cv_en.doc");
    File file = new File("src/test/resources/sample_cv_en.doc");
    String languageProperties = "candidatedocument_en.properties";
    parser.parse(file, languageProperties);
    System.out.println("Done");
  }
}
