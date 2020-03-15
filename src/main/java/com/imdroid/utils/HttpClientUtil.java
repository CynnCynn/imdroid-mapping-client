package com.imdroid.utils;

import com.alibaba.fastjson.JSON;
import com.imdroid.pojo.bo.BusinessException;
import com.imdroid.pojo.bo.ResponseResult;
import com.imdroid.programSelfStart.statusNotificationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.imdroid.pojo.bo.Const.ServerAddress;

@Slf4j
public class HttpClientUtil {
    /**
     * 上传文件，用multipart/form_data的头
     *
     * @param url
     * @param file
     */
    public static boolean uploadFile(String url, File file) {
        //构建HttpClient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //构建POST请求
        HttpPost httpPost = new HttpPost(url);
        //构建文件体
        FileBody fileBody = new FileBody(file, ContentType.MULTIPART_FORM_DATA);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity httpEntity = builder.build();
        httpPost.setEntity(httpEntity);
        try {
            //发送请求
            HttpResponse response = client.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String result = EntityUtils.toString(resEntity);
                ResponseResult responseResult = JSON.parseObject(result, ResponseResult.class);
                if (responseResult.isSuccess()) {
                    log.info("上传文件成功");
                    return true;
                } else {
                    throw new BusinessException("上传文件失败，失败原因:" + responseResult.getErrorMsg());
                }
            }
        } catch (IOException e) {
            log.error("上传文件失败", e);
        }
        return false;
    }

    public static String doGet(String url, Map<String, String> param) {
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = null;
        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, param.get(key));
                }
            }
            URI uri = builder.build();
            // 创建http GET请求
            httpGet = new HttpGet(uri);
            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            log.error("发送get请求失败", e);
        } finally {
            try {
                httpGet.abort();
                EntityUtils.consumeQuietly(response.getEntity());
                if (response != null) {
                    response.close();
                }
                httpclient.close();
            } catch (IOException e) {
                log.error("发送get请求失败", e);
            }
        }
        return resultString;
    }

    public static String doGet(String url) {
        return doGet(url, null);
    }

    public static String doPost(String url, Map<String, String> param) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建参数列表
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList<NameValuePair>();
                for (String key : param.keySet()) {
                    paramList.add(new BasicNameValuePair(key, param.get(key)));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.error("发送post请求失败", e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                log.error("发送post请求失败", e);
            }
        }

        return resultString;
    }

    public static String doPost(String url) {
        return doPost(url, null);
    }

    public static String doPostJson(String url, String json) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.error("发送post请求失败", e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                log.error("发送post请求失败", e);
            }
        }

        return resultString;
    }


    /**
     * 检查是否有网络
     *
     * @return
     */
    public static boolean isServerNormal() {
        boolean serverNormal = false;
        try {
//            TODO: 检查连接网络是否成功
            String address = ServerAddress.TEST;
            URL url = new URL(address);
            InputStream inputStream = url.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "/n");
            }
            if (sb.length() > 0) {
                serverNormal = true;
            }
        } catch (IOException e) {
            statusNotificationUtil.updateStatus(211);
            log.warn("连接服务器失败");
        }
        return serverNormal;
    }


}
