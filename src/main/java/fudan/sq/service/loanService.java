package fudan.sq.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.validation.Valid;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Map;
import java.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class loanService {
    public String postCardAuthenticationJson(String url, @Valid String param) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 1.获取URLConnection对象对应的输出流
            // out = new PrintWriter(conn.getOutputStream());
            // 2.中文有乱码的需要将PrintWriter改为如下
            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            // 发送请求参数
            // out.print(param);
            out.write(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            InputStreamReader isr = new InputStreamReader(
                    conn.getInputStream(), "UTF-8");

            in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }

        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // System.out.println("post推送结果："+result);
        return result;
    }
    public static String sendGet(String url,String param)

    {
        String result = "";

        try{
            String urlName = url + "?"+param;//
            System.out.println("url:"+urlName);

            URL U = new URL(urlName);

            URLConnection connection = U.openConnection();

            connection.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;

            while ((line = in.readLine())!= null)

            {
                result += line;

            }

            in.close();

        }catch(Exception e){
            System.out.println("没有结果！"+e);

        }

        return result;

    }
    public static String doPut(String strUrl,String param){
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
                System.out.println("1");

            } finally {
                response1.close();

            }

        }catch(Exception e){
            e.printStackTrace();

        }

        return jsonString.toString();

    }

    public void insertLoan(String url,String loginToken,String accountNum,int balance,String branchNum,int contractAmount,String contractNum,String createTime,String dueDate){
        doPut(url,"login-token="+loginToken+"&accountNum="+accountNum+"&balance="+balance+"&branchNum="+branchNum+"&contractAmount="+contractAmount+"&contractNum="+contractNum+"&createTime="+createTime+"&dueDate="+dueDate);

    }









}





