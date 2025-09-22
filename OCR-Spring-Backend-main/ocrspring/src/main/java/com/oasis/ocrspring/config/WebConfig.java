package com.oasis.ocrspring.config;

import com.oasis.ocrspring.annotations.Authenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer
{
    @Autowired
    private Authenticator authenticator;

    @Value("${uploadDir}")
    private String uploadDir;

    @Value("${reportUploadDir}")
    private String reportUploadDir;

    @Value("${consentFormUploadDir}")
    private String consentFormUploadDir;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry)
    {
        registry.addInterceptor(this.authenticator).addPathPatterns("/v3/test/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from all upload directories under /files/
        // This allows all file types to be served from their respective directories
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadDir + "/",
                                    "file:" + reportUploadDir + "/",
                                    "file:" + consentFormUploadDir + "/");
    }

}
