package fudan.sq.httpUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import com.google.gson.Gson;
import fudan.sq.service.Token;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class httpUtils {
    public static final Gson gson = new Gson();
    /***
     * httpClient-Get请求
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static Map<String, Object> httpClientGet(String url) throws Exception {
        HttpClient client = new HttpClient();
        client.getParams().setContentCharset("UTF-8");
        GetMethod httpGet = new GetMethod(url);
        httpGet.setRequestHeader("login-token", Token.token);
        try {
            client.executeMethod(httpGet);
            String response = httpGet.getResponseBodyAsString();
            Map<String, Object> map = gson.fromJson(response, Map.class);
            return map;
        } catch (Exception e) {
            throw e;
        } finally {
            httpGet.releaseConnection();
        }
    }

    /***
     * httpClient-Post请求
     * @param url 请求地址
     * @param params post参数
     * @return
     * @throws Exception
     */
    public static Map<String, Object> httpClientPost(String url, String params) throws Exception {
        HttpClient client = new HttpClient();
        client.getParams().setContentCharset("UTF-8");
        PostMethod httpPost = new PostMethod(url);
        try {
            RequestEntity requestEntity = new ByteArrayRequestEntity(params.getBytes("utf-8"));
            httpPost.setRequestEntity(requestEntity);
            client.executeMethod(httpPost);
            String response = httpPost.getResponseBodyAsString();
            Map<String, Object> map = gson.fromJson(response, Map.class);
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            httpPost.releaseConnection();
        }
    }


    /**
     * put方法
     * */
    public static Map<String,Object> doPut(String strUrl,String param){
        CloseableHttpClient httpclient = HttpClients.createDefault();

        StringBuffer jsonString= new StringBuffer();

        try {
            final HttpPut put=new HttpPut(strUrl);

            put.setEntity(new StringEntity(param,"UTF-8"));

            CloseableHttpResponse response1= httpclient.execute(put );

            try {
                HttpEntity entity1 = response1.getEntity();

                BufferedReader br = new BufferedReader(new InputStreamReader(entity1.getContent()));

                String line;

                while ((line = br.readLine()) != null) {
                    jsonString.append(line);

                }

                EntityUtils.consume(entity1);

            } finally {
                response1.close();

            }

        }catch(Exception e){
            e.printStackTrace();

        }
        Map<String,Object> res = gson.fromJson(jsonString.toString(),Map.class);
        return res;
    }

}
