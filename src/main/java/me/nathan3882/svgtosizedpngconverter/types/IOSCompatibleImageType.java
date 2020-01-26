package me.nathan3882.svgtosizedpngconverter.types;

import me.nathan3882.svgtosizedpngconverter.TransformerType;

import java.util.Arrays;
import java.util.List;

public enum IOSCompatibleImageType implements FilenameAttachableImageType {

    ONE_X("@1x", 100, 100),
    TWO_X("@2x", 200, 200),
    THREE_X("@3x", 300, 300);

    private final String appendString;
    private final int height;
    private final int width;

    IOSCompatibleImageType(String appendString, int width, int height) {
        this.appendString = appendString;
        this.width = width;
        this.height = height;
    }

    public static List<IOSCompatibleImageType> asList() {
        return Arrays.asList(IOSCompatibleImageType.values());
    }

    @Override
    public boolean isPrepended() {
        return false;
    }

    @Override
    public boolean isAppended() {
        return true;
    }

    /**
     * This gets the string that will be appended to the output png image for IOS image types.
     *
     * @return the string to be appended.
     */
    @Override
    public String getFilenameAttachString() {
        return appendString;
    }

    @Override
    public TransformerType getTransformerType() {
        return TransformerType.IOS;
    }

    /**
     * Get the height
     *
     * @return the height of this IOSImageType
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Get the width
     *
     * @return the width of this IOSImageType
     */
    @Override
    public int getWidth() {
        return width;
    }
}
