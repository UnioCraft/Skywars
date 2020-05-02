package me.uniodex.skywars.enums;

import org.apache.commons.lang.StringEscapeUtils;

public enum SpecialCharacter {
    ARROW(StringEscapeUtils.unescapeJava("\u279D")), HEART(StringEscapeUtils.unescapeJava("\u2764")), STAR(StringEscapeUtils.unescapeJava("\u272A"));

    private String value;

    SpecialCharacter(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
