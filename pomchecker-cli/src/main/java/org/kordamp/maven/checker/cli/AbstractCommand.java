/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.maven.checker.cli;

import org.kordamp.maven.checker.Logger;
import org.kordamp.maven.checker.cli.internal.Colorizer;
import org.kordamp.maven.checker.cli.internal.SimpleLoggerAdapter;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
@CommandLine.Command(mixinStandardHelpOptions = true,
    versionProvider = Versions.class)
abstract class AbstractCommand implements Callable<Integer> {
    protected Logger logger;

    @CommandLine.Option(names = {"-d", "--debug"},
        description = "Set log level to debug.")
    boolean debug;

    @CommandLine.Option(names = {"-i", "--info"},
        description = "Set log level to info.")
    boolean info;

    @CommandLine.Option(names = {"-w", "--warn"},
        description = "Set log level to warn.")
    boolean warn;

    @CommandLine.Option(names = {"-q", "--quiet"},
        description = "Log errors only.")
    boolean quiet;

    @CommandLine.Option(names = {"--file"},
        description = "The POM file to check",
        required = true)
    Path pomFile;

    @CommandLine.Option(names = "-D",
        paramLabel = "<key=value>",
        description = "Sets a System property. Repeatable.",
        mapFallbackValue = "")
    void setProperty(Map<String, String> props) {
        props.forEach(System::setProperty);
    }

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.ParentCommand
    Main parent;

    protected Main parent() {
        return parent;
    }

    public Integer call() {
        Banner.display(spec.commandLine().getOut());

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");

        SimpleLoggerAdapter.Level level = SimpleLoggerAdapter.Level.INFO;
        if (debug) {
            level = SimpleLoggerAdapter.Level.DEBUG;
            System.setProperty("org.slf4j.simpleLogger.org.kordamp.maven", "debug");
        } else if (info) {
            level = SimpleLoggerAdapter.Level.INFO;
            System.setProperty("org.slf4j.simpleLogger.org.kordamp.maven", "info");
        } else if (warn) {
            level = SimpleLoggerAdapter.Level.WARN;
            System.setProperty("org.slf4j.simpleLogger.org.kordamp.maven", "warn");
        } else if (quiet) {
            level = SimpleLoggerAdapter.Level.ERROR;
            System.setProperty("org.slf4j.simpleLogger.org.kordamp.maven", "error");
        }

        logger = new SimpleLoggerAdapter(parent().out, level);

        try {
            execute();
        } catch (HaltExecutionException e) {
            logger.error(e.getCause().getMessage());
            return 1;
        } catch (Exception e) {
            new Colorizer(parent().out).println(e.getMessage());
            e.printStackTrace(new Colorizer(parent().err));
            return 1;
        }

        return 0;
    }

    protected abstract void execute();
}
