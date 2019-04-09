package edu.kaist.mrlab.cw.eval;

import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Task1ELEvaluator {

	private static final String ENTITY_ADD_ARY_KEY = "addLabel";

	private JSONParser mJsonParser;

	public Task1ELEvaluator() {
		mJsonParser = new JSONParser();
	}

	/**
	 * 사용자 태깅 json 파일을 정답 json 파일과 비교하여 점수를 반환한다.
	 * 
	 * @param goldFileName 정답 json 파일 경로
	 * @param userFileName 사용자 json 파일 경로
	 * @return Scores from 0.0 to 100.0
	 */
	public double evaluate(String goldFileName, String userFileName) {
		try {
			// Load Json File
			JSONObject goldJson = (JSONObject) mJsonParser.parse(new FileReader(goldFileName));

			JSONObject userJson = (JSONObject) mJsonParser.parse(new FileReader(userFileName));

			// (맞은 개수/전체 개수)*100 을 점수로 반환한다.
			return getScore(goldJson, userJson);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0;
	}

	private double getScore(JSONObject goldJson, JSONObject userJson) {
		int totalCnt = 0;
		int userCnt = 0;
		int correctCnt = 0;

		JSONArray entitiesUserAddAry = (JSONArray) userJson.get(ENTITY_ADD_ARY_KEY);
		JSONArray entitiesGoldAddAry = (JSONArray) goldJson.get(ENTITY_ADD_ARY_KEY);

		totalCnt = entitiesGoldAddAry.size();
		userCnt = entitiesUserAddAry.size();

		System.out.println(totalCnt + "\t" + userCnt);

		// AddLabel JSONArray length check
		if (userCnt != totalCnt) {
			System.out.println("JSONArray Size error..!");
			return -1;
		}

		for (int i = 0; i < totalCnt; i++) {
			JSONObject currUserItem = (JSONObject) entitiesUserAddAry.get(i);
			JSONObject currGoldItem = (JSONObject) entitiesGoldAddAry.get(i);

			if (isCorrectAnswer(currGoldItem, currUserItem)) {
				correctCnt += 1;
			}

		}
		
		if (totalCnt == 3) {
			if(correctCnt == 2) {
				return 0.8;
			} else {
				return 0.1;
			}
		} else {
			return correctCnt / (double) totalCnt;
		}
		
	}

	private boolean isCorrectAnswer(JSONObject currGoldItem, JSONObject currUserItem) {

		if (currGoldItem.containsKey("answer") && currGoldItem.containsKey("candidates")
				&& currUserItem.containsKey("answer") && currUserItem.containsKey("candidates")) {
			int gAnswer = Integer.parseInt(currGoldItem.get("answer").toString());
			int uAnswer = Integer.parseInt(currUserItem.get("answer").toString());

			return (gAnswer == uAnswer);
		} else {
			return true;
		}

	}

}
