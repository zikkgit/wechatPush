package com.cws.utils;

import com.cws.DateUtils.LunarCalendarFestivalUtils;
import com.cws.configure.PushConfigure;
import com.cws.pojo.Result;
import com.cws.pojo.Weather;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpTemplateMsgService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;

/**
 * PushUtil
 *
 * @author cws
 * @date 2022/8/22 21:40
 */
public class PushUtil {

    private static WxMpTemplateMsgService wxService = null;

    /**
     * 消息推送主要业务代码
     */
    public static String push() {
        // 构建模板消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .templateId(PushConfigure.getTemplateId())
                .build();
        // 计算天数
        long loveDays = MemoryDayUtil.calculationLianAi(PushConfigure.getLoveDate());
        long birthdays = 0;
        if (PushConfigure.isUseLunar()) {
            // 如果使用农历生日
            birthdays = MemoryDayUtil.calculationBirthdayByLunar(PushConfigure.getBirthday());
        } else {
            birthdays = MemoryDayUtil.calculationBirthday(PushConfigure.getBirthday());
        }
        templateMessage.addData(new WxMpTemplateData("loveDays", loveDays + "", "#FF1493"));
        templateMessage.addData(new WxMpTemplateData("birthdays", birthdays + "", "#FFA500"));

        // 获取天气数据
        Result weatherResult = WeatherUtil.getWeather();
        StringBuilder messageAll = new StringBuilder();
        if (!"0".equals(weatherResult.getCode())) {
            messageAll.append("<br/>");
            messageAll.append(weatherResult.getMessage());
            templateMessage.addData(new WxMpTemplateData("weather", "***", "#00FFFF"));
        } else {
            Weather weather = (Weather) weatherResult.getData();
            // 拿到农历日期
            LunarCalendarFestivalUtils festival = new LunarCalendarFestivalUtils();
            festival.initLunarCalendarInfo(weather.getDate());

            templateMessage.addData(new WxMpTemplateData("date", weather.getDate() + "  " + weather.getWeek(), "#00BFFF"));
            templateMessage.addData(new WxMpTemplateData("lunar", "农历" + festival.getLunarYear() + "年 " + festival.getLunarMonth() + "月" + festival.getLunarDay(), "#00BFFF"));
            templateMessage.addData(new WxMpTemplateData("festival", festival.getLunarTerm() + " " + festival.getSolarFestival() + " " + festival.getLunarFestival(), "#00BFFF"));
            templateMessage.addData(new WxMpTemplateData("weather", weather.getText_now(), "#00FFFF"));
            templateMessage.addData(new WxMpTemplateData("low", weather.getLow() + "", "#173177"));
            templateMessage.addData(new WxMpTemplateData("temp", weather.getTemp() + "", "#EE212D"));
            templateMessage.addData(new WxMpTemplateData("wc_day", weather.getWc_day() + "", "#EE212D"));
            templateMessage.addData(new WxMpTemplateData("wd_day", weather.getWd_day() + "", "#EE212D"));
            templateMessage.addData(new WxMpTemplateData("high", weather.getHigh() + "", "#FF6347"));
            templateMessage.addData(new WxMpTemplateData("city", weather.getCityName() + "", "#173177"));
        }

        // 天行数据接口
        Result rainbowResult = RainbowUtil.getRainbow();
        if (!"200".equals(rainbowResult.getCode())) {
            messageAll.append("<br/>");
            messageAll.append(rainbowResult.getMessage());
        } else {
            templateMessage.addData(new WxMpTemplateData("rainbow", (String) rainbowResult.getData(), "#FF69B4"));
        }
        // 备注
        String remark = "❤";
        if (loveDays % 365 == 0) {
            remark = "\n今天是恋爱" + (loveDays / 365) + "周年纪念日!";
        }
        if (birthdays == 0) {
            remark = "\n今天是生日,生日快乐呀!";
        }
        if (loveDays % 365 == 0 && birthdays == 0) {
            remark = "\n今天是生日,也是恋爱" + (loveDays / 365) + "周年纪念日!";
        }
        templateMessage.addData(new WxMpTemplateData("remark", remark, "#FF1493"));


        System.out.println(templateMessage.toJson());

        // 拿到service
        WxMpTemplateMsgService service = getService();

        int suc = 0;
        int err = 0;
        for (String userId : PushConfigure.getUserId()) {
            templateMessage.setToUser(userId);
            try {
                service.sendTemplateMsg(templateMessage);
                suc += 1;
            } catch (WxErrorException e) {
                err += 1;
                messageAll.append(suc).append("个成功!");
                messageAll.append(err).append("个失败!");
                messageAll.append("<br/>");
                messageAll.append(e.getMessage());
                return "推送结果:" + messageAll;
            }
        }

        return "成功推送给" + suc + "个用户!" + messageAll;
    }

    /**
     * 获取 WxMpTemplateMsgService
     *
     * @return WxMpTemplateMsgService
     */
    private static WxMpTemplateMsgService getService() {
        if (wxService != null) {
            return wxService;
        }
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        wxStorage.setAppId(PushConfigure.getAppId());
        wxStorage.setSecret(PushConfigure.getSecret());
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);
        wxService = wxMpService.getTemplateMsgService();
        return wxService;
    }
}
