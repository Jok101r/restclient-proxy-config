package com.akum.restclientproxyconfig.config.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ProxyUtils {
  public static boolean matchesPattern(String input, String pattern) {
    if (pattern.contains("/")) {
      return isInSubnet(input, pattern);
    }

    String regex = pattern
        .replace(".", "\\.")
        .replace("*", ".*");

    return input.matches(regex);
  }

  public static InetAddress[] parseAddress(String address) {
    try {
      return InetAddress.getAllByName(address);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException(String.format("Failed to parse address %s", address), e);
    }
  }

  private static boolean isInSubnet(String ip, String cidr) {
    if (!cidr.contains("/"))
      return cidr.equals(ip);

    String[] parts = cidr.split("/");
    String subnetBase = parts[0];
    int prefixLength = Integer.parseInt(parts[1]);

    byte[] ipBytes = parseAddress(ip)[0].getAddress();
    byte[] subnetBytes = parseAddress(subnetBase)[0].getAddress();

    int fullBytes = prefixLength / 8;
    int remainingBits = prefixLength % 8;

    for (int i = 0; i < fullBytes; i++) {
      if (ipBytes[i] != subnetBytes[i]) return false;
    }

    if (remainingBits > 0) {
      int mask = (0xFF00 >> remainingBits) & 0xFF;
      return (ipBytes[fullBytes] & mask) == (subnetBytes[fullBytes] & mask);
    }

    return true;
  }
}
