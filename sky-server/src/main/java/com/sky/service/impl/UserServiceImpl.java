package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;


    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //调用微信接口，获取openid
        String openid = getOpenid(userLoginDTO.getCode());
        //判断openid是否为空，为空则登录失败，抛出业务异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前用户是否为新用户，是则自动完成注册
        User user = userMapper.getByOpenId(openid);
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            //新用户插入数据库
            userMapper.insert(user);
        }
        //返回登录成功的用户信息
        return user;
    }

    /**
     * 获取微信用户openid
     *
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        //组装请求参数
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("appid", weChatProperties.getAppid());
        queryParams.put("secret", weChatProperties.getSecret());
        queryParams.put("js_code", code);
        queryParams.put("grant_type", "authorization_code");

        //发送GET请求
        String json = HttpClientUtil.doGet(WX_LOGIN_URL, queryParams);
        //解析结果
        JSONObject jsonObject = JSONObject.parseObject(json);
        //获取openid
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
