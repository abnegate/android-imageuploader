package com.jakebarnby.imageuploader.models;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by jake on 1/18/17.
 */

public class SourceHTTPRequest {
    private String mApiBaseUrl;
    private String mAccessToken;

    public SourceHTTPRequest(String apiBaseUrl) {
        mAccessToken = "";
        mApiBaseUrl = apiBaseUrl;
    }

    public SourceHTTPRequest(String apiBaseUrl, String accessToken) {
        mAccessToken = accessToken;
        mApiBaseUrl = apiBaseUrl;
    }

    /**
     * Create http request to an api endpoint.
     * This is a synchronus method, call it on a separate thread.
     *
     * @param method   HTTP method, can be GET or POST
     * @param endpoint Api endpoint.
     * @param params   Request parameters
     * @return Api response in json format.
     * @throws Exception If error occured.
     */
    public String createRequest(String method, String endpoint, List<NameValuePair> params) throws Exception {
        if (method.equals("POST")) {
            return requestPost(endpoint, params);
        } else {
            return requestGet(endpoint, params);
        }
    }

    /**
     * Create http request to an api endpoint.
     * This is an asynchronous method, so you have to define a listener to handle the result.
     *
     * @param method   Http method, can be GET or POST
     * @param endpoint Api endpoint.
     * @param params   Request parameters
     * @param listener Request listener
     */
    public void createRequest(String method, String endpoint, List<NameValuePair> params, SourceHTTPRequestListener listener) {
        new RequestTask(method, endpoint, params, listener).execute();
    }

    /**
     * Create http GET request to an api endpoint.
     *
     * @param endpoint Api endpoint.
     * @param params   Request parameters
     * @return Api response in json format.
     * @throws Exception If error occured.
     */
    public String requestGet(String endpoint, List<NameValuePair> params) throws Exception {
        String requestUri = mApiBaseUrl + ((endpoint.indexOf("/") == 0) ? endpoint : "/" + endpoint);

        return get(requestUri, params);
    }

    /**
     * Create http POST request to an api endpoint.
     *
     * @param endpoint Api endpoint.
     * @param params   Request parameters
     * @return Api response in json format.
     * @throws Exception If error occured.
     */
    private String requestPost(String endpoint, List<NameValuePair> params) throws Exception {
        String requestUri = mApiBaseUrl + ((endpoint.indexOf("/") == 0) ? endpoint : "/" + endpoint);

        return post(requestUri, params);
    }

    /**
     * Create http GET request to an api endpoint.
     *
     * @param requestUri Api url
     * @param params     Request parameters
     * @return Api response in json format.
     * @throws Exception If error occured.
     */
    public String get(String requestUri, List<NameValuePair> params) throws Exception {
        InputStream stream = null;
        String response = "";

        try {
            String requestUrl = requestUri;

            if (!mAccessToken.equals("")) {
                if (params == null) {
                    params = new ArrayList<NameValuePair>(1);
                    params.add(new BasicNameValuePair("access_token", mAccessToken));
                } else {
                    params.add(new BasicNameValuePair("access_token", mAccessToken));
                }
            }

            if (params != null) {
                StringBuilder requestParamSb = new StringBuilder();
                int size = params.size();

                for (int i = 0; i < size; i++) {
                    BasicNameValuePair param = (BasicNameValuePair) params.get(i);

                    requestParamSb.append(param.getName() + "=" + param.getValue() + ((i != size - 1) ? "&" : ""));
                }

                String requestParam = requestParamSb.toString();

                requestUrl = requestUri + ((requestUri.contains("?")) ? "&" + requestParam : "?" + requestParam);
            }

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(requestUrl);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity == null) {
                throw new Exception("Request returns empty result");
            }

            stream = httpEntity.getContent();
            response = streamToString(stream);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new Exception(httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return response;
    }

    /**
     * Create http POST request to an api endpoint
     *
     * @param requestUrl Api url
     * @param params     Request parameters
     * @return Api response in json format.
     * @throws Exception If error occured.
     */
    public String post(String requestUrl, List<NameValuePair> params) throws Exception {
        InputStream stream = null;
        String response = "";

        try {
            if (!mAccessToken.equals("")) {
                if (params == null) {
                    params = new ArrayList<>(1);
                    params.add(new BasicNameValuePair("access_token", mAccessToken));
                } else {
                    params.add(new BasicNameValuePair("access_token", mAccessToken));
                }
            }
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestUrl);
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity == null) {
                throw new Exception("Request returns empty result");
            }

            stream = httpEntity.getContent();
            response = streamToString(stream);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                Log.d("REQUEST_URL: ", requestUrl);
                throw new Exception(httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            throw e;
        }
        return response;
    }

    private class RequestTask extends AsyncTask<URL, Integer, Long> {
        String method, endpoint, response = "";
        List<NameValuePair> params;
        SourceHTTPRequestListener listener;

        public RequestTask(String method, String endpoint, List<NameValuePair> params, SourceHTTPRequestListener listener) {
            this.method = method;
            this.endpoint = endpoint;
            this.params = params;
            this.listener = listener;
        }

        protected void onCancelled() {
        }

        protected void onPreExecute() {
        }

        protected Long doInBackground(URL... urls) {
            long result = 0;

            try {
                if (method.equals("POST")) {
                    response = requestPost(endpoint, params);
                } else {
                    response = requestGet(endpoint, params);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            if (!response.equals("")) {
                listener.onSuccess(response);
            } else {
                listener.onError("Failed to process api request");
            }
        }
    }

    //Request listener
    public interface SourceHTTPRequestListener {
        public abstract void onSuccess(String response);
        public abstract void onError(String error);
    }

    /**
     * Builds a reponse string from an inputstream
     * @param is    The input steam to parse
     * @return      The response
     * @throws IOException
     */
    private String streamToString(InputStream is) throws IOException {
        String str  = "";

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader 	= new BufferedReader(new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }
            str = sb.toString();
        }
        return str;
    }
}
