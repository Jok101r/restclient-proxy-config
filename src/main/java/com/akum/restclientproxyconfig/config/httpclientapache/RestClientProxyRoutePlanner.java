package com.akum.restclientproxyconfig.config.httpclientapache;


import com.akum.restclientproxyconfig.config.utils.ProxyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;

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
    String hostname = targetHost.getHostName();
    String ipAddress = ProxyUtils.parseAddress(targetHost.getHostName()).getHostAddress();

    boolean isExcluded = noProxyHosts.stream()
        .anyMatch(pattern ->
            ProxyUtils.matchesPattern(ipAddress, pattern) || ProxyUtils.matchesPattern(hostname, pattern));

    if (isExcluded) {
      log.info("Exclusion from proxy for {}", ipAddress);
      return null;
    }
    return super.determineProxy(targetHost, context);
  }


}
