package edu.kaist.mrlab.cw.script;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class InputFormatter {
	
	private static String cnnInputPath = "/test/wjd1004109/PL-Web-Demo/prepro/data/wiki_ex_PL4.txt";
	private static String b2kInputPath = "/test/wjd1004109/PL-Web-Demo/prepro/data/wiki_ex_PL5.txt";
	
	public static void prepare(String paragraph) throws Exception {
		
		BufferedWriter cnn = Files.newBufferedWriter(Paths.get(cnnInputPath));
		BufferedWriter b2k = Files.newBufferedWriter(Paths.get(b2kInputPath));
		
		String sentences = getLines(paragraph);
		StringTokenizer st = new StringTokenizer(sentences, "\t");
		while(st.hasMoreTokens()) {
			String sentence = st.nextToken();
			cnn.write(sentence + "\n");
			b2k.write(sentence.replace("<< ", " [").replace(" >>", "] ") + "\n");
		}
		
		cnn.close();
		b2k.close();
		
	}
	
	public static String getLines(String input) {
		String result = "";
		String[] arr = input.split("[다][.]");

		if (arr.length == 1) {
			input = input.trim();
			result += (input + "\n");
			return result;
		} else if (arr.length > 1) {
			for (String ar : arr) {
				if (ar.endsWith(".") || (ar.length() == 0)) {
					ar = ar.trim();
					result += (ar + "\n");
				} else {
					ar = ar.trim();
					result += (ar + "다.\n");
				}
			}
		}
		return result;
	}
}
