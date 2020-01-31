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
import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * @Classname SubuploadListener
 * @Description 监听redis上传队列
 * @Date 2019/12/16 17:54
 * @Created by 张宁海
 */
@Component
public class SubuploadListener implements Runnable {
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
        logger.info("FILE_UPLOAD_PENDING监听开始");
        while (true){
            try {
                String request= (String) redisTemplate.opsForList().rightPop("FILE_UPLOAD_PENDING",60, TimeUnit.SECONDS);
                if(null==request||"".equals(request)||"null".equals(request)){
                    continue;
                }
                SFTPUtil sftp=null;
                logger.info("FILE_UPLOAD_PENDING监听,任务开始执行->{}",request);
                JSONObject jsonObject=JSONObject.parseObject(request);
                String merchantNumber=jsonObject.getString("merchantNumber");
                String taskId=jsonObject.getString("taskId");
                try {
                    String host=env.getProperty(merchantNumber+".host");
                    String port=env.getProperty(merchantNumber+".port");
                    String type=env.getProperty(merchantNumber+".type");
                    String user=env.getProperty(merchantNumber+".user");//用户名
                    String password=env.getProperty(merchantNumber+".password");//密码
                    String privateKey=env.getProperty(merchantNumber+".key");//私钥
                    String passphrase=env.getProperty(merchantNumber+".passphrase");//密钥
                    String directory=jsonObject.getString("directory");;//上传目录
                    String fileName=jsonObject.getString("fileName");//文件名
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
                        Boolean flag=sftp.upload(directory,fileName,new ByteArrayInputStream(jsonObject.getBytes("data")));
                        response(taskId,String.valueOf(flag),flag==true?"上传成功!":"上传失败!");
                    }else if("2".equals(type)){
                        Boolean flag=FtpUtil.uploadFile(host,Integer.parseInt(port),user,AESUtil.decrypt(password, AESUtil.getAssetsDevPwdField()),directory,fileName,new ByteArrayInputStream(jsonObject.getBytes("data")));
                        response(taskId,String.valueOf(flag),flag==true?"上传成功!":"上传失败!");
                    }else if("3".equals(type)){
                        //走http请求协议
                        String url = jsonObject.getString("url");//post请求的url
                        Boolean flag=httpService.upload(url,jsonObject.getBytes("data"),fileName);
                        response(taskId,String.valueOf(flag),flag==true?"上传成功!":"上传失败!");
                    }
                    if(sftp!=null){
                        sftp.logout();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    logger.error(e.getMessage(),e);
                    response(taskId,"false",e.getMessage());
                }
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }

        }
    }
    /**
     * @Author 张宁海
     * @Description //发送下载队列
     * @Date 21:44 2019/12/17
     * @Param [TaskNumber, data]
     * @return void
     **/
    private void response(String TaskId,String flag,String msg){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("TaskId",TaskId);
        jsonObject.put("data",flag);
        jsonObject.put("msg",msg);
        logger.info("FILE_UPLOAD_DONE任务结束{}",jsonObject.toJSONString());
        service.publish("FILE_UPLOAD_DONE",jsonObject.toJSONString());
    }
}
