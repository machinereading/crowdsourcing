package edu.kaist.mrlab.cw.postpro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ELD {

	private Path inputPath;
	private ArrayList<Path> filePathList;

	public void loadCorpus() throws Exception {
		this.filePathList = Files.walk(this.inputPath).filter(p -> Files.isRegularFile(p))
				.collect(Collectors.toCollection(ArrayList::new));
		System.out.println("done!");
		System.out.println("Number of file paths: " + filePathList.size());
	}

	private synchronized Path extractInputPath() {
		if (this.filePathList.isEmpty()) {
			return null;
		} else {
			return this.filePathList.remove(this.filePathList.size() - 1);
		}
	}

	public ELD setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	public void loadLBox() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/wiki-entity-tagged-corpus.tsv"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			if (st.countTokens() == 4) {
				String docID = st.nextToken();
				String parID = st.nextToken();
				String stcIDs = st.nextToken();
				String par = st.nextToken();

				IDMap.put(docID + "\t" + parID, par);
			}
		}
	}

	public HashMap<String, List<String>> kboxEntityTypes = new HashMap<>();

	public void loadKBoxEntityType() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/entity_type_kbox"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			if (st.countTokens() == 2) {
				String entity = st.nextToken();
				String type = st.nextToken();
				List<String> types = null;
				if (kboxEntityTypes.containsKey(entity)) {
					types = kboxEntityTypes.get(entity);
					types.add(type);
				} else {
					types = new ArrayList<>();
					types.add(type);
				}
				kboxEntityTypes.put(entity, types);
			}
		}
	}

	public HashMap<String, List<String>> entityMap = new HashMap<>();

	public String getPlainText(String input) {
		String result = input;
		int open = result.indexOf("<link>");
		int close = -1;
		int bar = -1;
		String first = null;
		String second = null;
		String surface = null;
		String entity = null;
		while (open >= 0) {
			bar = result.indexOf("|", open);
			close = result.indexOf("</link>", bar);
			surface = result.substring(open + 6, bar);
			entity = result.substring(bar + 1, close);
			first = result.substring(0, open);
			second = result.substring(close + 7, result.length());

			result = first + "[" + surface + "]" + second;

			List<String> set = null;
			if (entityMap.containsKey(entity)) {
				set = entityMap.get(entity);
				set.add(surface);
			} else {
				set = new ArrayList<String>();
				set.add(surface);
			}
			entityMap.put(entity, set);

			open = result.indexOf("<link>");

		}

		open = result.indexOf("<elu>");
		while (open >= 0) {
			bar = result.indexOf("|", open);
			close = result.indexOf("</elu>", bar);
			surface = result.substring(open + 5, bar);
			entity = result.substring(bar + 1, close);
			first = result.substring(0, open);
			second = result.substring(close + 6, result.length());

			if (surface.length() < 3) {

				result = first + surface + second;

			} else {
				result = first + "[" + surface + "]" + second;
				List<String> set = null;
				if (entityMap.containsKey(entity)) {
					set = entityMap.get(entity);
					set.add(surface);
				} else {
					set = new ArrayList<String>();
					set.add(surface);
				}
				entityMap.put(entity, set);
			}

			open = result.indexOf("<elu>");

		}

		return result;
	}

	public HashMap<String, String> IDMap = new HashMap<>();
	private BufferedReader br;

	private int open;
	private int close;
	private int idx;

	public String getSurfaceItem(String boundary, int idx) {

		open = boundary.indexOf("[", idx);
		close = boundary.indexOf("]", open);

		String surfaceItem = boundary.substring(open + 1, close);

		return surfaceItem;
	}

	public void generate() throws Exception {

		while ((inputPath = ELD.this.extractInputPath()) != null) {

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			System.out.println(inputPath.toString());

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);

			if (inputPath.toString().contains("input_402.json")
					|| inputPath.toString().contains("input_478.json") || inputPath.toString().contains("input_5.json")
					|| inputPath.toString().contains("input_472.json")
					|| inputPath.toString().contains("input_202.json")
					|| inputPath.toString().contains("input_296.json") || inputPath.toString().contains("input_62.json")
					|| inputPath.toString().contains("input_493.json")
					|| inputPath.toString().contains("input_324.json") || inputPath.toString().contains("input_37.json")
					|| inputPath.toString().contains("input_312.json")
					|| inputPath.toString().contains("input_499.json")
					|| inputPath.toString().contains("input_271.json")
					|| inputPath.toString().contains("input_461.json")
					|| inputPath.toString().contains("input_50.json")) {
				continue;
			}

			JSONObject result = new JSONObject();

			JSONParser parser = new JSONParser();
			JSONObject object = (JSONObject) parser.parse(str.toString().trim());
			String text = object.get("text").toString();
			int wikiPageID = Integer.parseInt(object.get("wikiPageId").toString());
			int pIdx = Integer.parseInt(object.get("pIdx").toString());
			JSONArray entities = (JSONArray) object.get("entities");
			JSONArray globalSID = (JSONArray) object.get("globalSId");

			String par = IDMap.get(wikiPageID + "\t" + pIdx);
			par = par.replace("[.<line>.]", " ");
			// System.out.println(par);

			String boundary = getPlainText(par).trim();
			// System.out.println(boundary);
			String plainText = boundary.replace("[", "").replace("]", "");

			String mentionTagged = "";
			idx = 0;

			int countEntity = 0;

			for (int i = 0; i < entities.size(); i++) {

				JSONObject entity = (JSONObject) entities.get(i);
				int st = Integer.parseInt(entity.get("st").toString());
				int en = Integer.parseInt(entity.get("en").toString());
				String keyword = entity.get("keyword").toString().replace("[", "").replace("]", "").replace("<", "")
						.replace(">", "");
				String eType = entity.get("e_type").toString();

				List<String> surfaces = entityMap.get(keyword);

				if (surfaces == null) {
					surfaces = new ArrayList<String>();
					surfaces.add(keyword);
				}
				System.out.println(surfaces);

				if (eType.equals("NEW")) {
					int s = boundary.indexOf(surfaces.get(0), idx);
					String left = boundary.substring(0, s);
					String right = boundary.substring(s + surfaces.get(0).length(), boundary.length());
					boundary = left + "[" + surfaces.get(0) + "]" + right;
					boundary = boundary.replace("[[", "[").replace("]]", "]");
				}

				open = boundary.indexOf("[", idx);
				close = boundary.indexOf("]", open);

				String surfaceItem = getSurfaceItem(boundary, idx);

				if (open != 0 && open - idx > 0) {
					mentionTagged += boundary.substring(idx, open);
				}

				while (!surfaces.contains(surfaceItem)) {
					mentionTagged += boundary.substring(open + 1, close);
					idx = close + 1;
					surfaceItem = getSurfaceItem(boundary, idx);
					if (open != 0 && open - idx > 0) {
						mentionTagged += boundary.substring(idx, open);
					}
					countEntity++;
				}

				idx = close + 1;

				mentionTagged += boundary.substring(open, idx);

				int stMention = open - countEntity * 2;
				int enMention = idx - (countEntity + 1) * 2;

				entity.put("st_mention", stMention);
				entity.put("en_mention", enMention);

				countEntity++;

				JSONArray kboxTypeArray = new JSONArray();
				List<String> types = kboxEntityTypes.get(keyword.replace(" ", "_"));
				if (types != null) {
					for (String type : types) {
						kboxTypeArray.add(type);
					}
				} else {
					kboxTypeArray.add("NULL");
				}

				String net = null;

				if (kboxTypeArray.contains("Person")) {
					net = "PERSON";
				} else if (kboxTypeArray.contains("Organisation")) {
					net = "ORGANIZATION";
				} else if (kboxTypeArray.contains("Place")) {
					net = "LOCATION";
				} else {
					net = "ETC";
				}

				Matcher m = Pattern.compile("^([0-9]+?)년$").matcher(keyword);
				if (m.find()) {
					net = "TIME";
				}
				m = Pattern.compile("^([0-9]+?)월_([0-9]+?)일$").matcher(keyword);
				if (m.find()) {
					net = "TIME";
				}
				m = Pattern.compile("^([0-9]+?)월 ([0-9]+?)일$").matcher(keyword);
				if (m.find()) {
					net = "TIME";
				}
				m = Pattern.compile("^([0-9]+?)월$").matcher(keyword);
				if (m.find()) {
					net = "TIME";
				}
				m = Pattern.compile("^([0-9]+?)일$").matcher(keyword);
				if (m.find()) {
					net = "TIME";
				}

				entity.put("kbox_types", kboxTypeArray);
				if (!eType.equals("NEW")) {
					entity.put("ne_type", net);
				}

			}

			if (idx < boundary.length()) {
				mentionTagged += boundary.substring(idx, boundary.length());
			}

			// System.out.println(mentionTagged);

			String fileName = inputPath.getFileName().toString();

			result.put("globalSId", globalSID);
			result.put("wikiPageId", wikiPageID);
			result.put("pIdx", pIdx);
			result.put("entityTagged", text);
			result.put("mentionTagged", mentionTagged);
			result.put("plainText", plainText);
			result.put("entities", entities);

			// System.out.println(result.toJSONString());

			BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/annotated/task1_ELD/", fileName));
			bw.write(result.toJSONString());
			bw.close();

			open = -1;
			close = -1;

			entityMap.clear();

		}

	}

	public static void main(String[] ar) throws Exception {

		ELD eld = new ELD();
		eld.setInputPath(Paths.get("data/annotated/task2_input"));
		eld.loadCorpus();
		eld.loadLBox();
		eld.loadKBoxEntityType();
		eld.generate();

	}

}
