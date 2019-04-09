package edu.kaist.mrlab.cw.prepro;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class Rule1 {
	public static void main(String[] ar) throws Exception {
		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/ds/ds_label_par_rule1.csv"));
		Reader in = new FileReader("data/ds/ds_label_par_.csv");
		CSVPrinter csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			String sentence = record.get(0);
			String relation = record.get(1);
			
			sentence = sentence.replace("[이하나] ", "하나");
			if(sentence.contains("<e1>이하나</e1>") || sentence.contains("(불교)")) {
				continue;
			}
			
			csvPrinter.printRecord(sentence, relation);

		}
		
		in.close();
		csvPrinter.close();
		bw.close();
	}
}
