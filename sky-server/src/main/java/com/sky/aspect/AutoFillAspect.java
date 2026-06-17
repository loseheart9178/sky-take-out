package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.enumeration.OperationType;
import com.sky.utils.RequestContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class AutoFillAspect {

    /*
    * 切入点
    * */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && " +
            "@annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /*
    * 前置通知，进行公共字段赋值
    * */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段填充");
        //1.获取当前方法注解上的数据库操作类型

        //1.1获取方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //1.2获取方法上的注解对象
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        //1.3获取数据库操作类型
        OperationType operationType = autoFill.value();
        //获取方法参数
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object object = args[0];

        //准备赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = RequestContextUtil.getCurrentEmployeeId();
        //根据对应的数据库操作类型，为对应的字段赋值
        if(operationType == OperationType.INSERT){
            //获取实体对象的set方法
            Method setCreateTime = object.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = object.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = object.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateUser = object.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            //调用方法，为对应的字段赋值
            setCreateTime.invoke(object, now);
            setUpdateTime.invoke(object, now);
            setCreateUser.invoke(object, currentId);
            setUpdateUser.invoke(object, currentId);
        } else if (operationType == OperationType.UPDATE) {
            Method setUpdateTime = object.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = object.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateTime.invoke(object, now);
            setUpdateUser.invoke(object, currentId);
        }
    }
}
