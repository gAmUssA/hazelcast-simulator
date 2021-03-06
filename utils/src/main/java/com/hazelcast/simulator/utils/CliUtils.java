/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.simulator.utils;

import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.OutputStream;

public final class CliUtils {

    private static final int HELP_WIDTH = 160;
    private static final int HELP_INDENTATION = 2;

    private CliUtils() {
    }

    public static OptionSet initOptionsWithHelp(OptionParser parser, String[] args) {
        try {
            OptionSpec helpSpec = parser.accepts("help", "Show help").forHelp();
            OptionSet options = parser.parse(args);

            if (options.has(helpSpec)) {
                printHelpAndExit(parser, System.out);
            }

            return options;
        } catch (OptionException e) {
            throw new CommandLineExitException(e.getMessage() + ". Use --help to get overview of the help options.");
        }
    }

    public static void printHelpAndExit(OptionParser parser) {
        printHelpAndExit(parser, System.out);
    }

    static void printHelpAndExit(OptionParser parser, OutputStream sink) {
        try {
            parser.formatHelpWith(new BuiltinHelpFormatter(HELP_WIDTH, HELP_INDENTATION));
            parser.printHelpOn(sink);
        } catch (Exception e) {
            throw new CommandLineExitException("Could not print command line help", e);
        }
        System.exit(0);
    }
}
