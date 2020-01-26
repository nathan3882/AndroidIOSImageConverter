package me.nathan3882.svgtosizedpngconverter.types;

import me.nathan3882.svgtosizedpngconverter.TransformerType;
import me.nathan3882.svgtosizedpngconverter.svglogic.SvgFile;
import me.nathan3882.svgtosizedpngconverter.transformers.Transformable;

import java.io.File;

public interface FilenameAttachableImageType extends Transformable {

    /**
     * Gets whether this {@link #getFilenameAttachString()} is prepended or not
     *
     * @return true if it's prepended before, otherwise it's appended and this will return false;
     */
    boolean isPrepended();

    default boolean isAppended() {
        return !isPrepended();
    }

    /**
     * Get the width
     *
     * @return the width of this FilenameAttachableImageType
     */
    int getWidth();

    /**
     * Get the height
     *
     * @return the height of this FilenameAttachableImageType
     */
    int getHeight();

    /**
     * This string should be inserted either BEFORE (ie (@link isPrepended} is true) or AFTER (ie @link isAppended is true) the
     * {@link File#getName} of the {@link SvgFile} file.
     *
     * @return
     */
    String getFilenameAttachString();

    @Override
    TransformerType getTransformerType();
}
