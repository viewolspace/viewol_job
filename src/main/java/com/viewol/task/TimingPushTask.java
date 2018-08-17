package com.viewol.task;

import com.viewol.pojo.ScheduleUser;
import com.viewol.service.IScheduleService;
import com.viewol.service.IWxService;
import com.viewol.util.ServiceFactory;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * 定时推送：定时给报名参加活动的用户推送消息提醒。活动开始前10分钟推送
 * 任务每隔一分钟扫描一次
 */
public class TimingPushTask {
    private static final Log logger = LogFactory.getLog("viewol_job");

    private IScheduleService scheduleService = ServiceFactory.getScheduleService();
    private IWxService wxService = ServiceFactory.getWxService();

    public void timePush(Map<String, String> paramMap) {
        String templateId = paramMap.get("templateId");
        String url = paramMap.get("url");
        logger.info("推送任务启动：templateId={},url={}", templateId, url);

        //1.查询需要提醒的记录
        List<ScheduleUser> needRemindList = scheduleService.queryNeedReminder();

        if(needRemindList == null || needRemindList.size() == 0){
            logger.info("暂无需要提醒的用户");
            return;
        }

        int success = 0;
        int error = 0;
        for(ScheduleUser scheduleUser : needRemindList){
            try{
                //2.调用微信接口发送提醒消息
                String msgId = wxService.sendTemplateMsg(scheduleUser.getScheduleId(), scheduleUser.getUserId(), scheduleUser.getUuid(), templateId, url);
                logger.info("消息推送：活动ID={},用户ID={},模板ID={},msgId={}", scheduleUser.getScheduleId(), scheduleUser.getUserId(), templateId, msgId);

                //3.更新数据提醒字段为“已提醒”
                if(msgId !=null && !"".equals(msgId) && !"-1".equals(msgId)){
                    scheduleService.updateReminderFlag(scheduleUser.getId());
                    success ++;
                } else {
                    error ++;
                }

            } catch (Exception e) {
                logger.error("提醒失败",e);
                error ++;
            }

        }


        logger.info("推送完成：成功推送={}，失败推送={}", success, error);
    }
}
