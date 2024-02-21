package org.onedatashare.server.config;

import org.onedatashare.server.controller.IndexFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexFilterConfig {
    @Bean
    public FilterRegistrationBean<IndexFilter> indexFilterConfigBean() {
        FilterRegistrationBean<IndexFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new IndexFilter());
        return registrationBean;
    }
}
