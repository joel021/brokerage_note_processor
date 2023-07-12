package com.api.calculator.stockprice.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_FORM_ENCODED = "application/x-www-form-urlencoded";

    public static Map<String, Object> postWithResp(String apiurl, String contentJsonFormat, String contentType){

        try{
            URL url = new URL(apiurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Accept", "application/json");

            if (contentJsonFormat != null){
                byte[] out = contentJsonFormat.getBytes(StandardCharsets.UTF_8);
                OutputStream stream = connection.getOutputStream();
                stream.write(out);
            }
            //connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()));

            int code = connection.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connection.disconnect();
            Map<String, Object> resp = new ObjectMapper().readValue(response.toString(), HashMap.class);
            resp.put("code", code);

            return resp;
        }catch (Exception i){
            System.out.println("\n\nerror on post: ");
            System.out.println(i.getMessage());
        }
        return null;
    }
    public static void post(String apiurl, String contentJsonFormat, String contentType){

        try{
            URL url = new URL(apiurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Accept", "application/json");

            if (contentJsonFormat != null){
                byte[] out = contentJsonFormat.getBytes(StandardCharsets.UTF_8);
                OutputStream stream = connection.getOutputStream();
                stream.write(out);
            }
            connection.getResponseCode();
            connection.getInputStream();
            connection.disconnect();

        }catch (Exception i){
            System.out.println("\n\nerror on post: ");
            System.out.println(i.getMessage());
        }
    }
}
