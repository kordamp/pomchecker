/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
public class CheckMavenCentral extends AbstractCommand {
    @CommandLine.Option(names = {"--no-strict"},
        description = "Checks if <repositories> and <pluginRepositories> are present")
    boolean strict;

    @CommandLine.Option(names = {"--no-release"},
        description = "Checks if version is -SNAPSHOT")
    boolean release;

    @Override
    protected void execute() {
        try {
            MavenProject project = PomParser.createMavenProject(pomFile.toFile());
            MavenCentralChecker.check(logger, project, !release, !strict);
        } catch (PomCheckException e) {
            throw new HaltExecutionException(e);
        } catch (Exception e) {
            throw new PomcheckerException("Unexpected error", e);
        }
    }
}
