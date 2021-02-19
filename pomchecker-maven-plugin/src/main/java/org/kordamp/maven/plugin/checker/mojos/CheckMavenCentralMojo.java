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
package org.kordamp.maven.plugin.checker.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.kordamp.maven.checker.MavenCentralChecker;
import org.kordamp.maven.checker.PomCheckException;

/**
 * Checks if a POM complies with the rules for uploading to Maven Central.
 *
 * @author Andres Almiray
 * @since 1.0.0
 */
@Mojo(name = "check-maven-central")
public class CheckMavenCentralMojo extends AbstractMojo {
    /**
     * The project whose model will be checked.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Checks if version is -SNAPSHOT.
     */
    @Parameter(property = "checker.release", defaultValue = "true")
    private boolean release = true;

    /**
     * Checks if &lt;repositories&gt; and &lt;pluginRepositories&gt; are present.
     */
    @Parameter(property = "checker.strict", defaultValue = "true")
    private boolean strict = true;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            MavenCentralChecker.check(getLog(), project, release, strict);
        } catch (PomCheckException e) {
            throw new MojoExecutionException("MavenCentral check failed", e);
        }
    }
}
