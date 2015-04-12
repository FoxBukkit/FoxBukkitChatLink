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
package com.foxelbox.foxbukkit.chatlink.json;

import com.foxelbox.foxbukkit.chatlink.util.Utils;

import java.util.UUID;

public class ChatMessageOut {
    public ChatMessageOut(String server, UserInfo from) {
        this.server = server;
        this.from = from;
        this.to = new MessageTarget("all", null);
        this.context = UUID.randomUUID();
    }

    private static String[] xmlEscapeArray(String[] in) {
        final String[] out = new String[in.length];
        for(int i = 0; i < in.length; i++)
            out[i] = Utils.XMLEscape(in[i]);
        return out;
    }

    public ChatMessageOut(ChatMessageIn messageIn, String formatXML, String[] formatXMLArgs) {
        this(messageIn);
        setContents(formatXML, formatXMLArgs);
    }

    public ChatMessageOut(ChatMessageIn messageIn) {
        this(messageIn.server, messageIn.from);
        this.context = messageIn.context;
    }

    public void setContents(String formatXML, String[] formatXMLArgs) {
        this.contents = String.format(formatXML, xmlEscapeArray(formatXMLArgs));
    }

    public String server;
    public UserInfo from;
    public MessageTarget to;

    public long timestamp = System.currentTimeMillis() / 1000;

    public UUID context;
    public boolean finalize_context = false;
    public String type = "text";

    public int importance = 0;

    public String contents;
}
