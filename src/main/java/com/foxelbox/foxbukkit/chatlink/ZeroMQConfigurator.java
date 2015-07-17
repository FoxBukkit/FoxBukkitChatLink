/**
 * This file is part of FoxBukkitChatLink.
 *
 * FoxBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.foxbukkit.chatlink;

import org.zeromq.ZMQ;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class ZeroMQConfigurator {
    public static void parseZeroMQConfig(String config, ZMQ.Socket socket, String serviceType, String serviceName) {
        String[] values = config.split(";");
        for(int i = 0; i < values.length; i += 2) {
            String cValue = values[i + 1];
            switch(values[i].toLowerCase()) {
                case "connect":
                    socket.connect(cValue);
                    break;
                case "announce":
                    int port; boolean fallthrough = true;
                    if(cValue.equalsIgnoreCase("random")) {
                        port = socket.bindToRandomPort("tcp://*");
                        fallthrough = false;
                    } else {
                        port = Integer.parseInt(cValue);
                    }
                    announceService(serviceType, serviceName, port);
                    if(!fallthrough) {
                        break;
                    }
                    cValue = "tcp://*:" + port;
                case "bind":
                    socket.bind(cValue);
                    break;
            }
        }
    }

    public static String getDefaultConfig(String mode, int port) {
        return mode + ";tcp://*:" + port;
    }

    private final ZMQ.Socket socket;

    private static String formatDNS(String serviceType, String serviceName) {
        return "_" + serviceName + "-" + serviceType + "._tcp.local.";
    }

    private static final ArrayList<ServiceInfo> services = new ArrayList<>();

    public static void shutdown() {
        for(ServiceInfo info : services) {
            jmDNS.unregisterService(info);
        }
        jmDNS.unregisterAllServices();;
        try {
            jmDNS.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void announceService(final String serviceType, final String serviceName, final int port) {
        new Thread() {
            public void run() {
                final String type = formatDNS(serviceType, serviceName);
                try {
                    final ServiceInfo info = ServiceInfo.create(type, "n" + port, port, "me");
                    synchronized (services) {
                        services.add(info);
                    }
                    jmDNS.registerService(info);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

    private static final JmDNS jmDNS;
    static {
        try {
            jmDNS = JmDNS.create(InetAddress.getLocalHost(), UUID.randomUUID().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, HashSet<String>> registeredHosts = new HashMap<>();

    public ZeroMQConfigurator(ZMQ.Socket _socket, String serviceType, final String serviceName) {
        this.socket = _socket;

        final String type = formatDNS(serviceType, serviceName);
        jmDNS.addServiceListener(type, new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent serviceEvent) {

            }

            @Override
            public void serviceRemoved(ServiceEvent serviceEvent) {
                HashSet<String> hosts = registeredHosts.get(serviceEvent.getName());
                if(hosts == null) {
                    return;
                }
                for(String addr : hosts) {
                    System.out.println("ZeroMQ: DISCONNECT: " + type + ": " + addr);
                    socket.disconnect(addr);
                }
            }

            @Override
            public void serviceResolved(ServiceEvent serviceEvent) {
                HashSet<String> hosts = registeredHosts.get(serviceEvent.getName());
                if(hosts == null) {
                    hosts = new HashSet<>();
                    registeredHosts.put(serviceEvent.getName(), hosts);
                }
                final int port = serviceEvent.getInfo().getPort();
                for(InetAddress address : serviceEvent.getInfo().getInetAddresses()) {
                    String addr = "tcp://" + address.getHostAddress() + ":" + port;
                    hosts.add(addr);
                    System.out.println("ZeroMQ: CONNECT: " + type + ": " + addr);
                    socket.connect(addr);
                }
            }
        });
    }
}
