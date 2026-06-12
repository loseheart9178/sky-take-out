package com.sky.utils;

import com.sky.constant.JwtClaimsConstant;
import com.sky.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类
 * 基于Spring的RequestContextHolder实现
 */
@Component
@Slf4j
public class UserContextUtil {

    private static JwtProperties jwtProperties;

    @Autowired
    public void setJwtProperties(JwtProperties jwtProperties) {
        UserContextUtil.jwtProperties = jwtProperties;
    }

    /**
     * 获取当前请求的用户ID（管理端员工）
     *
     * @return 员工ID
     */
    public static Long getCurrentEmployeeId() {
        if(jwtProperties == null){
            log.warn("jwtProperties未初始化");
            return null;
        }
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return null;
            }

            // 从请求头中获取token
            String token = request.getHeader(jwtProperties.getAdminTokenName());
            if (token == null || token.isEmpty()) {
                log.warn("未找到管理端token");
                return null;
            }

            // 解析token获取员工ID
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            return Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
        } catch (Exception e) {
            log.error("获取管理端员工ID失败", e);
            return null;
        }
    }

    /**
     * 获取当前请求的用户ID（用户端）
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return null;
            }

            // 从请求头中获取token
            String token = request.getHeader(jwtProperties.getUserTokenName());
            if (token == null || token.isEmpty()) {
                log.warn("未找到用户端token");
                return null;
            }

            // 解析token获取用户ID
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            return Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
        } catch (Exception e) {
            log.error("获取用户端用户ID失败", e);
            return null;
        }
    }

    /**
     * 获取当前请求的HttpServletRequest对象
     *
     * @return HttpServletRequest对象
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("无法获取请求上下文");
                return null;
            }
            return attributes.getRequest();
        } catch (Exception e) {
            log.error("获取HttpServletRequest失败", e);
            return null;
        }
    }

    /**
     * 获取当前请求的token（管理端）
     *
     * @return token字符串
     */
    public static String getCurrentToken() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return null;
            }
            return request.getHeader(jwtProperties.getAdminTokenName());
        } catch (Exception e) {
            log.error("获取管理端token失败", e);
            return null;
        }
    }

    /**
     * 获取当前请求的token（用户端）
     *
     * @return token字符串
     */
    public static String getCurrentUserToken() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return null;
            }
            return request.getHeader(jwtProperties.getUserTokenName());
        } catch (Exception e) {
            log.error("获取用户端token失败", e);
            return null;
        }
    }
}
