package com.company;

public class Token {
    private final String item;
    private final String itemClass;
    private final String tokenLength;
    private final String commonStartPoint;
    private final String linePoint;

    Token(String name, String className, int start, int end, int common, int line) {
        item=name;
        tokenLength = (end - start) + "";
        itemClass=className;
        linePoint = line +"";
        commonStartPoint=common + "";
    }

    public String getItem() {
        return item;
    }

    public String getLinePoint() {
        return linePoint;
    }

    public String getItemClass() {
        return itemClass;
    }

    public String getTokenLength() {
        return tokenLength;
    }

    public String getCommonStartPoint() {
        return commonStartPoint;
    }
}
