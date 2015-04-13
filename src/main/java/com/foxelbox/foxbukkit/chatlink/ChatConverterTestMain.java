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

import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;

public class ChatConverterTestMain {
    public static void main(String[] args) {
        /*System.out.println(ChatMessageOut.convertLegacyColors(
                "\u00a7nMinecraft Formatting\n" +
                "\n" +
                "\u00a7r\u00a700 \u00a711 \u00a722 \u00a733\n" +
                "\u00a744 \u00a755 \u00a766 \u00a777\n" +
                "\u00a788 \u00a799 \u00a7aa \u00a7bb\n" +
                "\u00a7cc \u00a7dd \u00a7ee \u00a7ff\n" +
                "\n" +
                "\u00a7r\u00a70k \u00a7kMinecraft\n" +
                "\u00a7rl \u00a7lMinecraft\n" +
                "\u00a7rm \u00a7mMinecraft\n" +
                "\u00a7rn \u00a7nMinecraft\n" +
                "\u00a7ro \u00a7oMinecraft\n" +
                "\u00a7rr \u00a7rMinecraft"
        ));*/

        System.out.println(ChatMessageOut.convertLegacyColors("\u00a76Z\u00a75ido's \u00a75\u00a7dD\u00a7co\u00a76r\u00a7ei\u00a7ad\u00a7bi\u00a79a\u00a75n"));
    }
}
