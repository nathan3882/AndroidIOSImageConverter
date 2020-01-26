package me.nathan3882.svgtosizedpngconverter.svglogic;

public enum AttributeName {

    WIDTH("width"),
    HEIGHT("height");


    private final String pretty;

    AttributeName(String pretty) {
        this.pretty = pretty;
    }

    public String getPretty() {
        return pretty;
    }
}
