package me.nathan3882.svgtosizedpngconverter;

import lombok.Getter;

@Getter
public enum TransformerType {

    IOS("iOS Images"),
    ANDROID("Android Images");

    private final String pretty;

    TransformerType(String pretty) {
        this.pretty = pretty;
    }

    public String getPretty() {
        return pretty;
    }
}
