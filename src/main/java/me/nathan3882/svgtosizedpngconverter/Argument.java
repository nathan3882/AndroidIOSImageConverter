package me.nathan3882.svgtosizedpngconverter;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.List;
import java.util.Optional;

public enum Argument {

    OUTPUT_FILE_DIRECTORY("o", "outputDirectory", "This argument lets the program know which directory to put / output the iOS and Android png files into."),
    INPUT_FILE("i", "inputFile", "This argument lets the program know which directory to fetch the initial SVG file from.");

    private final String argumentString;
    private final String argumentStringLong;
    private final String description;

    Argument(String argumentString, String argumentStringLong, String description) {
        this.argumentString = argumentString;
        this.argumentStringLong = argumentStringLong;
        this.description = description;
    }

    public Optional<String> getValueFromContainer(CommandLine commandLineOptionContainer) {
        Option[] optionArray = commandLineOptionContainer.getOptions();
        final List<String> argList = commandLineOptionContainer.getArgList();

        //Arg list is the actual provided values at what ever index. Indexes are the same as the index in options list
        for (int index = 0; index < optionArray.length; index++) {

            Option argumentString = optionArray[index];

            if (getArgumentString().equals(argumentString.getOpt())) {
                // If this Argument is 'o' AND argument string's getOpt is 'o' as index i, then we know that the
                // argList.get(i) will be the associated value from the container.
                return Optional.of(argList.get(index));
            }

        }
        return Optional.empty();
    }

    public String getDescription() {
        return this.description;
    }

    public String getArgumentStringLong() {
        return this.argumentStringLong;
    }

    public String getArgumentString() {
        return argumentString;
    }

}
