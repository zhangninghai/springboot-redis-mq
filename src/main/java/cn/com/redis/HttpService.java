package cn.com.redis;

import cn.com.action.HttpClientUtils;
import org.springframework.stereotype.Component;

/**
 * @Classname HttpService
 * @Description TODO
 * @Date 2019/12/30 16:26
 * @Created by 张宁海
 */
@Component
public class HttpService {

    byte[] download(String url, String body,String connectType) {
        if ("get".equalsIgnoreCase(connectType)) {
            return HttpClientUtils.downloadGet(url);
        } else if ("post".equalsIgnoreCase(connectType)) {
            return HttpClientUtils.downloadPost(url, body);
        }
        return new byte[0];
    }

    Boolean  upload(String url, byte [] bytes,String fileName) {
        return HttpClientUtils.upload(url,bytes,fileName);
    }

}
