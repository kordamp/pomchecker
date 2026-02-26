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
package org.kordamp.maven.plugin.checker.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.kordamp.maven.checker.BomChecker;
import org.kordamp.maven.checker.MavenLoggerAdapter;
import org.kordamp.maven.checker.PomCheckException;

/**
 * Checks if a POM file is a minimal BOM file.
 * Minimal BOM files contain the following elements:
 * <ul>
 * <li>&lt;groupId&gt;</li>
 * <li>&lt;artifactId&gt;</li>
 * <li>&lt;version&gt;</li>
 * <li>&lt;dependencyManagement&gt;</li>
 * </ul>
 *
 * @author Andres Almiray
 * @since 1.0.0
 */
@Mojo(name = "check-bom")
public class CheckBomMojo extends AbstractMojo {
    /**
     * Fail the build on error.
     */
    @Parameter(property = "checker.fail.on.error", defaultValue = "true")
    private boolean failOnError;

    /**
     * The project whose model will be checked.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            BomChecker.check(new MavenLoggerAdapter(getLog()), project, new BomChecker.Configuration()
                .withFailOnError(failOnError));
        } catch (PomCheckException e) {
            throw new MojoExecutionException("Bom check failed", e);
        }
    }
}
