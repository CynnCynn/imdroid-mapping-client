package com.imdroid.configuration;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:FastJson配置
 * @Author: iceh
 * @Date: create in 2018-09-06 00:55
 * @Modified By:
 */
@Configuration
public class FastJsonConfiguration {
    @Bean//使用@Bean注入fastJsonHttpMessageConvert
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        //1.需要定义一个Convert转换消息的对象
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        //2.添加fastjson的配置信息，比如是否要格式化返回的json数据
//
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat, SerializerFeature.WriteNonStringKeyAsString);
        //3.在convert中添加配置信息
        fastConverter.setFastJsonConfig(fastJsonConfig);
        // 中文乱码解决方案
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.valueOf("text/html;charset=UTF-8"));//设定html格式且编码为UTF-8
        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);//设定json格式且编码为UTF-8
        fastConverter.setSupportedMediaTypes(mediaTypes); //规则赋予转换对象
        fastConverter.setDefaultCharset(Charset.forName("UTF-8"));
//        HttpMessageConverter<?> converter = fastConverter;
        return new HttpMessageConverters(fastConverter);
    }
}
