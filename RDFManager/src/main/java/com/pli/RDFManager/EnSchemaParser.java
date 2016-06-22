package com.pli.RDFManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class EnSchemaParser {
	
	public static String pathPrefix = "//home//pli//Desktop//";
	
	public static void main( String[] args ) throws FileNotFoundException
    {
		String path = pathPrefix + "ontology.enSchema";
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    	System.out.println(line);
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
