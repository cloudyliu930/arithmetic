package com.cloudy;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashingWithoutVirtualNode {

    // 服务器地址
    private static String[] servers = {"127.0.0.1:80", "127.0.0.2:80", "127.0.0.3:80", "127.0.0.4:80"};

    // 真是服务器节点，考虑到上下线服务器，这里用linkedlist
    private static List<String> realNodes = new LinkedList<String>();

    // 虚拟节点，key表示hash值，value表示虚拟节点
    private static SortedMap<Integer, String> virtualNodes = new TreeMap<Integer, String>();

    // 单服务器虚拟节点数
    private static final int VIRTUAL_NODES = 5;

    static {
        for (int i = 0; i < servers.length; i++) {
            realNodes.add(servers[i]);
        }

        for (String node : realNodes) {
            for (int i1 = 0; i1 < VIRTUAL_NODES; i1++) {
                // 虚拟节点名称
                String virtualNodeName = node + "&&VN" + String.valueOf(i1);
                int hash = getHash(virtualNodeName);
                System.out.println("虚拟节点：[" + virtualNodeName + "]被添加，hash值为：" + hash);
                virtualNodes.put(hash, virtualNodeName);
            }
        }
    }

    //使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

    // 获取应当路由到的server
    private static String getServer(String key) {
        // 获取hash值
        int hash = getHash(key);
        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
        String virtualNode;
        if (subMap.isEmpty()) {
            // 如果没有比当前hash大的数据，那么取第一个节点
            Integer i = virtualNodes.firstKey();
            virtualNode = virtualNodes.get(i);
        } else {
            // 第一个key是顺时针过去离node最近的一个节点
            Integer i = subMap.firstKey();
            virtualNode = subMap.get(i);
        }

        if (StringUtils.isNotEmpty(virtualNode)) {
            return virtualNode.substring(0, virtualNode.indexOf("&&"));
        }
        return null;
    }

    public static void main(String[] args) {
        String[] keys = {"令妃", "富查", "娴妃"};
        for (int i = 0; i < keys.length; i++) {
            System.out.println("[" + keys[i] + "]的hash值为：" + getHash(keys[i]) + "，被路由到：[" + getServer(keys[i]) + "]");
        }
    }

}