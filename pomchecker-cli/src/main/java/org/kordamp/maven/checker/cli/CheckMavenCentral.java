/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 Andres Almiray.
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

import org.apache.maven.project.MavenProject;
import org.kordamp.maven.checker.MavenCentralChecker;
import org.kordamp.maven.checker.PomCheckException;
import org.kordamp.maven.checker.cli.internal.PomParser;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
@CommandLine.Command(name = "check-maven-central",
    description = "Checks if a POM complies with the rules for uploading to Maven Central")
public class CheckMavenCentral extends AbstractCommand<Main> {
    @CommandLine.Option(names = {"--strict"},
        negatable = true,
        defaultValue = "true", fallbackValue = "true",
        description = "Checks if <repositories> and <pluginRepositories> are present.")
    boolean strict;

    @CommandLine.Option(names = {"--release"},
        negatable = true,
        defaultValue = "true", fallbackValue = "true",
        description = "Checks if version is -SNAPSHOT.")
    boolean release;

    @CommandLine.Option(names = {"--fail-on-error"},
        negatable = true,
        defaultValue = "true", fallbackValue = "true",
        description = "Fails the build on error.")
    boolean failOnError;

    @CommandLine.Option(names = {"--fail-on-warning"},
        negatable = true,
        defaultValue = "true", fallbackValue = "true",
        description = "Fails the build on warning.")
    boolean failOnWarning;

    @Override
    protected void execute() {
        try {
            logger.info("Maven Central checks: {}", pomFile.toAbsolutePath().toString());
            MavenProject project = PomParser.createMavenProject(pomFile.toFile());
            MavenCentralChecker.check(logger, project, new MavenCentralChecker.Configuration()
                .withRelease(release)
                .withStrict(strict)
                .withFailOnError(failOnError)
                .withFailOnWarning(failOnWarning));
        } catch (PomCheckException e) {
            throw new HaltExecutionException(e);
        }
    }
}
