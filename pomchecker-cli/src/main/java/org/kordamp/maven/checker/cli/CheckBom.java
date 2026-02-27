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

import org.kordamp.maven.checker.BomChecker;
import org.kordamp.maven.checker.MavenProject;
import org.kordamp.maven.checker.PomCheckException;
import org.kordamp.maven.checker.cli.internal.PomParser;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
@CommandLine.Command(name = "check-bom",
    description = "Checks if a POM file is a minimal BOM file")
public class CheckBom extends AbstractCommand<Main> {
    @CommandLine.Option(names = {"--fail-on-error"},
        negatable = true,
        defaultValue = "true", fallbackValue = "true",
        description = "Fails the build on error.")
    boolean failOnError;

    @Override
    protected void execute() {
        try {
            logger.info("BOM checks: {}", pomFile.toAbsolutePath().toString());
            MavenProject project = PomParser.createMavenProject(pomFile.toFile(), collectRepositories());
            BomChecker.check(logger, project, new BomChecker.Configuration()
                .withFailOnError(failOnError));
        } catch (PomCheckException e) {
            throw new HaltExecutionException(e);
        }
    }
}
