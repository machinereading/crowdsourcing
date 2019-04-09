package edu.kaist.mrlab.cw.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class EntityDescription {
	public static void main(String[] ar) throws Exception {
		
		String DBPENTITY = "http://ko.dbpedia.org/resource/";
		
		BufferedReader br = Files.newBufferedReader(Paths.get("data/tsv_long_abstracts_ko.ttl"));
		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/entity_description.tsv"));
		
		String input = null;
		while((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String sbj = st.nextToken();
			st.nextToken(); // prd;
			String obj = st.nextToken();
			
			sbj = sbj.substring(1, sbj.length() - 1);
			sbj = sbj.replace(DBPENTITY, "");
			obj = obj.replace("@ko", "");
			obj = obj.substring(1, obj.length() - 1);

			bw.write(sbj + "\t" + obj + "\n");
			
		}
		
	}
}
