/**
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
import org.kordamp.maven.checker.BomChecker;
import org.kordamp.maven.checker.PomCheckException;
import org.kordamp.maven.checker.cli.internal.PomParser;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
@CommandLine.Command(name = "check-bom",
    description = "Checks if a POM file is a minimal BOM file")
public class CheckBom extends AbstractCommand {
    @Override
    protected void execute() {
        try {
            MavenProject project = PomParser.createMavenProject(pomFile.toFile());
            BomChecker.check(logger, project);
        } catch (PomCheckException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            throw new PomcheckerException("Unexpected error", e);
        }
    }
}
