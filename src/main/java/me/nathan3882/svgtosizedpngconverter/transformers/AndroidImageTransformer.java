package me.nathan3882.svgtosizedpngconverter.transformers;

import me.nathan3882.svgtosizedpngconverter.TransformerType;

import java.io.File;
import java.io.IOException;

public class AndroidImageTransformer extends SvgImageTransformer {
    public AndroidImageTransformer(File inputSvgFile, File outputDirectory) throws IOException {
        super(inputSvgFile, outputDirectory);
    }

    /**
     * This gets the output directory for this specific {@link TransformerType}.
     * It appends {@link TransformerType#getPretty()}
     *
     * @return
     */
    @Override
    public File getOutputDirectory() {
        final String outputDirectoryPath = super.getOutputDirectoryPath();
        final String pretty = getTransformerType().getPretty();
        return new File(outputDirectoryPath + File.separatorChar + pretty);
    }


    @Override
    public TransformerType getTransformerType() {
        return TransformerType.ANDROID;
    }
}
