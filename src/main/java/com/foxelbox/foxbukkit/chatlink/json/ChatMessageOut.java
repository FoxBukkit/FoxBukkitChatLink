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

import com.foxelbox.dependencies.redis.RedisManager;
import com.foxelbox.foxbukkit.chatlink.Main;
import com.foxelbox.foxbukkit.chatlink.RedisHandler;
import com.foxelbox.foxbukkit.chatlink.util.Utils;

import java.util.*;
import java.util.regex.Pattern;

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
            out[i] = convertLegacyColors(Utils.XMLEscape(in[i]));
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

    public void setContentsPlain(String plain) {
        this.contents = convertLegacyColors(Utils.XMLEscape(plain));
    }

    private static final char COLOR_CHAR = '\u00a7';
    private static final Pattern FIX_REDUNDANT_TAGS = Pattern.compile("<([a-z]+)[^>]*>(\\s*)</\\1>", Pattern.CASE_INSENSITIVE);
    private static final Map<Character, String> colorNames = new HashMap<>();
    static {
        colorNames.put('0', "black");
        colorNames.put('1', "dark_blue");
        colorNames.put('2', "dark_green");
        colorNames.put('3', "dark_aqua");
        colorNames.put('4', "dark_red");
        colorNames.put('5', "dark_purple");
        colorNames.put('6', "gold");
        colorNames.put('7', "gray");
        colorNames.put('8', "dark_gray");
        colorNames.put('9', "blue");
        colorNames.put('a', "green");
        colorNames.put('b', "aqua");
        colorNames.put('c', "red");
        colorNames.put('d', "light_purple");
        colorNames.put('e', "yellow");
        colorNames.put('f', "white");
    }

    public static String convertLegacyColors(String in) {
        StringBuilder out = new StringBuilder("<color name=\"white\">");

        int lastPos = 0; char currentColor = 'f';

        Set<String> openTagsSet = new HashSet<>();
        Stack<String> openTags = new Stack<>();
        openTagsSet.add("color");
        openTags.push("color");

        while(true) {
            int pos = in.indexOf(COLOR_CHAR, lastPos);
            if(pos < 0) {
                if(lastPos == 0) {
                    return in;
                }
                break;
            }
            char newColor = in.charAt(pos + 1);

            if(pos > 0) {
                out.append(in.substring(lastPos, pos));
            }

            lastPos = pos + 2;

            if((newColor >= '0' && newColor <= '9') || (newColor >= 'a' && newColor <= 'f') || newColor == 'r') {

                boolean doesNotChangeColor = newColor == 'r' || currentColor == newColor;

                while(!openTags.empty()) {
                    String tag = openTags.pop();
                    if(doesNotChangeColor && tag.equals("color")) {
                        continue;
                    }
                    out.append("</");
                    out.append(tag);
                    out.append('>');
                }
                openTagsSet.clear();

                openTagsSet.add("color");
                openTags.push("color");

                if(doesNotChangeColor) {
                    continue;
                }

                out.append("<color name=\"");
                out.append(colorNames.get(newColor));
                out.append("\">");

                currentColor = newColor;
            } else {
                switch (newColor) {
                    case 'l':
                        if(!openTagsSet.contains("b")) {
                            openTags.push("b");
                            openTagsSet.add("b");
                            out.append("<b>");
                        }
                        break;
                    case 'm':
                        if(!openTagsSet.contains("s")) {
                            openTags.push("s");
                            openTagsSet.add("s");
                            out.append("<s>");
                        }
                        break;
                    case 'n':
                        if(!openTagsSet.contains("u")) {
                            openTags.push("u");
                            openTagsSet.add("u");
                            out.append("<u>");
                        }
                        break;
                    case 'o':
                        if(!openTagsSet.contains("i")) {
                            openTags.push("i");
                            openTagsSet.add("i");
                            out.append("<i>");
                        }
                        break;
                }
            }
        }

        if(lastPos < in.length()) {
            out.append(in.substring(lastPos));
        }
        while(!openTags.empty()) {
            String tag = openTags.pop();
            out.append("</");
            out.append(tag);
            out.append('>');
        }

        return FIX_REDUNDANT_TAGS.matcher(out.toString()).replaceAll("$2");
    }

    public String server;
    public UserInfo from;
    public MessageTarget to;

    public long timestamp = System.currentTimeMillis() / 1000;
    public long id = Main.redisManager.incrBy("lastMessageID", 1);

    public UUID context;
    public boolean finalize_context = false;
    public String type = "text";

    public int importance = 0;

    public String contents;
}
