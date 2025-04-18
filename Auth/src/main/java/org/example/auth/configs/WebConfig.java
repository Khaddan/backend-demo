/*package org.example.auth.configs;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilter() {
        FilterRegistrationBean<CharacterEncodingFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new CharacterEncodingFilter());
        filter.addInitParameter("encoding", "UTF-8");
        filter.addInitParameter("forceEncoding", "true");
        filter.addUrlPatterns("/*");
        return filter;
    }
}
*/
