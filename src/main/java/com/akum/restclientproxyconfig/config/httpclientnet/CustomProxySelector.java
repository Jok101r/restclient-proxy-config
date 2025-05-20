package com.akum.restclientproxyconfig.config.httpclientnet;

import com.akum.restclientproxyconfig.config.common.HttpProxyProperties;
import com.akum.restclientproxyconfig.config.utils.ProxyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class CustomProxySelector extends ProxySelector {

  private final Set<String> noProxyHosts;
  private final ProxySelector defaultSelector;

  CustomProxySelector(HttpProxyProperties httpProxyProperties) {
    noProxyHosts = Arrays.stream(httpProxyProperties.getNonProxyHosts().split("[|,;]"))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .collect(Collectors.toSet());
    defaultSelector = ProxySelector.of(new InetSocketAddress(httpProxyProperties.getProxyHost(), Integer.parseInt(httpProxyProperties.getProxyPort())));
  }

  @Override
  public List<Proxy> select(URI uri) {
    String hostname = uri.getHost();
    List<String> ipAddresses = Arrays.stream(ProxyUtils.parseAddress(hostname))
        .map(InetAddress::getHostAddress)
        .toList();

    boolean undetermined = noProxyHosts.stream()
        .anyMatch(nonProxyHost -> ipAddresses.stream()
            .anyMatch(ip -> (ProxyUtils.matchesPattern(ip, nonProxyHost) || ProxyUtils.matchesPattern(hostname, nonProxyHost))));

    if (undetermined) {
      log.info("Exclusion from proxy for {}", ipAddresses);
      return List.of(Proxy.NO_PROXY);
    }
    return defaultSelector.select(uri);
  }

  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    defaultSelector.connectFailed(uri, sa, ioe);
  }
}
