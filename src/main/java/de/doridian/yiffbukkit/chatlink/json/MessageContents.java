/**
 * This file is part of YiffBukkitChatLink.
 *
 * YiffBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * YiffBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with YiffBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.doridian.yiffbukkit.chatlink.json;

public class MessageContents {
    public MessageContents(String plain, String formatXML, String[] formatXMLArgs) {
        this.plain = plain;
        this.xml_format = formatXML;
        this.xml_format_args = formatXMLArgs;
    }

    public MessageContents(String plain) {
        this.plain = plain;
    }

    public String plain;
    public String xml_format;
    public String[] xml_format_args;
}
