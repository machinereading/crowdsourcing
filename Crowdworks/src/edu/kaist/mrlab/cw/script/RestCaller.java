package edu.kaist.mrlab.cw.script;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestCaller {
	
	public String callKA(String inputText) {

        String output = null;

        try {

            Client client = Client.create();

            WebResource webResource = client.resource("http://143.248.135.20:31235/etri_parser");
            
            String input = "{\"text\": \"" + inputText + "\"}";

            ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            // System.out.println("Output from Server .... \n");
            output = response.getEntity(String.class);

        } catch (Exception e) {

            e.printStackTrace();

        }
        return output;

    }

	public String callKoEL(String inputText) {

        String output = null;

        try {

            Client client = Client.create();

            WebResource webResource = client.resource("http://143.248.135.60:2229/entity_linking");
            
            String input = inputText;

            ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            // System.out.println("Output from Server .... \n");
            output = response.getEntity(String.class);

        } catch (Exception e) {

            e.printStackTrace();

        }
        return output;

    }
	
	public static void main(String[] ar) throws Exception {
		RestCaller rc = new RestCaller();
		String result = rc.callKA("어니스트 헤밍웨이는 미국에서 태어났다.");
		System.out.println(result);
//		JSONParser parser = new JSONParser();
//		JSONArray object = (JSONArray) parser.parse(result);
//		System.out.println(object.toJSONString());
		System.out.println(rc.callKoEL(result));
	}
	
	
}
