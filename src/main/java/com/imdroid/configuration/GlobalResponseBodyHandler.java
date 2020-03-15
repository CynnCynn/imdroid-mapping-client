package com.imdroid.configuration;

import com.imdroid.pojo.bo.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @Description:全局返回信息处理类
 * @Author: iceh
 * @Date: create in 2018-11-26 16:42
 * @Modified By:
 */


@Slf4j
@ControllerAdvice
public class GlobalResponseBodyHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        // 根据业务和上面各个参数判断，是否执行下面的beforeBodyWrite方法，返回true则执行，否则不执行
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object obj, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        Method method = methodParameter.getMethod();
        boolean isCollectionEmpty = obj instanceof Collection && ((Collection) obj).isEmpty();
        boolean isMapEmpty = obj instanceof Map && ((Map) obj).isEmpty();
        //由于异常消息部分只将
        if (method.getName().contains("ExceptionHandler")) {
            return new ResponseResult(false, obj.toString(), null, null);
        }
        //返回结果不能为空或空串
        else if (null == obj || "".equals(obj) || isCollectionEmpty || isMapEmpty) {
            //返回结果为空的方法
            String declaringMethod = method.getDeclaringClass() + "." + method.getName();
            //要打印的信息
            StringBuffer logMsg = new StringBuffer();
            logMsg.append("业务异常").append("\r\n").append(declaringMethod).append("\r\n").append("requestParameter={");
            //通过request获取参数内容
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest();
            Map<String, String[]> parameterMap = servletRequest.getParameterMap();
            for (String key : parameterMap.keySet()) {
                logMsg.append(key).append(":");
                for (String value : parameterMap.get(key)) {
                    logMsg.append(value);
                }
            }
            log.error(logMsg.append("}").toString());
            serverHttpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseResult(false, "返回值为空，请检查请求参数是否有误", null, obj);
        }
        return obj;
    }

}
