package me.nathan3882.svgtosizedpngconverter.types;

import me.nathan3882.svgtosizedpngconverter.TransformerType;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public enum AndroidCompatibleImageType implements FilenameAttachableImageType {


    L_DPI("ldpi", 36, 36), //Resources for low-density (ldpi) screens (~120dpi)

    M_DPI("mdpi", 48, 48), //Resources for medium-density (mdpi) screens (~160dpi). (This is the baseline density.)

    H_DPI("hdpi", 72, 72), //Resources for high-density (hdpi) screens (~240dpi).

    X_HDPI("xhdpi", 96, 96), //Resources for extra-high-density (xhdpi) screens (~320dpi).

    XX_HDPI("xxhdpi", 144, 144), //	Resources for extra-extra-high-density (xxhdpi) screens (~480dpi).

    XXX_HDPI("xxxhdpi", 192, 192), //	Resources for extra-extra-extra-high-density (xxxhdpi) uses (~640dpi).

    //	Resources for all densities. These are density-independent resources.
    NO_DPI("nodpi", 0, 0); //  The system does not scale resources tagged with this qualifier, regardless of the current screen's density.


    private final String densityString;
    private final int height;
    private final int width;

    AndroidCompatibleImageType(String densityString, int width, int height) {
        this.width = width;
        this.height = height;
        this.densityString = densityString;
    }

    public static List<AndroidCompatibleImageType> asList() {
        return Arrays.asList(AndroidCompatibleImageType.values());
    }

    @Override
    public boolean isPrepended() {
        return true; //we need to prepend a slash as Android images are all in different sub directories.
    }

    @Override
    public boolean isAppended() {
        return false;
    }

    /**
     * Get the width
     *
     * @return the width of this FilenameAttachableImageType
     */
    @Override
    public int getWidth() {
        return this.width;
    }

    /**
     * Get the height
     *
     * @return the height of this FilenameAttachableImageType
     */
    @Override
    public int getHeight() {
        return this.height;
    }

    /**
     * @return
     */
    @Override
    public String getFilenameAttachString() {
        return getPrefix() + getDensityString() + File.separatorChar;
    }

    @Override
    public TransformerType getTransformerType() {
        return TransformerType.ANDROID;
    }

    /**
     * Specifically for the Android Compatible Image Types, in android the files have drawable with a dash prepended to the density string.
     *
     * @return the prefix.
     */
    private String getPrefix() {
        return "drawable-";
    }

    public String getDensityString() {
        return densityString;
    }
}
