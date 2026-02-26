/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 Andres Almiray.
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
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
@CommandLine.Command(mixinStandardHelpOptions = true,
    versionProvider = Versions.class)
abstract class AbstractCommand<C extends IO> extends BaseCommand implements Callable<Integer> {
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

    Path pomFile;

    @CommandLine.ParentCommand
    private C parent;

    @CommandLine.Option(names = {"--file"},
        defaultValue = CommandLine.Option.NULL_VALUE,
        description = "The POM file to check. Defaults to pom.xml")
    void setPomFile(Path pomFile) {
        Path pomFileWithDefault = pomFile != null ? pomFile : Paths.get("pom.xml");

        if (!pomFileWithDefault.toFile().exists()) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                String.format("Invalid value '%s' for option '--file': The file does not exist.",
                    pomFileWithDefault));
        }

        this.pomFile = pomFileWithDefault;
    }

    @CommandLine.Option(names = {"--repository"},
        paramLabel = "<repository>",
        description = "Absolute path to a local Maven repository")
    String[] repositories;

    protected C parent() {
        return parent;
    }

    public Integer call() {
        Banner.display(spec.commandLine().getErr());

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

        logger = new SimpleLoggerAdapter(parent().getOut(), level);

        try {
            execute();
        } catch (HaltExecutionException e) {
            logger.error(e.getCause().getMessage());
            return 1;
        } catch (Exception e) {
            new Colorizer(parent().getOut()).println(e.getMessage());
            e.printStackTrace(new Colorizer(parent().getErr()));
            return 1;
        }

        return 0;
    }

    protected abstract void execute();

    protected Set<Path> collectRepositories() {
        Set<Path> set = new LinkedHashSet<>();
        if (null != repositories) {
            for (String repository : repositories) {
                set.add(Paths.get(repository.trim()));
            }
        }
        return set;
    }
}
