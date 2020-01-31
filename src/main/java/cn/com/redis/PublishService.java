package cn.com.redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @Classname PublishService
 * @Description TODO
 * @Date 2019/12/16 16:57
 * @Created by 张宁海
 */
@Component
public class PublishService {
   @Resource
   @Qualifier("StringRedisTemplate")
   RedisTemplate redisTemplate;
    @Autowired
    private SubdownloadListener subdownloadListener;
    @Autowired
    private SubuploadListener subuploadListener;

    /**
     * 发布方法
     * @param channel 消息发布订阅 主题
     * @param message 消息信息
     */
    public void publish(String channel, Object message) {
        redisTemplate.opsForList().leftPush(channel,message);
    }
    @PostConstruct
    void init(){
        new Thread(subuploadListener).start();
        new Thread(subdownloadListener).start();
    }
}
