package com.oasis.ocrspring.config;

import com.oasis.ocrspring.annotations.Authenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer
{
    @Autowired
    private Authenticator authenticator;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry)
    {
        registry.addInterceptor(this.authenticator).addPathPatterns("/api/test/**");
    }

}
