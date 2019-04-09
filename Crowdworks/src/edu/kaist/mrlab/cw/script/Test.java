package edu.kaist.mrlab.cw.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Test {
	
	public static void main(String[] ar) throws Exception {
		
		String str = "[Tip] 정확하게 이야기하면, '[알리_사미_옌_경기장] '은 [갈라타사라이_SK_(축구)] 의 옛 경기장이며 2011년부터는 새롭게 설립한 '튀르크 텔리콤 아레나'를 홈구장으로 사용하고 있다. 경기장의 이름이 바뀐 이유는 [터키] 의 이동통신 회사인 튀르크 텔레콤이 매년 1,025만 달러를 지급하는 조건으로 10년 동안 경기장 명명권을 취득했기 때문이다.";
		String e1 = "갈라타사라이_SK_(축구)";
		str = str.replace("[" + e1 + "]", "<e1>" + e1 + "</e1>");
		System.out.println(str);
		
	}
}
