package me.nathan3882.svgtosizedpngconverter.transformers;

import me.nathan3882.svgtosizedpngconverter.TransformerType;
import me.nathan3882.svgtosizedpngconverter.exceptions.DuplicateFileException;
import me.nathan3882.svgtosizedpngconverter.exceptions.LackOfTransformationException;
import me.nathan3882.svgtosizedpngconverter.svglogic.SvgFile;
import me.nathan3882.svgtosizedpngconverter.types.AndroidCompatibleImageType;
import me.nathan3882.svgtosizedpngconverter.types.FilenameAttachableImageType;
import me.nathan3882.svgtosizedpngconverter.types.IOSCompatibleImageType;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.DOMImplementation;

import javax.imageio.ImageIO;
import javax.xml.transform.TransformerException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
    private final BufferedImage inputSvgAsBufferedImage;
    private final String outputDirectoryPath;
    private final String inputSvgPath;

    public SvgImageTransformer(File inputSvgFile, File outputDirectoryPath) throws IOException {
        this.inputSvgFile = inputSvgFile;
        this.inputSvgFileName = inputSvgFile.getName();
        this.inputSvgPath = inputSvgFile.getPath();
        this.inputSvgAsBufferedImage = ImageIO.read(this.inputSvgFile);
        this.outputDirectoryPath = outputDirectoryPath.getPath();
    }

    /**
     * Strips the file name of it's extension if {@param imageType #isAppended} and then appends an attach string after the withoutExtension String
     * Otherwise if {@param imageType #isPrepended} and then prepends the string.
     *
     * @param outputDirectory the file name with the extension still intact.
     * @param imageType       the image type to then fetch the appendable string from.
     * @return a new file name appended with the appendable string from {@link FilenameAttachableImageType}.
     */
    public static String getAmendedFileName(String outputDirectory, String fileNameWithExtension, FilenameAttachableImageType imageType) {

        if (imageType.isPrepended()) {
            return outputDirectory + imageType.getFilenameAttachString() + fileNameWithExtension;
        }
        //If we reach here, we know that the image type is appended after the name but before the extension.
        final String noExtension = FilenameUtils.removeExtension(fileNameWithExtension);
        final String extension = FilenameUtils.getExtension(fileNameWithExtension);

        return outputDirectory + noExtension + imageType.getFilenameAttachString() + "." + extension;
    }

    /**
     * This function should be overridden to transform the {@link #inputSvgAsBufferedImage} into
     * png files that are placed into the {@link #outputDirectoryPath}
     */
    public boolean transform() throws IOException, TransformerException, DuplicateFileException, LackOfTransformationException {

        String outputDirPath = getOutputDirectory().getPath();
        final Path dir = new File(outputDirPath).toPath();
        try {
            Files.createDirectory(dir);
            System.out.println("Created directory @ " + outputDirPath + " to house the images.");
        } catch (FileAlreadyExistsException e) {
//            The directory @ outputDirPath already exists... goody.
        }

        final List<? extends FilenameAttachableImageType> iosCompatibleImageTypes =
                getTransformerType() == TransformerType.ANDROID ? AndroidCompatibleImageType.asList() : IOSCompatibleImageType.asList();

        for (FilenameAttachableImageType imageType : iosCompatibleImageTypes) {

            final String outputDirectoryBeforePotentialPrepend = outputDirPath + File.separatorChar;

            final String inputSvgFileName = getInputSvgFileName();
            final String amendedFileName = SvgImageTransformer.getAmendedFileName(outputDirectoryBeforePotentialPrepend, inputSvgFileName, imageType);
            final File outputLocation = new File(amendedFileName);

            final int width = imageType.getWidth();
            final int height = imageType.getHeight();

            SvgFile baseSvgFile = SvgFile.fromFile(this.getInputSvgPath());

            baseSvgFile.resizeInMemory(width, height);

//            try {
//                Files.createDirectory(outputLocation.toPath());
//            } catch (FileAlreadyExistsException e) {
//                Directory to house amendedFileName already exists, goody
//            } catch (NoSuchFileException exception) {
//                about to create the file
//            }

            final SvgFile resizedSvgFile = baseSvgFile.saveTo(outputLocation, true);

            System.out.println(getTransformerType().getPretty() + " file " + baseSvgFile + " resized and created @ " + resizedSvgFile + ".");

            //convert to png
        }
        return true;
    }

    /**
     * @param fileToResizeInMemory the {@link SvgFile} to resize.
     * @param newWidth             the new width
     * @param newHeight            the new height
     * @return the {@link SvgFile} resized according to new width / height param values.
     */
    protected SvgFile resizeInMemory(SvgFile fileToResizeInMemory, int newWidth, int newHeight) throws IOException, TransformerException {
        fileToResizeInMemory.resizeInMemory(newWidth, newHeight);
        return fileToResizeInMemory;
    }

    protected String getOutputDirectoryPath() {
        return this.outputDirectoryPath;
    }

    public abstract File getOutputDirectory();

    public static DOMImplementation getSvgDomImplementation() {
        return SVG_DOM_IMPLEMENTATION;
    }

    public String getInputSvgPath() {
        return inputSvgPath;
    }

    protected File getInputSvgFile() {
        return this.inputSvgFile;
    }

    protected BufferedImage getInputSvgAsBufferedImage() {
        return this.inputSvgAsBufferedImage;
    }

    public String getInputSvgFileName() {
        return inputSvgFileName;
    }
}
