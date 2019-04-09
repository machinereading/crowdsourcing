package edu.kaist.mrlab.cw.eval;

import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Task1EDEvaluator {

	private static final String ENTITY_ADD_ARY_KEY = "addLabel";
	private static final String ENTITY_DEL_ARY_KEY = "delLabel";
	private static final double addWeight = 0.9;
	private static final double delWeight = 0.1;

	private JSONParser mJsonParser;

	public Task1EDEvaluator() {
		mJsonParser = new JSONParser();
	}

	/**
	 * 사용자 태깅 json 파일을 정답 json 파일과 비교하여 점수를 반환한다.
	 * 
	 * @param goldFileName
	 *            정답 json 파일 경로
	 * @param userFileName
	 *            사용자 json 파일 경로
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
		double userAnswerCnt = 0;
		double goldAnswerCnt = 0;
		int correctCnt = 0;
		double score = 0.0;

		JSONArray entitiesUserAddAry = (JSONArray) userJson.get(ENTITY_ADD_ARY_KEY);
		JSONArray entitiesGoldAddAry = (JSONArray) goldJson.get(ENTITY_ADD_ARY_KEY);

		userAnswerCnt = entitiesUserAddAry.size();
		goldAnswerCnt = entitiesGoldAddAry.size();

		for (int i = 0; i < userAnswerCnt; i++) {
			JSONObject currUserItem = (JSONObject) entitiesUserAddAry.get(i);
			int ugrade = Integer.parseInt(currUserItem.get("grade").toString());
			if (ugrade == 1) {
				for (int j = 0; j < goldAnswerCnt; j++) {
					JSONObject currGoldItem = (JSONObject) entitiesGoldAddAry.get(j);

					if (isCorrectAnswer(currGoldItem, currUserItem, true)) {
						correctCnt += 1;
						break;
					}
				}
			} else {
				userAnswerCnt--;
			}

		}

		double addPrecision = correctCnt / (userAnswerCnt + 0.001);
		double addRecall = correctCnt / (goldAnswerCnt + 0.001);

		double addF1 = 2 * addPrecision * addRecall / (addPrecision + addRecall + 0.001);

		JSONArray entitiesUserDelAry = (JSONArray) userJson.get(ENTITY_DEL_ARY_KEY);
		JSONArray entitiesGoldDelAry = (JSONArray) goldJson.get(ENTITY_DEL_ARY_KEY);

		userAnswerCnt = entitiesUserDelAry.size();
		goldAnswerCnt = entitiesGoldDelAry.size();
		correctCnt = 0;

		if (goldAnswerCnt == 0 && userAnswerCnt == 0) {
			return addF1 * 100;
		}

		for (int i = 0; i < userAnswerCnt; i++) {
			JSONObject currUserItem = (JSONObject) entitiesUserDelAry.get(i);
			for (int j = 0; j < goldAnswerCnt; j++) {
				JSONObject currGoldItem = (JSONObject) entitiesGoldDelAry.get(j);

				if (isCorrectAnswer(currGoldItem, currUserItem, false)) {
					correctCnt += 1;
				}
			}

		}

		double delPrecision = correctCnt / (userAnswerCnt + 0.001);
		double delRecall = correctCnt / (goldAnswerCnt + 0.001);

		double delF1 = 2 * delPrecision * delRecall / (delPrecision + delRecall + 0.001);

		score = (addF1 * addWeight + delF1 * delWeight) * 100;

		return score;
	}

	private boolean isCorrectAnswer(JSONObject currGoldItem, JSONObject currUserItem, boolean isAdd) {

		String gkeyword = currGoldItem.get("keyword").toString();
		String gdataType = "DEL";
		if (isAdd) {
			gdataType = currGoldItem.get("dataType").toString();
		}
		int gstartPosition = Integer.parseInt(currGoldItem.get("startPosition").toString());
		int gendPosition = Integer.parseInt(currGoldItem.get("endPosition").toString());

		String ukeyword = currUserItem.get("keyword").toString();
		String udataType = "DEL";
		if (isAdd) {
			udataType = currUserItem.get("dataType").toString();
		}
		int ustartPosition = Integer.parseInt(currUserItem.get("startPosition").toString());
		int uendPosition = Integer.parseInt(currUserItem.get("endPosition").toString());
		// Entity 등급

		return (ukeyword.equals(gkeyword)) && (gstartPosition == ustartPosition)
				&& (gendPosition == uendPosition);
	}

}
