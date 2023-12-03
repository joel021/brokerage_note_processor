package com.api.calculator.stockprice.ws.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    public static Map<String, Object> postWithResp(String apiurl, String contentJsonFormat) throws IOException {

        URL url = new URL(apiurl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        if (contentJsonFormat != null){
            byte[] out = contentJsonFormat.getBytes(StandardCharsets.UTF_8);
            OutputStream stream = connection.getOutputStream();
            stream.write(out);
        }

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
    }
    public static int post(String apiurl, String contentJsonFormat) throws IOException {

        URL url = new URL(apiurl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        if (contentJsonFormat != null){
            byte[] out = contentJsonFormat.getBytes(StandardCharsets.UTF_8);
            OutputStream stream = connection.getOutputStream();
            stream.write(out);
        }
        int code = connection.getResponseCode();
        System.out.println("code:"+code);
        connection.getInputStream().close();
        connection.disconnect();

        return code;
    }
}
