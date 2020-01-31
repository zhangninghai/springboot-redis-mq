package cn.com.redis;
import cn.com.action.AESUtil;
import cn.com.ftp.FtpUtil;
import cn.com.ftp.SFTPUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/**
 * @Classname SubscribeListener
 * @Description 监听redis下载队列
 * @Date 2019/12/16 16:54
 * @Created by 张宁海
 */
@Component
public class SubdownloadListener implements Runnable {
    Logger logger= LoggerFactory.getLogger(getClass());
    @Autowired
    private PublishService service;
    @Autowired
    private Environment env;
    @Autowired
    private HttpService httpService;
    @Resource
    @Qualifier("StringRedisTemplate")
    RedisTemplate redisTemplate;


    @Override
    public void run() {
        logger.info("FILE_DOWNLOAD_PENDING监听开始");
        try {
            while (true){
                String request= (String) redisTemplate.opsForList().rightPop("FILE_DOWNLOAD_PENDING",60, TimeUnit.SECONDS);
                if(null==request||"".equals(request)||"null".equals(request)){
                    continue;
                }
                logger.info("FILE_DOWNLOAD_PENDING监听->{}",request);
                JSONObject jsonObject=JSONObject.parseObject(request);
                String merchantNumber=jsonObject.getString("");
                String bucketName=jsonObject.getString("");
                String taskId=jsonObject.getString("");
                SFTPUtil sftp=null;
                try {
                    String host=env.getProperty(merchantNumber+".host");//主机名
                    String port=env.getProperty(merchantNumber+".port");//端口
                    String type=env.getProperty(merchantNumber+".type");//连接类型
                    String user=env.getProperty(merchantNumber+".user");//用户名
                    String password=env.getProperty(merchantNumber+".password");//密码
                    String privateKey=env.getProperty(merchantNumber+".key");//私钥
                    String passphrase=env.getProperty(merchantNumber+".passphrase");//密钥
                    String directory=jsonObject.getString("directory");;//下载目录
                    String fileName=jsonObject.getString("fileName");//文件名
                    String fileId=jsonObject.getString("fileId");;//
                    String isTemp=jsonObject.getString("isTemp");//
                    logger.info("merchantNumber->{},user->{},host->{},port->{},privateKey->{},passphrase->{}",merchantNumber,user,host,port,privateKey,passphrase);
                    if("1".equals(type)){
                        if("".equals(password)||"null".equals(password)){
                            //密码为空走密钥连接
                            sftp=new SFTPUtil(user,host,Integer.parseInt(port),privateKey,passphrase);
                        }else {
                            //有密码则走密码连接
                            sftp=new SFTPUtil(user, AESUtil.decrypt(password, AESUtil.getAssetsDevPwdField()),host,Integer.parseInt(port));
                        }
                        sftp.login();
                        byte[] rest= sftp.download(directory,fileName);
                        response(taskId,fileId,isTemp,rest,"下载成功！",bucketName);
                    }else if("2".equals(type)){
                        byte[] rest=FtpUtil.downloadFile(host,Integer.parseInt(port),user,AESUtil.decrypt(password, AESUtil.getAssetsDevPwdField()),directory,fileName);
                        response(taskId,fileId,isTemp,rest,"下载成功！",bucketName);
                    }else if("3".equals(type)){
                        //走http请求协议
                        String url = jsonObject.getString("url");
                        String body = jsonObject.getString("body");
                        String connectType = jsonObject.getString("connectType");
                        byte[] rest=httpService.download(url,body,connectType);
                        response(taskId,fileId,isTemp,rest,rest.length>0?"下载成功!":"下载失败！",bucketName);
                    }
                    if(sftp!=null){
                        sftp.logout();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    logger.error(e.getMessage(),e);
                    try {
                        response(taskId,"","","".getBytes(),e.getMessage(),bucketName);
                    } catch (UnsupportedEncodingException ex) {
                        ex.printStackTrace();
                        logger.error(e.getMessage(),e);
                    }
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

    }
    /**
     * @Author 张宁海
     * @Description //发送下载队列
     * @Date 21:44 2019/12/17
     * @Param [TaskNumber, data]
     * @return void
     **/
    private void response(String TaskId,String fileId,String isTemp,byte[] data,String msg,String bucketName) throws UnsupportedEncodingException {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("TaskId",TaskId);
        jsonObject.put("data",data);
        jsonObject.put("fileId",fileId);
        jsonObject.put("isTemp",isTemp);
        jsonObject.put("msg",msg);
        jsonObject.put("bucketName",bucketName);
        logger.info("FILE_DOWNLOAD_DONE任务结束{}",jsonObject.toJSONString());
        service.publish("FILE_DOWNLOAD_DONE",jsonObject.toJSONString());
    }

}
