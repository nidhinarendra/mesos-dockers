package org.systemsoftware.mesosraspberry;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Anushavijay on 8/1/16.
 */


public class SendInfo {

    public static JSONObject getJSON(String endpointURL) throws IOException {
        StringBuilder result=new StringBuilder();
        URL url = new URL(endpointURL);
        HttpURLConnection getconn = (HttpURLConnection) url.openConnection();
        getconn.setRequestMethod("GET");
        BufferedReader buffer = new BufferedReader(new InputStreamReader(getconn.getInputStream()));
        String line;
        while ((line = buffer.readLine()) != null) {
            result.append(line);
        }
        buffer.close();
        JSONObject jsonObj = new JSONObject(result.toString());

        getconn.disconnect();
        return jsonObj;

    }


    public static String postInfo(String jsonBody,String postUrl) throws IOException {

        //Posting infomation to the server to update database

        URL serverUrl = new URL(postUrl);
        System.out.println("Printing JSON BODY"+jsonBody);

        HttpURLConnection httpCon = (HttpURLConnection) serverUrl.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setDoInput(true);
        httpCon.setRequestMethod("POST");
        httpCon.setRequestProperty("Content-Type", "application/json");
        OutputStream os = httpCon.getOutputStream();
        os.write(jsonBody.getBytes());
        os.flush();


        // Retriving message from server

        StringBuilder serverOutput = new StringBuilder();
        int HttpResult = httpCon.getResponseCode();
        System.out.println(httpCon.getResponseCode());
        if (HttpResult == HttpURLConnection.HTTP_OK || HttpResult==HttpURLConnection.HTTP_BAD_REQUEST) {
            BufferedReader serverBuffer = new BufferedReader(
                    new InputStreamReader(httpCon.getInputStream(), "utf-8"));
            System.out.println("buffering thing");
            String output = null;
            while ((output = serverBuffer.readLine()) != null) {
                serverOutput.append(output + "\n");
            }
            serverBuffer.close();
            System.out.println("ServerOutput"+serverOutput.toString());
        } else {
            System.out.println("This is the system's response code"+ httpCon.getResponseMessage());
        }


        httpCon.disconnect();
       return serverOutput.toString();
    }
}



 /*









 */