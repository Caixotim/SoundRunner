package com.example.goncalomatos.spotifysdktest;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestHttpTask extends AsyncTask<String, String, String> {

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String responseString = null;
        try {
            URL url = new URL(params[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                responseString = readStream(in);
            } catch (IOException e) {
                //TODO Handle problems..
            } finally {
                urlConnection.disconnect();
            }
        }catch(Exception e){ //not pretty
            System.out.println(e);
        }
        return responseString;
    }

}
