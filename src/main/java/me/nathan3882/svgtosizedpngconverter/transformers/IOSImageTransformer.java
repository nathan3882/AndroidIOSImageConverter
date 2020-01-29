package me.nathan3882.svgtosizedpngconverter.transformers;

import java.io.File;
import java.io.IOException;
import me.nathan3882.svgtosizedpngconverter.TransformerType;

public class IOSImageTransformer extends SvgImageTransformer {

    public IOSImageTransformer(File inputSvgFile, File outputDirectory) throws IOException {
        super(inputSvgFile, outputDirectory);
    }

    /**
     * This gets the output directory for this specific {@link TransformerType}.
     * It appends {@link TransformerType#getPretty()}
     *
     * @return an appended string
     */
    @Override
    public File getOutputDirectory() {
        final String outputDirectoryPath = super.getOutputDirectoryPath();
        final String pretty = getTransformerType().getPretty();
        return new File(outputDirectoryPath + File.separatorChar + pretty + File.separatorChar);
    }


    @Override
    public TransformerType getTransformerType() {
        return TransformerType.IOS;
    }

}
