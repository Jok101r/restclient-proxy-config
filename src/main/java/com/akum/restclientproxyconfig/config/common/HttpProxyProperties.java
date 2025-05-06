package com.akum.restclientproxyconfig.config.common;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@ToString
@Configuration
public class HttpProxyProperties {
  @Value("${http.proxyHost:}")
  private String proxyHost;
  @Value("${http.proxyPort:}")
  private String proxyPort;
  @Value("${http.proxyUser:}")
  private String proxyUser;
  @Value("${http.proxyPassword:}")
  private String proxyPassword;
  @Value("${http.nonProxyHosts:}")
  private String nonProxyHosts;
}
