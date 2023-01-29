package com.cws.scheduled;

import com.cws.utils.PushUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Task 定时任务
 *
 * @author cws
 * @date 2022/8/22 21:42
 */
@EnableScheduling
@Configuration
public class Task {
    // 定时任务
    @Scheduled(cron = "${wechat.cron}")
    public void goodMorning() {
        PushUtil.push();
    }
}
