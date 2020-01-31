package cn.com.action;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * java类简单作用描述
 *
 * @ProjectName: walkincq
 * @Package: com.example.cq.util
 * @Description: java类作用描述
 * @Author: 张宁海
 * @create: 2018-12-08 16:18
 * <p>Copyright: Copyright (c) 2018</p>
 */
public class HttpClientUtils<main> {
    private static String charSet = "UTF-8";
    private static CloseableHttpClient httpClient = null;
    private static CloseableHttpResponse response = null;





    /**
     * @Author 张宁海
     * @Description //文件流上传到服务端
     * @Date 15:08 2019/12/30
     * @Param [url, bytes, fileName]
     * @return java.lang.String
     **/
   public static Boolean upload(String url, byte [] bytes,String fileName){
        try {
            if ("https".equalsIgnoreCase(getUrl(url))) {
                httpClient = SSLClientCustom.createSSLClientDefault();
            } else {
                httpClient = HttpClients.createDefault();
            }
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            /*绑定文件参数，传入文件流和contenttype，此处也可以继续添加其他formdata参数*/
            builder.addBinaryBody("file",new ByteArrayInputStream(bytes), ContentType.MULTIPART_FORM_DATA,fileName);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);

            response = httpClient.execute(httpPost);
            if (response != null) {
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    return true;
                }else {
                    return false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }


    /**
     * @Author 张宁海
     * @Description 文件流get下载
     * @Date 15:53 2019/12/30
     * @Param [url, fileName]
     * @return byte[]
     **/
    public static byte [] downloadGet(String url){
       try {
           if ("https".equalsIgnoreCase(getUrl(url))) {
               httpClient = SSLClientCustom.createSSLClientDefault();
           } else {
               httpClient = HttpClients.createDefault();
           }
           //发送get请求

           HttpGet httpGet = new HttpGet(url);
           response = httpClient.execute(httpGet);
           if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
               InputStream in=response.getEntity().getContent();
               return IOUtils.toByteArray(in);
           }
       }catch (Exception e){
        e.printStackTrace();
       }finally {
           if (httpClient != null) {
               try {
                   httpClient.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
           if (response != null) {
               try {
                   response.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }

       return new byte[0];
    }
    /**
     * @Author 张宁海
     * @Description 文件下载post
     * @Date 16:06 2019/12/30
     * @Param [url]
     * @return byte[]
     **/
    public static byte [] downloadPost(String url,String jsonStr){
        try {
            if ("https".equalsIgnoreCase(getUrl(url))) {
                httpClient = SSLClientCustom.createSSLClientDefault();
            } else {
                httpClient = HttpClients.createDefault();
            }
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json;Charset=utf-8");
            httpPost.setHeader(HTTP.CONTENT_ENCODING,"UTF-8");
            if(!"".equals(jsonStr)&&null!=jsonStr){
                StringEntity se = new StringEntity(jsonStr, charSet);
                se.setContentType("application/json;Charset=utf-8");
                se.setContentEncoding(new BasicHeader("Content-Type", "application/json;Charset=utf-8"));
                httpPost.setEntity(se);
            }
            response = httpClient.execute(httpPost);
            if (response != null) {
                InputStream inputStream = response.getEntity().getContent();
                return IOUtils.toByteArray(inputStream);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new byte[0];
    }



    /**
     * https的post请求
     *
     * @param url
     * @return
     */
    public static String doHttpsPost(String url, String jsonStr) {
        try {
            httpClient = SSLClientCustom.createSSLClientDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json;Charset=utf-8");
            StringEntity se = new StringEntity(jsonStr, charSet);
            se.setContentType("application/json;Charset=utf-8");
            se.setContentEncoding(new BasicHeader("Content-Type", "application/json;Charset=utf-8"));
            httpPost.setEntity(se);

            response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    return EntityUtils.toString(resEntity, charSet);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * http  post   请求
     *
     * @param reqURL
     * @param data
     * @return
     */
    public static String sendPostRequest(String reqURL, String data) {
        String result = "";
        // 创建HttpClientBuilder
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        // HttpClient
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        // 依次是代理地址，代理端口号，协议类型
        //HttpHost proxy = new HttpHost("192.168.4.28", 9080, "http");
        //RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
        // 请求地址
        HttpPost httpPost = new HttpPost(reqURL);
        httpPost.setConfig(config);

        try {
            StringEntity entity = new StringEntity(data, charSet);
            entity.setContentType("application/json;Charset=utf-8");
            httpPost.setEntity(entity);
            CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
            //status= response.getStatusLine().getStatusCode()+"";
            result = EntityUtils.toString(response.getEntity(), charSet);
            closeableHttpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * @param url
     * @param param
     * @param sslflag true发起https的get请求 false发起http的get请求
     * @return
     */
    public static String doHttpGet(String url, Map<String, String> param, Boolean sslflag) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;

        try {
            if (sslflag == true) {
                httpclient = SSLClientCustom.createSSLClientDefault();
            } else {
                httpclient = HttpClients.createDefault();
            }
            if (param != null && !param.isEmpty()) {
                //参数集合
                List<NameValuePair> getParams = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> entry : param.entrySet()) {
                    getParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(getParams), "UTF-8");
            }
            //发送gey请求
            HttpGet httpGet = new HttpGet(url);
            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    static String getUrl(String str) throws MalformedURLException, URISyntaxException {
        URL url = new URL(str);
        URI uri = url.toURI();
        System.out.println();
        return uri.getScheme();
    }


}
