package edu.kaist.mrlab.cw.prepro;

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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.cedarsoftware.util.io.JsonWriter;

import info.debatty.java.stringsimilarity.Levenshtein;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

public class ELDisambiguation {

	/**
	 * Entity - Set<Surface> Map
	 */
	private Map<String, Set<String>> labelCache = new HashMap<>();
	private Map<String, Set<String>> entityTypes = new HashMap<>();
	private Map<String, String> entityDescription = new HashMap<>();
	/**
	 * Surface - Set<Entity> Map
	 */
	private Map<String, Set<String>> invLabelCache = new HashMap<>();

	private JSONParser mJsonParser = new JSONParser();

	private Path inputPath;
	private Path outputPath;
	private ArrayList<Path> filePathList;

	private Levenshtein l = new Levenshtein();

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

	public ELDisambiguation setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	public ELDisambiguation setOutputPath(Path outputPath) {
		this.outputPath = outputPath;
		return this;
	}

	private void loadLabelCache() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/labelCache.tsv"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
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

		for (String entity : labelCache.keySet()) {
			Set<String> labelSet = labelCache.get(entity);
//			System.out.println(entity + "\t" + labelSet);
			for (String label : labelSet) {
				if (invLabelCache.containsKey(label)) {
					Set<String> entitySet = invLabelCache.get(label);
					entitySet.add(entity);
					invLabelCache.put(label, entitySet);
				} else {
					Set<String> entitySet = new HashSet<>();
					entitySet.add(entity);
					invLabelCache.put(label, entitySet);
				}
			}
		}

		br.close();
	}

	private void loadDescription() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/entity_description.tsv"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			if (st.countTokens() != 2) {
				continue;
			}
			String entity = st.nextToken();
			String description = st.nextToken();

			entityDescription.put(entity, description);

		}
		br.close();
	}

	public void loadEntityTypes() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/entity_type_kbox"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String entity = st.nextToken();
			String type = st.nextToken();

			if (entityTypes.containsKey(entity)) {
				Set<String> typeSet = entityTypes.get(entity);
				typeSet.add(type);
				entityTypes.put(entity, typeSet);
			} else {
				Set<String> typeSet = new HashSet<>();
				typeSet.add(type);
				entityTypes.put(entity, typeSet);
			}
		}
		br.close();
	}

	public void getCandidates(String jsonInput, String fileName) throws Exception {

		JSONObject result = new JSONObject();

		JSONObject object = (JSONObject) mJsonParser.parse(jsonInput.trim());
		JSONArray addLabel = (JSONArray) object.get("addLabel");

		int docID = Integer.parseInt(object.get("docID").toString());
		int parID = Integer.parseInt(object.get("parID").toString());
		JSONArray entities = (JSONArray) object.get("entities");
		JSONArray delLabel = (JSONArray) object.get("delLabel");
		String globalSID = object.get("globalSID").toString();
		String plainText = object.get("plainText").toString();

		Iterator<JSONObject> it = addLabel.iterator();

		JSONArray newAddLabel = new JSONArray();

		int totalCount = 0;

		while (it.hasNext()) {
			JSONObject entityObj = it.next();
			int grade = Integer.parseInt(entityObj.get("grade").toString());
			String keyword = entityObj.get("keyword").toString();
			String dataType = entityObj.get("dataType").toString();

			if (grade == 1) {

				if (invLabelCache.containsKey(keyword)) {

					JSONArray candidates = new JSONArray();
					Set<String> entitySet = invLabelCache.get(keyword);
					int id = 0;

					for (String entity : entitySet) {
						JSONObject desObj = new JSONObject();
						String description = entityDescription.get(entity);
						if (description == null) {
							continue;
						}
						desObj.put("entity", entity);
						desObj.put("description", description);
						desObj.put("cID", id);
						candidates.add(desObj);
						id++;
					}

//					if (id >= 1) {
						entityObj.put("candidates", candidates);
						entityObj.put("selected", "");
						totalCount += id;
//					}

				} else {
					JSONArray candidates = new JSONArray();
					entityObj.put("candidates", candidates);
					entityObj.put("selected", "");
				}

			}

			newAddLabel.add(entityObj);
		}

		result.put("docID", docID);
		result.put("parID", parID);
		result.put("entities", entities);
		result.put("addLabel", newAddLabel);
		result.put("delLabel", delLabel);
		result.put("globalSID", globalSID);
		result.put("plainText", plainText);

		if (totalCount > 0) {
			BufferedWriter bw = Files.newBufferedWriter(Paths.get(outputPath.toString(), fileName));
			bw.write(JsonWriter.formatJson(result.toString()));
			bw.close();
		}

	}

	public void generate() throws Exception {
		BufferedReader br = null;
		while ((inputPath = ELDisambiguation.this.extractInputPath()) != null) {

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			System.out.println("doing: " + inputPath.toString());

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);

			String fileName = inputPath.getFileName().toString();

			if (fileName.contains("DS_Store")) {
				continue;
			}

			getCandidates(str.toString(), fileName);

		}
	}

	static String root = "data/annotated/wiki-10000-part2";

	public static void main(String[] ar) throws Exception {

		ELDisambiguation eld = new ELDisambiguation();

		eld.loadLabelCache();
		System.out.println("Label Cache loaded...!");
		eld.setInputPath(Paths.get(root, "task1/merge_190321"));
		eld.setOutputPath(Paths.get(root, "task1-2/input_190321"));
		eld.loadCorpus();
		System.out.println("Corpus loaded...!");
		eld.loadEntityTypes();
		System.out.println("Entity Types loaded...!");
		eld.loadDescription();
		System.out.println("Entity Description loaded...!");
		eld.generate();

	}

}
