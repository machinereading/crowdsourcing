package edu.kaist.mrlab.cw.prepro;

import java.io.FileReader;
import java.io.Reader;
import java.util.HashSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class Statistics {
	public static void main(String[] ar) throws Exception {
		
		HashSet<String> sentenceSet = new HashSet<>();
		
		Reader in = new FileReader("data/ds/ds_label_par_rule1.csv");
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			String sentence = record.get(0);
			String relation = record.get(1);
		
			sentence.replace("<e1>", "");
			sentence.replace("</e1> ", "");
			
			sentence.replace("<e2>", "");
			sentence.replace("</e2> ", "");
			
			sentence.replace("[", "");
			sentence.replace("] ", "");
			
			sentenceSet.add(sentence);
			
		}
		
		System.out.println(sentenceSet.size());
		
	}
}
