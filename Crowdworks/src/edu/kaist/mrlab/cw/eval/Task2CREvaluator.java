package edu.kaist.mrlab.cw.eval;

import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Task2CREvaluator {
	
	private static final String ENTITY_ARY_KEY = "entities";
	private static final String PRONOUNS_ARY_KEY = "pronouns";
	private static final String ANCESTOR_KEY = "ancestor";
		
	private JSONParser mJsonParser;
	
	
	public Task2CREvaluator() {
		mJsonParser = new JSONParser();
	}
	
	/**
	 * 사용자 태깅 json 파일을 정답 json 파일과 비교하여 점수를 반환한다. 
	 * @param goldFileName 정답 json 파일 경로
	 * @param userFileName 사용자 json 파일 경로 
	 * @return Scores from 0.0 to 100.0
	 */
	public double evaluate(String goldFileName, String userFileName) {
		try {
			// Load Json File
			Object goldObj = mJsonParser.parse(new FileReader(goldFileName));
			JSONArray goldParList = (JSONArray) goldObj;
			JSONObject goldJson = (JSONObject) goldParList.get(goldParList.size()-1); 
			
			Object userObj = mJsonParser.parse(new FileReader(userFileName));
			JSONObject userJson = (JSONObject) userObj;
			
			// 조사가 제대로 안 지워졌으면 무조건 0점 
			if (!isJosaRemoved(goldJson, userJson)) {
				return 0.0;
			}
			
			// (맞은 개수/전체 개수)*100 을 점수로 반환한다. 
			// 단 평가 개수가 10개 미만일 때는 1개 틀린 것은 90점으로 봐줘서 통과하게 함.
			return getScore(goldJson, goldParList, userJson); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0;
	}
	
	private boolean isJosaRemoved(JSONObject goldJson, JSONObject userJson) {
		JSONArray goldPronounAry = (JSONArray) goldJson.get(PRONOUNS_ARY_KEY);
		JSONArray userPronounAry = (JSONArray) userJson.get(PRONOUNS_ARY_KEY);
		
		for (int i=0; i<goldPronounAry.size(); i++) {
			JSONObject currGoldPronoun = (JSONObject) goldPronounAry.get(i);
			if (((String)currGoldPronoun.get(ANCESTOR_KEY)).length() > 2) {
				/*
				 * 선행사가 있는 대명사 중에서,
				 * 조사가 제대로 안지워진 대명사가 있는지 확인
				 */
				String goldJosaRemoveTxt = (String) currGoldPronoun.get("surface");
				int currId = ((Long) currGoldPronoun.get("id")).intValue();
				for (int j=0; j<userPronounAry.size(); j++) {
					JSONObject currUserPronoun = (JSONObject) userPronounAry.get(j);
					if (((Long) currUserPronoun.get("id")).intValue() == currId) {
						String userJosaRemoveTxt = (String) currUserPronoun.get("surface");
						
						if (!goldJosaRemoveTxt.equals(userJosaRemoveTxt))
							return false;
					}
				}
			}
		}
		
		return true;
	}
	
	private double getScore(JSONObject goldJson, JSONArray goldParList, JSONObject userJson) {
		int totalCnt = 0;
		int correctCnt = 0;
		double score = 0.0;
		
		JSONArray entitiesAry = (JSONArray) userJson.get(ENTITY_ARY_KEY);
		for (int i=0; i<entitiesAry.size(); i++) {
			JSONObject currItem = (JSONObject)entitiesAry.get(i);
			if ((Boolean)currItem.get("is_target")) {
				totalCnt += 1;
				if (isCorrectAnswer(goldJson, goldParList, userJson, ENTITY_ARY_KEY, ((Long) currItem.get("id")).intValue())) {
					correctCnt += 1;
				}
			}
		}
		
		JSONArray pronounsAry = (JSONArray) userJson.get(PRONOUNS_ARY_KEY);
		for (int i=0; i<pronounsAry.size(); i++) {
			JSONObject currItem = (JSONObject)pronounsAry.get(i);
			totalCnt += 1;
			if (isCorrectAnswer(goldJson, goldParList, userJson, PRONOUNS_ARY_KEY, ((Long) currItem.get("id")).intValue())) {
				correctCnt += 1;
			}
		}
		
		if (totalCnt < 10 && correctCnt == (totalCnt-1)) {
			// 평가 개수가 10개 미만 일 때는 1개 틀린 것은 90점이라고 쳐줌.
			score = 90.0;
		} else {
			score = (double)correctCnt / (double)totalCnt * 100.0;
		}
		
		return score + 0.01;
	}
	
	private boolean isCorrectAnswer(JSONObject goldJson, JSONArray goldParList, JSONObject userJson, String targetAryKey, int targetId) {
		String userAncestorId="", userCorefGroup="";
		String goldAncestorId="", goldCorefGroup="";
		
		
		JSONObject userAnswer = getJSONObjHasID((JSONArray) userJson.get(targetAryKey), targetId);
		JSONObject goldAnswer = getJSONObjHasID((JSONArray) goldJson.get(targetAryKey), targetId);
		
		if (userAnswer == null || goldAnswer == null)
			return false;
		
		userAncestorId = (String) userAnswer.get(ANCESTOR_KEY);
		if(userAncestorId.length() > 2) {
			// 사용자 태깅에 선행사가 존재할 경우 해당 선행사의 gropuName(EntityName)을 가져옴
			String[] ansIds = userAncestorId.split("-");
			String ansParId = ansIds[0];
			int ansEntId = Integer.parseInt(ansIds[1]);
			for (int i=0; i<goldParList.size(); i++){
				JSONObject currPar = (JSONObject) goldParList.get(i);
				if (ansParId.equals((String)currPar.get("parID"))) {
					try {
						JSONArray currParEntAry = (JSONArray) currPar.get(ENTITY_ARY_KEY);
						JSONObject ancestorObj = getJSONObjHasID(currParEntAry, ansEntId);
						userCorefGroup = (String) ancestorObj.get("entityName");
						if (userCorefGroup == null) {
							userCorefGroup = "";
						}
					} catch(Exception e) {
						userCorefGroup = "";
					}
					break;
				}
			}
		}
		
		goldAncestorId = (String) goldAnswer.get(ANCESTOR_KEY);
		if(goldAncestorId.length() > 2)
			goldCorefGroup = (String) goldAnswer.get("entityName");
		
		boolean result = false;
		if (userAncestorId.equals("-1")) {
			result = (goldAncestorId.equals("-1"));
		} else if (userAncestorId.length() > 2){
			result = (goldAncestorId.length() > 2 && userCorefGroup.equals(goldCorefGroup));
		}
		
		return result;
	}
	
	private JSONObject getJSONObjHasID(JSONArray targetAry, int targetId) {
		for (int i=0; i<targetAry.size(); i++) {
			JSONObject currItem = (JSONObject)targetAry.get(i);
			if (((Long) currItem.get("id")).intValue() == targetId)
				return currItem;
		}
		return null;
	}
}
