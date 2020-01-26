package me.nathan3882.svgtosizedpngconverter.svglogic;

import me.nathan3882.svgtosizedpngconverter.exceptions.DuplicateFileException;
import me.nathan3882.svgtosizedpngconverter.exceptions.LackOfTransformationException;
import me.nathan3882.svgtosizedpngconverter.types.FileType;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.FilenameUtils;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SvgFile extends File implements TwoDimentional {

    private static Transformer transformer;
    private static PNGTranscoder pngTranscoder;


    static {
        pngTranscoder = new PNGTranscoder();
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
    }
    private SVGOMDocument resizedDocument;
    private SvgFile resizedSvgFile;
    private Integer height;
    private Integer width;

    /**
     * Creates a new <code>File</code> instance by converting the given
     * inputSvgFilePath string into an abstract inputSvgFilePath.  If the given string is
     * the empty string, then the result is the empty abstract inputSvgFilePath.
     *
     * @param inputSvgFilePath A inputSvgFilePath string
     * @throws NullPointerException If the <code>inputSvgFilePath</code> argument is <code>null</code>
     */
    public SvgFile(String inputSvgFilePath) {
        super(inputSvgFilePath);
    }

    public SvgFile(String parent, String child, TwoDimentional twoDimentional) {
        super(parent, child);
        this.height = twoDimentional.getHeight();
        this.width = twoDimentional.getWidth();
    }

    public static SvgFile fromFile(String inputSvgFilePath) {
        return new SvgFile(inputSvgFilePath);
    }

    /**
     * Function to resize a dot svg file.
     *
     * @param newWidth  the new width
     * @param newHeight the new height
     * @throws IOException          if error occurs during i/o of SVG file bytes.
     * @throws TransformerException if we couldnt initialise the {@link Transformer}
     */
    public void resizeInMemory(int newWidth, int newHeight) throws IOException, TransformerException {
        this.height = newHeight;
        this.width = newWidth;

        final String uri = super.toURI().toString();
        final SVGMetaPost svgMetaPost = new SVGMetaPost(uri);

        final SVGOMDocument svgDocument = (SVGOMDocument) svgMetaPost.getSVGDocument();
        final SVGOMSVGElement documentElement = (SVGOMSVGElement) svgDocument.getDocumentElement();

        documentElement.setAttribute(AttributeName.WIDTH.getPretty(), String.valueOf(newWidth));
        documentElement.setAttribute(AttributeName.HEIGHT.getPretty(), String.valueOf(newHeight));

        this.resizedDocument = svgDocument;
    }

    /**
     * This function will save a resized {@link SvgFile} and return the new svg instance that is equal to that resized svg
     *
     * @return a resized {@link SvgFile}
     * @throws DuplicateFileException
     */
    public String saveTo(File specifiedOutputFile, boolean pngToo) throws DuplicateFileException, LackOfTransformationException, TransformerException, IOException {
        final Optional<SVGOMDocument> resizedDocumentOptional = getResizedDocument();

        if (!resizedDocumentOptional.isPresent()) {
            throw new LackOfTransformationException("An SvgFile instance is in memory, and a save operation was attempted. However, it's not been resized to anything yet. Call SvgFile resizeTo to do so.");
        }

        final String specifiedOutputFileAbsolutePath = specifiedOutputFile.getAbsolutePath();

        if (specifiedOutputFile.exists()) {
            final boolean deleteSuccess = specifiedOutputFile.delete();
            if (!deleteSuccess) {
                throw new DuplicateFileException("A file exists at " + specifiedOutputFileAbsolutePath + " already! Please delete and try again!");
            }
        }

        final String path = FilenameUtils.getFullPath(specifiedOutputFileAbsolutePath);

        final SvgFile outputFile = new SvgFile(path, specifiedOutputFile.getName(), this);

        final String parent = outputFile.getParent();
        final Path outputFileParentPath = new File(parent).toPath();

        try {
            Files.createDirectory(outputFileParentPath);
        } catch (FileAlreadyExistsException e) {
            // file already exists...... we just deleted it.... wut
        }

        final StreamResult outputFileStreamResult = new StreamResult(outputFile.getPath());

        final Source inputDomSource = new DOMSource(resizedDocumentOptional.get());

        getTransformer().transform(inputDomSource, outputFileStreamResult);

        this.resizedSvgFile = outputFile;
        return outputFile.getPath();
    }

    /**
     * This function creates a png file in the same location that the scaled svg file will be located.
     */
    public void createPngAlternative() throws IOException, TranscoderException {

        //Step -1: We read the input SVG document into Transcoder Input
        //We use Java NIO for this purpose
        final SvgFile resizedSvgFile = getResizedSvgFile();

        final String svgInputNameWithoutExtension = FilenameUtils.removeExtension(resizedSvgFile.getName());

        final String convertThisToPng = resizedSvgFile.toURI().toURL().toString();

        final TranscoderInput svgTranscoderInput = new TranscoderInput(convertThisToPng);

        //Step-2: Define OutputStream to PNG Image and attach to TranscoderOutput

        final String outputPngFileName = svgInputNameWithoutExtension + FileType.PNG.getExtensionWithDot();

        final String parent = resizedSvgFile.getParent();
        final String pathToOutputPng = parent + File.separatorChar + outputPngFileName;

        final File outputPngFile = new File(pathToOutputPng);

        OutputStream pngOutputStream = new FileOutputStream(outputPngFile);

        TranscoderOutput pngOutputTranscoder = new TranscoderOutput(pngOutputStream);

        pngTranscoder.transcode(svgTranscoderInput, pngOutputTranscoder);

        pngOutputStream.flush();
        pngOutputStream.close();
    }

    @Override
    public String toString() {
        return "{SvgFile(name=" + getName() + ", h=" + height + " & w=" + width + ")}";
    }

    @Override
    public int getHeight() {
        final Optional<Integer> newHeight = Optional.ofNullable(this.width);
        return newHeight.isPresent() ? newHeight.get() : 1;
    }

    @Override
    public int getWidth() {
        final Optional<Integer> newWidth = Optional.ofNullable(this.height);
        return newWidth.isPresent() ? newWidth.get() : 1;
    }

    public SvgFile getResizedSvgFile() {
        return resizedSvgFile;
    }

    /**
     * This gets the previously resized document
     *
     * @return the resized nonnull SVGOMDocument wrapped inside an {@link Optional}, or empty optional
     */
    public Optional<SVGOMDocument> getResizedDocument() {
        return Optional.ofNullable(resizedDocument);
    }

    public static Transformer getTransformer() {
        return SvgFile.transformer;
    }

}
