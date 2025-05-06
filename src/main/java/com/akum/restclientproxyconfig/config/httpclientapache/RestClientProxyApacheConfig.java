package com.akum.restclientproxyconfig.config.httpclientapache;

import com.akum.restclientproxyconfig.config.common.HttpProxyProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Configuration
public class RestClientProxyApacheConfig {

  @Autowired
  private HttpProxyProperties httpProxyProperties;

  @Bean
  public RestClient.Builder restClientHCBuilder() {
    RestClient.Builder builder = RestClient.builder();
    if (Objects.isNull(httpProxyProperties) || !StringUtils.hasText(httpProxyProperties.getProxyHost())) {
      log.info("No proxy configuration found in application configuration or errors, using default configuration!");
      return builder;
    }

    log.info("Proxy configuration found in application configuration: {}", httpProxyProperties);
    Set<String> noProxyHosts = new HashSet<>(Arrays.asList(httpProxyProperties.getNonProxyHosts().split("[|,;]")));
    HttpHost proxy = new HttpHost(httpProxyProperties.getProxyHost(), httpProxyProperties.getProxyPort());

    DefaultProxyRoutePlanner routePlanner = new RestClientProxyRoutePlanner(proxy, noProxyHosts);

    CredentialsProvider credentialsProvider = CredentialsProviderBuilder.create()
        .add(new AuthScope(proxy), httpProxyProperties.getProxyUser(), httpProxyProperties.getProxyPassword().toCharArray())
        .build();

    AuthCache authCache = new BasicAuthCache();
    BasicScheme basicAuth = new BasicScheme();
    authCache.put(proxy, basicAuth);
    HttpClientContext context = HttpClientContext.create();
    context.setCredentialsProvider(credentialsProvider);
    context.setAuthCache(authCache);

    HttpClient httpClient = HttpClients.custom()
        .setRoutePlanner(routePlanner)
        .setDefaultCredentialsProvider(credentialsProvider)
        .build();

    builder.requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
    return builder;
  }
}