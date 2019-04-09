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

public class LabelCacheGenerator {

	private static String WIKIENTITY = "http://www.wikidata.org/entity/";
	private static String DBPENTITY = "http://ko.dbpedia.org/resource/";

	private Map<String, Set<String>> labelCache = new HashMap<>();
	private Map<String, String> sameAsMap = new HashMap<>();

	private void loadSameAsMap() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/tsv_interlanguage_links_ko.ttl"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String sbj = st.nextToken();
			st.nextToken();
			String obj = st.nextToken();
			if (obj.contains(WIKIENTITY)) {

				sbj = sbj.substring(1, sbj.length() - 1);
				sbj = sbj.replace(DBPENTITY, "");

				obj = obj.substring(1, obj.length() - 1);
				obj = obj.replace(WIKIENTITY, "");

				sameAsMap.put(obj, sbj);
			}
		}
		System.out.println("Loaded sameAs link..!");
	}

	private void generate() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/tsv_labels_ko.ttl"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String sbj = st.nextToken();
			st.nextToken(); // pred
			String obj = st.nextToken();

			sbj = sbj.substring(1, sbj.length() - 1);
			sbj = sbj.replace("http://ko.dbpedia.org/resource/", "");

			obj = obj.substring(1, obj.length() - 4);

			if (labelCache.containsKey(sbj)) {
				Set<String> labelSet = labelCache.get(sbj);
				labelSet.add(obj);
				labelCache.put(sbj, labelSet);
			} else {
				Set<String> labelSet = new HashSet<>();
				labelSet.add(obj);
				labelCache.put(sbj, labelSet);
			}
		}

		System.out.println("Generated labelCache from DBpedia..!");
		printStatistics();
		
		br = Files.newBufferedReader(Paths.get("data/wikipedia_surface_entity.txt"));
		while((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			if(st.countTokens() != 2) {
				continue;
			}
			String entity = st.nextToken();
			String surface = st.nextToken();
			
			if (labelCache.containsKey(entity)) {
				Set<String> labelSet = labelCache.get(entity);
				labelSet.add(surface);
				labelCache.put(entity, labelSet);
			} else {
				Set<String> labelSet = new HashSet<>();
				labelSet.add(surface);
				labelCache.put(entity, labelSet);
			}
		}
		
		System.out.println("Generated labelCache from Wikipedia..!");
		printStatistics();
		
		br = Files.newBufferedReader(Paths.get("data/wikidata_dump.nt"));
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, " ");
			String sbj = st.nextToken();
			String prd = st.nextToken();
			String obj = "";
			while (st.hasMoreTokens()) {
				obj += st.nextToken();
			}
			obj = obj.substring(0, obj.length() - 1);

			if ((prd.contains("rdf-schema#label") 
					|| prd.contains("skos/core#altLabel")
					|| prd.contains("skos/core#prefLabel")
					|| prd.contains("schema.org/name")
//					|| prd.contains("schema.org/description")
			) && (obj.contains("@ko") 
//					|| obj.contains("@en")
					) && (!obj.contains("@koi"))) {

				sbj = sbj.substring(1, sbj.length() - 1);
				sbj = sbj.replace(WIKIENTITY, "");

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
		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/labelCache.tsv"));
		for (String sbj : labelCache.keySet()) {
			Set<String> labelSet = labelCache.get(sbj);
			for (String label : labelSet) {
				bw.write(sbj + "\t" + label + "\n");
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
		LabelCacheGenerator lcg = new LabelCacheGenerator();
		lcg.loadSameAsMap();
		lcg.generate();
		lcg.writeFile();
	}
}
