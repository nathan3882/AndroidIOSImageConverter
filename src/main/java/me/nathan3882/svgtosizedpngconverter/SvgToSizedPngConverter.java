package me.nathan3882.svgtosizedpngconverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import me.nathan3882.svgtosizedpngconverter.exceptions.DuplicateFileException;
import me.nathan3882.svgtosizedpngconverter.exceptions.LackOfTransformationException;
import me.nathan3882.svgtosizedpngconverter.transformers.AndroidImageTransformer;
import me.nathan3882.svgtosizedpngconverter.transformers.IOSImageTransformer;
import me.nathan3882.svgtosizedpngconverter.transformers.SvgImageTransformer;
import me.nathan3882.svgtosizedpngconverter.types.FileType;

public class SvgToSizedPngConverter {

    private static final Argument OUTPUT_FILE_DIR_ARG = Argument.OUTPUT_FILE_DIRECTORY;
    private static final Argument INPUT_FILE_ARG = Argument.INPUT_FILE;
    private static final InputStream in = System.in;
    private static final String[] EMPTY_ARRAY = new String[]{ };

    public static void main(String[] args) throws IOException, ParseException, TransformerException, DuplicateFileException, LackOfTransformationException {

        String[] scannerArgs = queryForArgs();

        final Options argumentOptions = new Options();

        for (Option configuredArgumentOption : SvgToSizedPngConverter.getConfiguredArgumentOptions()) {
            argumentOptions.addOption(configuredArgumentOption);
        }

        CommandLine commandLineOptionContainer = null;

        boolean providedOptions = false;
        while (!providedOptions) {
            try {
                commandLineOptionContainer = new DefaultParser().parse(argumentOptions, scannerArgs);
            } catch (MissingOptionException e) {
                scannerArgs = queryForArgs();
                continue;
            }
            providedOptions = true;
        }

        final String outputDirectoryArgOptionString = OUTPUT_FILE_DIR_ARG.getArgumentString();
        final String inputSvgFileArgOptionString = INPUT_FILE_ARG.getArgumentString();


        File imageOutputDirectory; //this is the directory that we will output the iOS and Android images to.
        if (commandLineOptionContainer.hasOption(outputDirectoryArgOptionString)) {

            final Optional<String> specifiedOutputFileDirectoryOptional = Argument.OUTPUT_FILE_DIRECTORY.getValueFromContainer(commandLineOptionContainer);
            if (!specifiedOutputFileDirectoryOptional.isPresent()) {
                sendBlank();
                System.out.println("We couldn't fetch the output file directory from the run arguments :(");
                sendBlank();
                return;
            }

            String specifiedOutputFileDirectory = specifiedOutputFileDirectoryOptional.get();


            sendBlank();
            System.out.print("Output Directory argument option found - (-" + outputDirectoryArgOptionString + ").  " +
                    "Now outputting the iOS Images and Android Images to " + specifiedOutputFileDirectory + System.getProperty("line.separator"));
            sendBlank();
            if (specifiedOutputFileDirectory.equals("desktop")) {
                imageOutputDirectory = new File("C:\\Users\\natha\\OneDrive\\Desktop");
            } else {
                imageOutputDirectory = new File(specifiedOutputFileDirectory);
            }
        } else {
            sendBlank();
            System.out.println("Please specify an output directory using the option -o. For example append '-o desktop' or the full path.");
            sendBlank();
            return;
        }

        if (commandLineOptionContainer.hasOption(inputSvgFileArgOptionString)) {

            //Now we have an output directory AND an input directory, so we can proceed with the transformation of the SVG input file.

            final Optional<String> specifiedInputFilePathnameOptional = INPUT_FILE_ARG.getValueFromContainer(commandLineOptionContainer);

            if (!specifiedInputFilePathnameOptional.isPresent()) {
                System.out.println("We couldn't fetch the input file directory from the run arguments... strange :(");
                return;
            }

            String specifiedInputFilePathname = specifiedInputFilePathnameOptional.get();

            SvgToSizedPngConverter.doTransformation(imageOutputDirectory, specifiedInputFilePathname);

        }
    }

    private static String[] queryForArgs() {
        sendBlank(2);
        System.out.println("Please provide an argument for the output (-o) and the input svg file (-i). Enter desktop for 'System.getProperty(\"user.home\") + File.separatorChar + \"Desktop\"' as the output location... For example:");
        sendBlank();
        System.out.println("-o \"desktop\" -i \"C:\\Stock Input\\hamburger-icon.svg\"");
        sendBlank(2);

        System.out.print("Type here:-> ");

        Scanner scanner = new Scanner(in);
        if (scanner.hasNext()) {
            return Arrays.stream(disregardedSplit(scanner.nextLine())) //Stream the entered text
                    .map(entry -> entry.replace("\"", "")) //strip it of it's " characters
                    .toArray(String[]::new); //Collect to new array.

        }
        scanner.close();
        return EMPTY_ARRAY;

    }

    /**
     * This will split by a space. However, if this is inside double quotes then it will not be counted.
     *
     * @param input the input to perform the regex match on
     * @return a disregarded split.
     */
    private static String[] disregardedSplit(String input) {
        return input.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private static void sendBlank() {
        sendBlank(1);
    }

    private static void sendBlank(int number) {
        for (int i = 0; i < number; i++) {
            System.out.println(" ");
        }
    }

    private static void doTransformation(File imageOutputDirectory, String specifiedInputFilePathname) throws IOException, TransformerException, DuplicateFileException, LackOfTransformationException {
        final File inputFile = new File(specifiedInputFilePathname);

        final boolean exists = inputFile.exists();
        if (!exists || !FilenameUtils.getExtension(inputFile.getName()).equals(FileType.SVG.getExtensionNoDot())) {
            //terminate as we can't get the svg file.
            sendBlank();
            System.out.println("We cant find a dot svg file @ " + specifiedInputFilePathname);
            sendBlank();
            try {
                main(EMPTY_ARRAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
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

        sendBlank();
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
