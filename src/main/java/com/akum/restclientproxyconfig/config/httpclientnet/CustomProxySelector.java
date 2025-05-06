package com.akum.restclientproxyconfig.config.httpclientnet;

import com.akum.restclientproxyconfig.config.common.HttpProxyProperties;
import com.akum.restclientproxyconfig.config.utils.ProxyUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class CustomProxySelector extends ProxySelector {

  private final Set<String> noProxyHosts;
  private final ProxySelector defaultSelector;

  CustomProxySelector(HttpProxyProperties httpProxyProperties) {
    noProxyHosts = new HashSet<>(Arrays.asList(httpProxyProperties.getNonProxyHosts().split("[|,;]")));
    defaultSelector = ProxySelector.of(new InetSocketAddress(httpProxyProperties.getProxyHost(), Integer.parseInt(httpProxyProperties.getProxyPort())));
  }

  @Override
  public List<Proxy> select(URI uri) {
    String hostname = uri.getHost();
    String ipAddress = ProxyUtils.parseAddress(uri.getHost()).getHostAddress();

    boolean isExcluded = noProxyHosts.stream()
        .anyMatch(pattern ->
            ProxyUtils.matchesPattern(ipAddress, pattern) || ProxyUtils.matchesPattern(hostname, pattern));

    if (isExcluded) {
      log.info("Exclusion from proxy for {}", ipAddress);
      return List.of(Proxy.NO_PROXY);
    }
    return defaultSelector.select(uri);
  }

  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    defaultSelector.connectFailed(uri, sa, ioe);
  }
}
