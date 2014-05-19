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
