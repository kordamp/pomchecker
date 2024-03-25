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
package org.kordamp.maven.checker;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.lineSeparator;

/**
 * Checks if a POM file is a minimal BOM file.
 * <p>
 * The following blocks are required:
 * <ul>
 *   <li>&lt;dependencyManagement&gt;</li>
 * </ul>
 * <p>
 * The following blocks are forbidden:
 * <ul>
 *   <li>&lt;build&gt;</li>
 *   <li>&lt;reporting&gt;</li>
 *   <li>&lt;dependencies&gt;</li>
 *   <li>&lt;repositories&gt;</li>
 *   <li>&lt;pluginRepositories&gt;</li>
 *   <li>&lt;profiles&gt;</li>
 *   <li>&lt;modules&gt;</li>
 * </ul>
 *
 * @author Andres Almiray
 * @since 1.0.0
 */
public class BomChecker {
    public static class Configuration {
        private boolean failOnError;

        public boolean isFailOnError() {
            return failOnError;
        }

        /**
         * Sets the value for {@code failOnError}.
         *
         * @param failOnError if {@code true} fails the build when an error is encountered.
         */
        public Configuration withFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
            return this;
        }
    }

    /**
     * Checks the resolved model of the given MaveProject for compliance.
     *
     * @param log           the logger to use.
     * @param project       the project to be checked.
     * @param configuration configuration required for inspection.
     * @throws PomCheckException if the POM is invalid
     */
    public static void check(Logger log, MavenProject project, Configuration configuration) throws PomCheckException {
        Model model = project.getOriginalModel();

        List<String> errors = new ArrayList<>();

        // 1. is it packaged as 'pom'?
        log.debug("Checking <packaging>");
        if (!"pom".equals(model.getPackaging())) {
            errors.add("The value of <packaging> must be 'pom'.");
        }

        log.debug("Checking <dependencyManagement>");
        // 2. must have a <dependencyManagement> block
        if (null != model.getDependencyManagement()) {
            List<Dependency> dependencies = model.getDependencyManagement().getDependencies();
            if (dependencies == null || dependencies.isEmpty()) {
                errors.add("No dependencies have been defined in <dependencyManagement>.");
            }
        } else {
            errors.add("No <dependencyManagement> block has been defined.");
        }

        log.debug("Checking <build>");
        if (null != model.getBuild()) {
            errors.add("The <build> block should not be present.");
        }

        log.debug("Checking <reporting>");
        if (null != model.getReporting()) {
            errors.add("The <reporting> block should not be present.");
        }

        log.debug("Checking <dependencies>");
        if (null != model.getDependencies() && !model.getDependencies().isEmpty()) {
            errors.add("The <dependencies> block should not be present.");
        }

        log.debug("Checking <repositories>");
        if (null != model.getRepositories() && !model.getRepositories().isEmpty()) {
            errors.add("The <repositories> block should not be present.");
        }

        log.debug("Checking <pluginRepositories>");
        if (null != model.getPluginRepositories() && !model.getPluginRepositories().isEmpty()) {
            errors.add("The <pluginRepositories> block should not be present.");
        }

        log.debug("Checking <profiles>");
        if (null != model.getProfiles() && !model.getProfiles().isEmpty()) {
            errors.add("The <profiles> block should not be present.");
        }

        log.debug("Checking <modules>");
        if (null != model.getModules() && !model.getModules().isEmpty()) {
            errors.add("The <modules> block should not be present.");
        }

        if (!errors.isEmpty()) {
            StringBuilder b = new StringBuilder(lineSeparator())
                .append("The POM file")
                .append(lineSeparator())
                .append(project.getFile().getAbsolutePath())
                .append(lineSeparator())
                .append("is not a valid BOM due to the following reasons:")
                .append(lineSeparator());
            for (String s : errors) {
                b.append(" * ").append(s).append(lineSeparator());
            }

            if (configuration.isFailOnError()) {
                throw new PomCheckException(b.toString());
            } else {
                log.warn(b.toString());
            }
        } else {
            log.info("BOM {} passes all checks.", project.getFile().getAbsolutePath());
        }
    }
}
