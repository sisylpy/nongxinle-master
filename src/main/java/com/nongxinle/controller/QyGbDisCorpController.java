package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-02 22:59
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.WeChatSuiteReturn;
import com.nongxinle.utils.*;
import com.nongxinle.utils.aes.WXBizMsgCrypt;
import com.nongxinle.utils.aes.WechatConstants;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.QyGbDisCorpEntity;
import com.nongxinle.service.QyGbDisCorpService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.DateUtils.formatWhatFullTime;


@RestController
@RequestMapping("api/qygbdiscorp")
public class QyGbDisCorpController {
	@Autowired
	private QyGbDisCorpService qyGbDisCorpService;


	@RequestMapping(value = "/tryGroup", method = RequestMethod.POST)
	@ResponseBody
	public R tryGroup(final HttpServletRequest request,
					  @RequestParam(name = "msg_signature") final String sMsgSignature,
					  @RequestParam(name = "timestamp") final String sTimestamp,
					  @RequestParam(name = "nonce") final String sNonce,
					  HttpServletResponse response, String data) {

		try {
			InputStream inputStream = request.getInputStream();
			String sPostData = IOUtils.toString(inputStream, "UTF-8");
			Map<String, String> stringStringMap = MessageUtil.parseXml(sPostData);
			String toUserName = stringStringMap.get("ToUserName");
			System.out.println("callbackrxtoUserNametoUserName===" + toUserName);
			QywechatEnum qywechatEnum = QywechatEnum.JXPPSX;
			qywechatEnum.setCorpid(toUserName);
			QywechatInfo qywechatInfo = new QywechatInfo();
			qywechatInfo.setMsgSignature(sMsgSignature);
			qywechatInfo.setNonce(sNonce);
			qywechatInfo.setQywechatEnum(qywechatEnum);
			qywechatInfo.setTimestamp(sTimestamp);
			qywechatInfo.setSPostData(sPostData);
			WXBizMsgCrypt msgCrypt = new WXBizMsgCrypt(qywechatInfo.getQywechatEnum());
			String sMsg = msgCrypt.decryptMsg(qywechatInfo);
			Map<String, String> dataMap = MessageUtil.parseXml(sMsg);
			System.out.println("callbackrx回调信息：=========new" + dataMap);

			if(dataMap.get("InfoType") != null){
				if (dataMap.get("InfoType").equals("suite_ticket")) {
					System.out.println("saveTokenenSXXXX----------");
					saveSuiteTokenSx(dataMap);
				} else if (dataMap.get("InfoType").equals("create_auth")) {
					updateAuthTokenSx(dataMap);
				}else {
					System.out.println("callbackrx!== null and else====" + dataMap.get("InfoType"));
				}
			}
			if(dataMap.get("Event") != null){
				System.out.println("Event====Event=====" + dataMap.get("Event"));
			}

			try {
				PrintWriter writer = response.getWriter();
				writer.write("success");
				writer.flush();

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String ddd = "ECnP9bsrzB5Xa55710m1plfhOS8B5NKevOcDoSeKyfTb-4zmW_QQY4cbtMeuNgyy72kjnNKUsGAjQQbn-J9naaJ2yx8b1nthlIddqLaNjMEZ8FvlUpiOqQME1l3PiZJRukqKbQGdNfP3AGryWKh1wYgF3KiWMNRJVdXIA1ktmnVkYQBskoXyKmSlOvxwwq-cILNWT9gTccKxyBB3YVglWQ";
		String userCropUrl = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/list?suite_access_token=" + ddd;
		Map<String, Object> map = new HashMap<>();
		map.put("limit", "10");
		String body = HttpRequest.post(userCropUrl).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
		System.out.println("authTokenUrlfanhuibody===SSSXXXXXXXX" + body);
		JSONObject jsonObject = JSONObject.parseObject(body);

		// 发送请求，返回Json字符串// 转成Json对象 获取openid
		System.out.println("jsonObjectjsonObject" + jsonObject);

	    return R.ok().put("data", jsonObject);
	}

	public void updateAuthTokenSx(Map<String, String> mapSuite) {
		System.out.println("saveAutSsssXxxx");
		// 获取永久授权码
		String suiteToken = getWxProperty(Constant.SUITE_TOKEN_SX);
		String authTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/get_permanent_code?suite_access_token=" + suiteToken;
		System.out.println("rult" + authTokenUrl);
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("auth_code", mapSuite.get("AuthCode"));
			String body = HttpRequest.post(authTokenUrl).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
			System.out.println("authTokenUrlfanhuibody===SSSXXXXXXXX" + body);
			JSONObject corpInfo = JSONObject.parseObject(body);
			String permanent_code = corpInfo.getString("permanent_code");
			String auth_corp_info = corpInfo.getString("auth_corp_info");
			System.out.println("whatisthisSSSSSXXXXX");
			JSONObject jsonObject = JSONObject.parseObject(auth_corp_info);
			System.out.println("diercengSXSSXXXX" + jsonObject);

			String corp_name = jsonObject.getString("corp_name");
			String corpid = jsonObject.getString("corpid");
			String corp_round_logo_url = jsonObject.getString("corp_round_logo_url");
			//
			Map<String, Object> mapCorp= new HashMap<>();
			mapCorp.put("auth_corpid", corpid);
			mapCorp.put("permanent_code", permanent_code);
			String coprTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/get_corp_token?suite_access_token=" + suiteToken;

			String bodyCorp = HttpRequest.post(coprTokenUrl).body(JSONUtil.toJsonStr(mapCorp), ContentType.JSON.getValue()).execute().body();
			System.out.println("mapCorpmapCorpmapCorpmapCorpmapCorp===" + bodyCorp);
			JSONObject corpInfoCorp = JSONObject.parseObject(bodyCorp);
			String access_token = corpInfoCorp.getString("access_token");


			QyGbDisCorpEntity qyNxDisCorpEntity  = qyGbDisCorpService.queryQyCropByCropId(corpid);
			System.out.println("serachaupcocccooccoidididididi----SSXX" + qyNxDisCorpEntity);

			qyNxDisCorpEntity.setQyGbDisCorpAccessToken(access_token);
			qyNxDisCorpEntity.setQyGbDisCorpPermanentCode(permanent_code);
			qyNxDisCorpEntity.setQyGbDisCorpJoinDate(formatWhatFullTime(0));
			System.out.println("updteSqlok-----");
			qyGbDisCorpService.update(qyNxDisCorpEntity);
		} catch (Exception e) {
			System.out.println("woromd");
			throw new RuntimeException();
		}

	}

	public void saveSuiteTokenSx(Map<String, String> mapSuite) {
		System.out.println("gettokenekenekeneSSSXXXXXXXXX");
		// 获取第三方应用凭证url
		String suiteTokenUrl = Constant.THIRD_BUS_WECHAT_SUITE_TOKEN;
		// 	第三方应用access_token
		String suiteToken = "";
		try {
			Map<String, Object> map = new HashMap<>();
			//以ww或wx开头应用id
			map.put("suite_id", Constant.SuiteIDSX);
			//应用secret
			map.put("suite_secret", Constant.SuiteSecretSx);
			//企业微信后台推送的ticket
			map.put("suite_ticket", mapSuite.get("SuiteTicket"));
			String body = HttpRequest.post(suiteTokenUrl).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
			System.out.println("ceshisaveTokenenneenneSSSXXXXXX@@@22222" + body);
			WeChatSuiteReturn weChat = JSONUtil.toBean(body, WeChatSuiteReturn.class);
			if (weChat.getErrcode() == null || weChat.getErrcode() == 0) {
				// 保存suiteToken
				suiteToken = weChat.getSuite_access_token();
				saveWxProperty(Constant.SUITE_TOKEN_SX, suiteToken);
			}
			// 打印消息
			System.out.println("获取suite token成功:" + suiteToken);
		} catch (Exception e) {
			System.out.println("获取suite token失败errcode:" + suiteToken);
			throw new RuntimeException();
		}

	}


	private String getWxProperty(String key) {
		Properties pps = new Properties();
		InputStream is = QyWxControlloer.class.getClassLoader().getResourceAsStream("wx.properties");//假设当前这个方法是在CommonUtils类下面
		try {
			pps.load(is);
			String value = pps.getProperty(key);
			System.out.println(key + " = " + value);
			is.close();
			System.out.println("getWxProperty---------------" + key + "===" + pps.get(key));
			return value;

		} catch (IOException e) {
			e.printStackTrace();
			return "-1";
		}

	}

	private void saveWxProperty(String key, String value) {
		System.out.println("savekkekekeykey====" + key + "value==" + value);
		PropertiesConfiguration configuration = null;
		try {
			configuration = new PropertiesConfiguration("wx.properties");
			configuration.setProperty(key, value);
			configuration.save();
			System.out.println("condarrrr" + configuration.getKeys());
			System.out.println("saveWxProperty---------------" + key + "===" + configuration.getString(key));
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

	}



	
}
