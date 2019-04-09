package edu.kaist.mrlab.cw.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class WikidataAltLabel {

	private static String WIKIPROPERTY = "http://www.wikidata.org/entity/";
	private static String DBP = "http://dbpedia.org/ontology/";

	private Map<String, Set<String>> labelCache = new HashMap<>();
	/**
	 * WIKI, DBO
	 */
	private Map<String, String> sameAsMap = new HashMap<>();
	/**
	 * DBO, WIKI
	 */
	private Map<String, String> sameAsMapInv = new HashMap<>();

	private void loadSameAsMap() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/dbpedia_2016-10.nt"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, " ");
			String sbj = st.nextToken();
			String prd = st.nextToken();
			String obj = st.nextToken();
			if (obj.contains(WIKIPROPERTY) && prd.contains("equivalentProperty")) {

				sbj = sbj.substring(1, sbj.length() - 1);
				sbj = sbj.replace(DBP, "");

				obj = obj.substring(1, obj.length() - 1);
				obj = obj.replace(WIKIPROPERTY, "");

				sameAsMap.put(obj, sbj);
				sameAsMapInv.put(sbj, obj);
			}
		}
		System.out.println("Loaded sameAs link..!");
	}

	private void generate() throws Exception {
		
		BufferedReader br = Files.newBufferedReader(Paths.get("data/wikidata_dump.nt"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, " ");
			String sbj = st.nextToken();
			String prd = st.nextToken();
			String obj = "";
			while (st.hasMoreTokens()) {
				obj += st.nextToken() + " ";
			}
			obj = obj.substring(0, obj.length() - 3);

			if ((prd.contains("rdf-schema#label") 
					|| prd.contains("skos/core#altLabel")
					|| prd.contains("skos/core#prefLabel")
					|| prd.contains("schema.org/name")
//					|| prd.contains("schema.org/description")
			) && (obj.contains("@ko") 
					|| obj.contains("@en")
					) && (!obj.contains("@koi"))) {

				sbj = sbj.substring(1, sbj.length() - 1);
				sbj = sbj.replace(WIKIPROPERTY, "");

				obj = obj.replace("@en-ca", "");
				obj = obj.replace("@en-gb", "");
				obj = obj.replace("@en", "");
				obj = obj.replace("@ko", "");
				obj = obj.substring(1, obj.length() - 1);
				if (obj.contains("\\u")) {
					obj = unicodeConvert(obj);
				}

				String dbSbj = sameAsMap.get(sbj);

				if (dbSbj == null || obj.contains("Wikimedia") || obj.contains("위키미디어")) {
					continue;
				}

				if (labelCache.containsKey(dbSbj)) {
					Set<String> labelSet = labelCache.get(dbSbj);
					labelSet.add(obj);
					labelCache.put(dbSbj, labelSet);
				} else {
					Set<String> labelSet = new HashSet<>();
					labelSet.add(obj);
					labelCache.put(dbSbj, labelSet);
				}

//				System.out.println("\t" + dbSbj + "\t" + labelCache.get(dbSbj));

			}
		}
		System.out.println("Generated labelCache from Wikidata..!");
		printStatistics();
		
	}

	private void writeFile() throws Exception {
		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/dbp-wikidata-labels.tsv"));
		for (String sbj : labelCache.keySet()) {
			Set<String> labelSet = labelCache.get(sbj);
			for (String label : labelSet) {
				bw.write(sbj + "\t" + sameAsMapInv.get(sbj) + "\t" + label + "\n");
			}
		}
		bw.close();
		System.out.println("File writing done..!");
	}

	private void printStatistics() throws Exception {
		int entitySize = labelCache.keySet().size();
		int labelSize = 0;
		for (String sbj : labelCache.keySet()) {
			labelSize += labelCache.get(sbj).size();
		}

		System.out.println("== Number of Entities : " + entitySize);
		System.out.println("== Number of Labels : " + labelSize);
		System.out.println("== Ratio of label size over entity size : " + ((double) labelSize / (double) entitySize));
	}

	public static String unicodeConvert(String str) {
		StringBuilder sb = new StringBuilder();
		char ch;
		int len = str.length();
		try {
			for (int i = 0; i < len; i++) {
				ch = str.charAt(i);
				if (ch == '\\' && str.charAt(i + 1) == 'u') {
					sb.append((char) Integer.parseInt(str.substring(i + 2, i + 6), 16));
					i += 5;
					continue;
				}
				sb.append(ch);
			}
		} catch (Exception e) {
			System.out.println(str);
		}

		return sb.toString();
	}

	public static void main(String ar[]) throws Exception {
		WikidataAltLabel lcg = new WikidataAltLabel();
		lcg.loadSameAsMap();
		lcg.generate();
		lcg.writeFile();
	}
}
