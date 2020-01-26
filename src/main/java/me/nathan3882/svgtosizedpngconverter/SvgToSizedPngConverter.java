package me.nathan3882.svgtosizedpngconverter;

import me.nathan3882.svgtosizedpngconverter.exceptions.DuplicateFileException;
import me.nathan3882.svgtosizedpngconverter.exceptions.LackOfTransformationException;
import me.nathan3882.svgtosizedpngconverter.transformers.AndroidImageTransformer;
import me.nathan3882.svgtosizedpngconverter.transformers.IOSImageTransformer;
import me.nathan3882.svgtosizedpngconverter.transformers.SvgImageTransformer;
import me.nathan3882.svgtosizedpngconverter.types.FileType;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SvgToSizedPngConverter {

    private static final Argument OUTPUT_FILE_DIR_ARG = Argument.OUTPUT_FILE_DIRECTORY;
    private static final Argument INPUT_FILE_ARG = Argument.INPUT_FILE;

    public static void main(String[] args) throws IOException, ParseException, TranscoderException, TransformerException, DuplicateFileException, LackOfTransformationException {

        final Options argumentOptions = new Options();

        final String outputDirectoryArgOptionString = OUTPUT_FILE_DIR_ARG.getArgumentString();
        final String inputSvgFileArgOptionString = INPUT_FILE_ARG.getArgumentString();

        final List<Option> configuredArgumentOptions = SvgToSizedPngConverter.getConfiguredArgumentOptions();

        for (Option configuredArgumentOption : configuredArgumentOptions) {
            argumentOptions.addOption(configuredArgumentOption);
        }

        CommandLine commandLineOptionContainer = new DefaultParser().parse(argumentOptions, args);

        File imageOutputDirectory; //this is the directory that we will output the iOS and Android images to.
        if (commandLineOptionContainer.hasOption(outputDirectoryArgOptionString)) {

            final Optional<String> specifiedOutputFileDirectoryOptional = Argument.OUTPUT_FILE_DIRECTORY.getValueFromContainer(commandLineOptionContainer);
            if (!specifiedOutputFileDirectoryOptional.isPresent()) {
                System.out.println("We couldn't fetch the output file directory from the run arguments... strange :(");
                return;
            }

            String specifiedOutputFileDirectory = specifiedOutputFileDirectoryOptional.get();

            System.out.print("Output Directory argument option found - (-" + outputDirectoryArgOptionString + ").  " +
                    "Now outputting the iOS Images and Android Images to " + specifiedOutputFileDirectory + System.getProperty("line.separator"));

            if (specifiedOutputFileDirectory.equals("desktop")) {
                imageOutputDirectory = new File(System.getProperty("user.home") + File.separatorChar + "Desktop");
            } else {
                imageOutputDirectory = new File(specifiedOutputFileDirectory);
            }
        } else {
            System.out.println("Please specify an output directory using the option -o. For example append '-o desktop' or the full path.");

            return;
        }

        if (commandLineOptionContainer.hasOption(inputSvgFileArgOptionString)) {

            //Now we have an output directory AND an input directory, so we can proceed with the transformation of the SVG input file.

            final Optional<String> specifiedInputFilePathnameOptional = INPUT_FILE_ARG.getValueFromContainer(commandLineOptionContainer);

            if (!specifiedInputFilePathnameOptional.isPresent()) {
                System.out.println("We couldn't fetch the output file directory from the run arguments... strange :(");
                return;
            }

            String specifiedInputFilePathname = specifiedInputFilePathnameOptional.get();

            SvgToSizedPngConverter.doTransformation(imageOutputDirectory, specifiedInputFilePathname);


        }
    }

    private static void doTransformation(File imageOutputDirectory, String specifiedInputFilePathname) throws IOException, TransformerException, DuplicateFileException, LackOfTransformationException {
        final File inputFile = new File(specifiedInputFilePathname);

        final boolean exists = inputFile.exists();
        if (!exists || !FilenameUtils.getExtension(inputFile.getName()).equals(FileType.SVG.getExtensionNoDot())) {
            //terminate as we can't get the svg file.
            System.out.println("We cant find a dot svg file @ " + specifiedInputFilePathname);
            return;
        }

        //at this point we know that the file exists and it has a file extension of svg.
        SvgImageTransformer iOSImageTransformer = new IOSImageTransformer(inputFile, imageOutputDirectory);

        SvgImageTransformer androidImageTransformer = new AndroidImageTransformer(inputFile, imageOutputDirectory);

        final boolean doPngAlso = true;

        final boolean iOSDoneSuccessfully = iOSImageTransformer.transform(doPngAlso);
        final boolean isAndroidDoneSuccessfully = androidImageTransformer.transform(doPngAlso);

        final String androidPretty = androidImageTransformer.getTransformerType().getPretty();
        final String iosPretty = iOSImageTransformer.getTransformerType().getPretty();

        if (iOSDoneSuccessfully) {
            System.out.println(iosPretty + " images have been created at " + iOSImageTransformer.getOutputDirectory());
        } else {
            System.out.println("Some unknown programmatic error meant that no " + iosPretty + " were created :(");

        }

        if (isAndroidDoneSuccessfully) {
            System.out.println(androidPretty + " images have been created at " + androidImageTransformer.getOutputDirectory());
        } else {
            System.out.println("Some unknown programmatic error meant that no " + androidPretty + " + were created :(");
        }
    }

    private static List<Option> getConfiguredArgumentOptions() {
        Argument outputFileArgument = OUTPUT_FILE_DIR_ARG;
        Argument inputFileArgument = INPUT_FILE_ARG;

        Option builtOutputArgumentOption = Option.builder(outputFileArgument.getArgumentString())
                .longOpt(outputFileArgument.getArgumentStringLong())
                .desc(outputFileArgument.getDescription())
                .required()
                .build();

        Option builtInputArgumentOption = Option.builder(inputFileArgument.getArgumentString())
                .longOpt(inputFileArgument.getArgumentStringLong())
                .desc(inputFileArgument.getDescription())
                .required()
                .build();


        return Arrays.asList(builtOutputArgumentOption, builtInputArgumentOption);
    }

}
