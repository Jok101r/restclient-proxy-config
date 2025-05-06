package com.akum.restclientproxyconfig.config.httpclientnet;

import com.akum.restclientproxyconfig.config.common.HttpProxyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.util.Objects;

@Slf4j
@Configuration
public class RestClientProxyNetConfig {

  @Autowired
  private HttpProxyProperties httpProxyProperties;

  @Value("${spring.application.myservice.user:}")
  private String accessToServerUsername;
  @Value("${spring.application.myservice.password:}")
  private String accessToServerPassword;

  @Bean
  public RestClient.Builder restClientNetBuilder() {

    RestClient.Builder builder = RestClient.builder();

    if (Objects.isNull(httpProxyProperties) || !StringUtils.hasText(httpProxyProperties.getProxyHost())) {
      log.info("No proxy configuration found in application configuration or errors, using default configuration!");
      return builder;
    }

    System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
    System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

    Authenticator authenticator = new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        if (getRequestorType() == RequestorType.PROXY) {
          return new PasswordAuthentication(httpProxyProperties.getProxyUser(), httpProxyProperties.getProxyPassword().toCharArray());
        }
        if (getRequestorType() == RequestorType.SERVER) {
          return new PasswordAuthentication(accessToServerUsername, accessToServerPassword.toCharArray());
        }
        return null;
      }
    };

    HttpClient httpClient = HttpClient.newBuilder()
        .proxy(new CustomProxySelector(httpProxyProperties))
        .authenticator(authenticator)
        .build();

    builder.requestFactory(new JdkClientHttpRequestFactory(httpClient));
    return builder;
  }
}

