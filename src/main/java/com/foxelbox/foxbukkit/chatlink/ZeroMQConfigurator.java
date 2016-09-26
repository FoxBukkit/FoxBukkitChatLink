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

import java.util.HashMap;
import java.util.HashSet;

public class ZeroMQConfigurator {
    public static void parseZeroMQConfig(String config, ZMQ.Socket socket, String serviceType, String serviceName) {
        String[] values = config.split(";");
        for(int i = 0; i < values.length; i += 2) {
            String cValue = values[i + 1];
            switch(values[i].toLowerCase()) {
                case "connect":
                    socket.connect(cValue);
                    break;
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

    private HashMap<String, HashSet<String>> registeredHosts = new HashMap<>();

    public ZeroMQConfigurator(ZMQ.Socket _socket, String serviceType, final String serviceName) {
        this.socket = _socket;
    }
}
