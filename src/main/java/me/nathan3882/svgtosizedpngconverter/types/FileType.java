package me.nathan3882.svgtosizedpngconverter.types;

public enum FileType {

    SVG("svg"),
    PNG("png");

    private final String extensionNoDot;

    FileType(String extensionNoDot) {
        this.extensionNoDot = extensionNoDot;
    }

    public String getExtensionNoDot() {
        return extensionNoDot;
    }

    public String getExtensionWithDot() {
        return "." + this.extensionNoDot;
    }
}
