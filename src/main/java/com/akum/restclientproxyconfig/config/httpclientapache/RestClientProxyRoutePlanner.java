package com.akum.restclientproxyconfig.config.httpclientapache;


import com.akum.restclientproxyconfig.config.utils.ProxyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
public class RestClientProxyRoutePlanner extends DefaultProxyRoutePlanner {
  private final Set<String> noProxyHosts;

  public RestClientProxyRoutePlanner(HttpHost proxy, Set<String> noProxyHosts) {
    super(proxy);
    this.noProxyHosts = noProxyHosts;
  }

  @Override
  public HttpHost determineProxy(HttpHost targetHost, HttpContext context) throws HttpException {
    if (CollectionUtils.isEmpty(noProxyHosts))
      return super.determineProxy(targetHost, context);
    String hostname = targetHost.getHostName();
    List<String> ipAddresses = Arrays.stream(ProxyUtils.parseAddress(hostname))
        .map(InetAddress::getHostAddress)
        .toList();

    boolean undetermined = noProxyHosts.stream()
        .anyMatch(nonProxyHost -> ipAddresses.stream()
            .anyMatch(ip -> (ProxyUtils.matchesPattern(ip, nonProxyHost) || ProxyUtils.matchesPattern(hostname, nonProxyHost))));

    return undetermined ? null : super.determineProxy(targetHost, context);
  }


}
