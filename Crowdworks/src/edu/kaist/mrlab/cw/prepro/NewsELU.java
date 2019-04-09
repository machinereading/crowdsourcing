package edu.kaist.mrlab.cw.prepro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.kaist.mrlab.cw.script.RestCaller;

public class NewsELU {
	public static void main(String[] ar) throws Exception {

		int count = 2;

		RestCaller rc = new RestCaller();

		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/news_entity_tagged_" + count + ".txt"));

		Set<String> entitySet = new HashSet<>();

		// BufferedReader br =
		// Files.newBufferedReader(Paths.get("data/entity_type_kbox"));
		// String input = null;
		// while ((input = br.readLine()) != null) {
		// StringTokenizer st = new StringTokenizer(input, "\t");
		// String entity = st.nextToken();
		// String type = st.nextToken();
		//
		// Matcher m = Pattern.compile("^([0-9]+?)$").matcher(entity);
		// if (m.find()) {
		// continue;
		// }
		//
		// if (type.equals("AnatomicalStructure") || type.equals("Animal") ||
		// type.equals("Species")
		// || type.equals("TopicalConcept") || type.equals("Taxon") ||
		// type.equals("Taxon")
		// || type.equals("Eukaryote") || type.equals("Mammal") ||
		// type.equals("MusicGenre")
		// || type.equals("Genre") || type.equals("EthnicGroup") ||
		// type.equals("Taxon")) {
		// continue;
		// }
		//
		// if (type.equals("Person") || type.equals("Agent")) {
		// if (entity.length() > 2) {
		// entitySet.add(entity);
		// } else {
		// if (entitySet.contains(entity)) {
		// entitySet.remove(entity);
		// }
		// }
		// } else if (entity.length() >= 2) {
		// entitySet.add(entity.replace("_", " "));
		// }
		// }

		BufferedReader br = Files.newBufferedReader(Paths.get("data/news_parsed_" + count + ".txt"));
		BufferedWriter newInput = Files.newBufferedWriter(Paths.get("data/news_parsed_" + (count + 1) + ".txt"));
		String input = null;
		while ((input = br.readLine()) != null) {

			try {

				StringTokenizer st = new StringTokenizer(input, "\t");
				String xbID = st.nextToken();
				String wikiID = st.nextToken();
				String wikiTitle = st.nextToken();
				String content = st.nextToken();

				if (content.length() < 5 || content.length() > 300 || content.contains("cellpadding")
						|| content.contains("style") || content.contains("border")) {
					continue;
				}

				// if(input.contains("서성민")) {
				// System.out.println(input);
				// }

				content = content.replace("\"", "'").replace(" ", "").replace("<", "").replace(">", "").replace("/", "")
						.replace("\\", "");
				content = content.trim();

				String[] kv = content.split("\\s+");

				if (kv.length < 5) {
					continue;
				}

				String contentR = "";
				for (String k : kv) {
					contentR += k + " ";
				}
				contentR = contentR.trim();

				System.out.println(contentR);
				String resultR = null;

				resultR = rc.callKA(contentR);

				// System.out.println(resultR);
				String result = "";
				String elResult = rc.callKoEL(resultR);
				JSONParser parser = new JSONParser();
				JSONArray array = (JSONArray) parser.parse(elResult);

				Iterator<JSONObject> it = array.iterator();

				int startIndex = 0;

				while (it.hasNext()) {

					JSONObject entity = it.next();
					int start = Integer.parseInt(entity.get("start_offset").toString());
					int end = Integer.parseInt(entity.get("end_offset").toString());
					String text = entity.get("text").toString();
					String uri = entity.get("uri").toString().replace("http://ko.dbpedia.org/resource/", "");

					if (startIndex < 0) {
						break;
					}

					String first = contentR.substring(startIndex, start);

					if (text.length() < 2 || (uri.contains("(") && uri.contains(")") || uri.contains("불교"))) {
						result += first + text;
					} else {
						result += first + "<elu>" + text + "|" + uri + "</elu>";
					}

					startIndex = end;

				}

				result += contentR.substring(startIndex, contentR.length());

				System.out.println(xbID + "\t" + wikiID + "\t" + wikiTitle + "\t" + result);

				bw.write(xbID + "\t" + wikiID + "\t" + wikiTitle + "\t" + result + "\n");
			} catch (Exception e) {

				bw.close();

				br.readLine();
				while ((input = br.readLine()) != null) {
					newInput.write(input + "\n");
				}
				newInput.close();
				e.printStackTrace();
			}
		}

		bw.close();

	}
}
