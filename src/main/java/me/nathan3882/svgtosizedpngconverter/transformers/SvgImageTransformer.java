package me.nathan3882.svgtosizedpngconverter.transformers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.transform.TransformerException;
import me.nathan3882.svgtosizedpngconverter.TransformerType;
import me.nathan3882.svgtosizedpngconverter.exceptions.DuplicateFileException;
import me.nathan3882.svgtosizedpngconverter.exceptions.LackOfTransformationException;
import me.nathan3882.svgtosizedpngconverter.svglogic.SvgFile;
import me.nathan3882.svgtosizedpngconverter.types.AndroidCompatibleImageType;
import me.nathan3882.svgtosizedpngconverter.types.FilenameAttachableImageType;
import me.nathan3882.svgtosizedpngconverter.types.IOSCompatibleImageType;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.DOMImplementation;

/**
 * This is a base class that offers some generic functionality for Svg Image Transformer classes
 */
public abstract class SvgImageTransformer implements Transformable {

    private static final DOMImplementation SVG_DOM_IMPLEMENTATION;

    static {
        SVG_DOM_IMPLEMENTATION = SVGDOMImplementation.getDOMImplementation();
    }

    private final File inputSvgFile;
    private final String inputSvgFileName;
    private final String outputDirectoryPath;
    private final String inputSvgPath;

    public SvgImageTransformer(File inputSvgFile, File outputDirectoryPath) throws IOException {
        this.inputSvgFile = inputSvgFile;
        this.inputSvgFileName = inputSvgFile.getName();
        this.inputSvgPath = inputSvgFile.getPath();
        this.outputDirectoryPath = outputDirectoryPath.getPath();
    }

    /**
     * This function should be overridden to transform the {@link #inputSvgPath} svg file into
     * png files that are placed into the overridden abstract function {@link #getOutputDirectory}
     */
    public boolean transform(boolean isPngAlso) throws IOException, TransformerException, DuplicateFileException, LackOfTransformationException {

        try {
            String outputDirPath = getOutputDirectory().getPath();
            Path dir = new File(outputDirPath).toPath();
            Files.createDirectory(dir);
            System.out.println("Created directory @ " + outputDirPath + " to house the images.");
        } catch (FileAlreadyExistsException e) {
            //The directory @ outputDirPath already exists... goody.
        }

        List<? extends FilenameAttachableImageType> imageTypes =
                getTransformerType() == TransformerType.ANDROID ? AndroidCompatibleImageType.asList() : IOSCompatibleImageType.asList();

        for (FilenameAttachableImageType imageType : imageTypes) {

            final String inputSvgFileName = getInputSvgFileName();
            final String amendedFileName = this.getAmendedFileName(inputSvgFileName, imageType);
            final File outputLocation = new File(amendedFileName);

            final int width = imageType.getWidth();
            final int height = imageType.getHeight();

            SvgFile baseSvgFile = SvgFile.fromFile(this.getInputSvgPath());

            final boolean wasResizeSuccessful = baseSvgFile.resizeInMemory(width, height);

            if (!wasResizeSuccessful) {
                System.out.println("ERROR: " + getTransformerType().getPretty() + " file could not be resized to" +
                        " " + baseSvgFile.getHeight() + "x" + baseSvgFile.getWidth() + " due to an IOException :(");
                continue;
            }

            final String outputPath = baseSvgFile.saveTo(outputLocation, true);

            System.out.println(getTransformerType().getPretty() + " file " + FilenameUtils.getName(outputPath) + " resized to" +
                    " " + baseSvgFile.getHeight() + "x" + baseSvgFile.getWidth() + " & created @ " + outputPath + ".");

            if (!isPngAlso) {
                continue;
            }

            try {
                baseSvgFile.createPngAlternative();
                System.out.print("Created a png alternative in the same directory also!");
            } catch (TranscoderException e) {
                return false;
            }

        }
        return true;
    }

    /**
     * Strips the file name of it's extension if {@param imageType #isAppended} and then appends an attach string after the withoutExtension String
     * Otherwise if {@param imageType #isPrepended} and then prepends the string.
     *
     * @param imageType the image type to then fetch the appendable string from.
     * @return a new file name appended with the appendable string from {@link FilenameAttachableImageType}.
     */
    public String getAmendedFileName(String fileNameWithExtension, FilenameAttachableImageType imageType) {

        if (imageType.isPrepended()) {
            return getOutputDirectory() + imageType.getFilenameAttachString() + fileNameWithExtension;
        }

        //If we reach here, we know that the image type is appended after the name but before the extension.
        final String noExtension = FilenameUtils.removeExtension(fileNameWithExtension);
        final String extension = FilenameUtils.getExtension(fileNameWithExtension);

        return getOutputDirectory() + noExtension + imageType.getFilenameAttachString() + "." + extension;
    }

    protected String getOutputDirectoryPath() {
        return this.outputDirectoryPath;
    }

    public abstract File getOutputDirectory();

    public String getInputSvgPath() {
        return inputSvgPath;
    }

    public String getInputSvgFileName() {
        return inputSvgFileName;
    }
}
