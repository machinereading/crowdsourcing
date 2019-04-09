package edu.kaist.mrlab.cw.script;

import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ZAMention {

	public JSONObject getZARelations(String input) {

		JSONObject result = new JSONObject();
		KoreanAnalyzer ex = new KoreanAnalyzer();
		JSONArray resultArr = new JSONArray();
		
		try {
			String output = ex.getResult(input);
//			System.out.println(output);
			JSONParser parser = new JSONParser();
			JSONObject parsed = (JSONObject) parser.parse(output);
			JSONArray sentences = (JSONArray) parsed.get("sentence");
			Iterator<JSONObject> it = sentences.iterator();
			while(it.hasNext()) {
				JSONArray srlResultArr = new JSONArray();
				JSONObject sentence = it.next();
				JSONArray srl = (JSONArray) sentence.get("SRL");
				String text = sentence.get("text").toString();
				
				Iterator<JSONObject> srlIT = srl.iterator();
				while(srlIT.hasNext()) {
					
					boolean isContainsArg0 = false;
					
					JSONObject srlItem = srlIT.next();
					JSONArray srlTypes = (JSONArray) srlItem.get("argument");
					
					Iterator<JSONObject> typeIT = srlTypes.iterator();
					while(typeIT.hasNext()) {
						JSONObject typeItem = typeIT.next();
						String type = typeItem.get("type").toString();
						
						if(type.equals("ARG0")) {
							isContainsArg0 = true;
							break;
						}
						
					}
					
					if(!isContainsArg0) {
						JSONObject temp = new JSONObject();
						temp.put("verb", srlItem.get("verb"));
						temp.put("word_id", srlItem.get("word_id"));
						temp.put("ancestor", "");
						srlResultArr.add(temp);
					}
					
				}
				
				JSONObject rItem = new JSONObject();
				rItem.put("text", text.trim());
				rItem.put("verbs", srlResultArr);
				resultArr.add(rItem);
			}
			
			result.put("result", resultArr);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public static void main(String[] ar) {
		ZAMention zam = new ZAMention();
		String result = zam.getZARelations("생애. [1413년]에 [마리]는 [샤를_6세]와 [바이에른의_이자보]의 다섯 번째 아들인 [샤를_7세]과 약혼을 했다. [1422년] [4월]에 그녀는 [부르주]에서 그녀의 사촌이였던 [샤를]과 혼인하였고 이후 <프랑스>의 왕비가 되었다. [백년_전쟁]에서 남편의 승리는 [마리]의 가문에게서 받은 지원을 받은 덕이였으며, 특히 그녀의 어머니인 [욜란다_데_아라곤]이 그랬었다. [마리]와 [샤를]이 14명의 자녀를 두었음에도, 남편의 애정은 하녀인 [아녜스_소렐]에게 있었다.").toString();
		System.out.println(result);
	}
}
