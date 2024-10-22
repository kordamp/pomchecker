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

import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.lineSeparator;

/**
 * Checks if a POM complies with the rules for uploading to Maven Central.
 * <p>
 * The following blocks are required:
 * <ul>
 *   <li>&lt;groupId&gt;</li>
 *   <li>&lt;artifactId&gt;</li>
 *   <li>&lt;version&gt;</li>
 *   <li>&lt;name&gt;</li>
 *   <li>&lt;description&gt;</li>
 *   <li>&lt;url&gt;</li>
 *   <li>&lt;licenses&gt;</li>
 *   <li>&lt;scm&gt;</li>
 * </ul>
 * <p>
 * All previous blocks may be supplied by a parent POM with the exception of &lt;artifactId&gt;.
 * <p>
 * The following blocks are forbidden if {@code strict = true}
 * <ul>
 *   <li>&lt;repositories&gt;</li>
 *   <li>&lt;pluginRepositories&gt;</li>
 * </ul>
 *
 * @author Andres Almiray
 * @see <a href="http://maven.apache.org/repository/guide-central-repository-upload.html">http://maven.apache.org/repository/guide-central-repository-upload.html</a>
 * @see <a href="https://central.sonatype.org/pages/requirements.html">https://central.sonatype.org/pages/requirements.html</a>
 * @since 1.0.0
 */
public class MavenCentralChecker {
    public static class Configuration {
        private boolean release;
        private boolean strict;
        private boolean failOnError;
        private boolean failOnWarning;

        public boolean isRelease() {
            return release;
        }

        /**
         * Sets the value for {@code release}.
         *
         * @param release if {@code true} checks if version is not -SNAPSHOT.
         */
        public Configuration withRelease(boolean release) {
            this.release = release;
            return this;
        }

        public boolean isStrict() {
            return strict;
        }

        /**
         * Sets the value for {@code strict}.
         *
         * @param strict if {@code true} checks that &lt;repositories&gt; and &lt;pluginRepositories&gt; are not present
         */
        public Configuration withStrict(boolean strict) {
            this.strict = strict;
            return this;
        }

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

        public boolean isFailOnWarning() {
            return failOnWarning;
        }

        /**
         * Sets the value for {@code failOnWarning}.
         *
         * @param failOnWarning if {@code true} fails the build when a warning is encountered.
         */
        public Configuration withFailOnWarning(boolean failOnWarning) {
            this.failOnWarning = failOnWarning;
            return this;
        }
    }

    /**
     * Checks the resolved model of the given MavenProject for compliance.
     *
     * @param log           the logger to use.
     * @param project       the project to be checked.
     * @param configuration configuration required for inspection.
     * @throws PomCheckException if the POM is invalid
     */
    public static void check(Logger log, MavenProject project, Configuration configuration) throws PomCheckException {
        Model fullModel = project.getModel();
        Model originalModel = project.getOriginalModel();

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // sanity checks. redundant?
        log.debug("Checking <groupId>");
        if (isBlank(fullModel.getGroupId())) {
            errors.add("<groupId> can not be blank.");
        }
        log.debug("Checking <artifactId>");
        if (isBlank(fullModel.getArtifactId())) {
            errors.add("<artifactId> can not be blank.");
        }
        log.debug("Checking <version>");
        if (isBlank(fullModel.getVersion())) {
            errors.add("<version> can not be blank.");
        } else if (fullModel.getVersion().contains("${")) {
            errors.add("<version> contains an unresolved expression: " + fullModel.getVersion());
        }
        if (isRelocated(fullModel)) {
            log.debug("Checking <distributionManagement>/<relocation>");
            Relocation relocation = fullModel.getDistributionManagement().getRelocation();
            if (isBlank(relocation.getGroupId()) && isBlank(relocation.getArtifactId()) && isBlank(relocation.getVersion())) {
                errors.add("<distributionManagement>/<relocation> requires either <groupId>, <artifactId> or <version>.");
            }
        } else {
            log.debug("Checking <name>");
            if (isBlank(fullModel.getName())) {
                String parentName = resolveParentName(project.getFile().getParentFile(), fullModel);
                if (isBlank(parentName)) {
                    errors.add("<name> can not be blank.");
                } else {
                    warnings.add("<name> is not defined in POM. Will use value from parent: " +
                        lineSeparator() + "\t" + parentName);
                }
            }

            log.debug("Checking <description>");
            if (isBlank(fullModel.getDescription())) {
                errors.add("<description> can not be blank.");
            }
            if (isBlank(originalModel.getDescription())) {
                warnings.add("<description> is not defined in POM. Will use value from parent: " +
                    lineSeparator() + "\t" + fullModel.getDescription());
            }

            log.debug("Checking <url>");
            if (isBlank(fullModel.getUrl())) {
                errors.add("<url> can not be blank.");
            }
            if (isBlank(originalModel.getUrl())) {
                warnings.add("<url> is not defined in POM. Will use computed value from parent: " +
                    lineSeparator() + "\t" + fullModel.getUrl());
            }

            if (configuration.isRelease()) log.debug("Checking if version is not snapshot");
            if (configuration.isRelease() && fullModel.getVersion().endsWith("-SNAPSHOT")) {
                errors.add("<version> can not be -SNAPSHOT.");
            }

            log.debug("Checking <licenses>");
            if (fullModel.getLicenses() != null) {
                if (!fullModel.getLicenses().isEmpty()) {
                    // verify that all licenses have <name> & <url>
                    for (int i = 0; i < fullModel.getLicenses().size(); i++) {
                        License license = fullModel.getLicenses().get(i);
                        if (isBlank(license.getName()) && isBlank(license.getUrl())) {
                            errors.add("License " + i + " must define <name> and <url>.");
                        }
                    }
                } else {
                    errors.add("<licenses> block is required but was left empty.");
                }
            } else {
                errors.add("<licenses> block is required but was left undefined.");
            }

            log.debug("Checking <developers>");
            if (fullModel.getDevelopers() != null) {
                if (!fullModel.getDevelopers().isEmpty()) {
                    // verify that all developers have at least one of [id, name, organization, organizationUrl]
                    for (int i = 0; i < fullModel.getDevelopers().size(); i++) {
                        Developer developer = fullModel.getDevelopers().get(i);
                        if (isBlank(developer.getId()) &&
                            isBlank(developer.getName()) &&
                            isBlank(developer.getOrganization()) &&
                            isBlank(developer.getOrganizationUrl())) {
                            errors.add("Developer " + i + " must define at least one of <id>, <name>, <organization>, <organizationUrl>.");
                        }
                    }
                } else {
                    errors.add("<developers> block is required but was left empty.");
                }
            } else {
                errors.add("<developers> block is required but was left undefined.");
            }

            log.debug("Checking <scm>");
            if (fullModel.getScm() == null) {
                errors.add("The <scm> block is required.");
            }

            log.debug("Checking <repositories>");
            if (null != originalModel.getRepositories() && !originalModel.getRepositories().isEmpty()) {
                if (configuration.isStrict()) {
                    errors.add("The <repositories> block should not be present.");
                } else {
                    warnings.add("The <repositories> block should not be present.");
                }
            }

            log.debug("Checking <pluginRepositories>");
            if (null != originalModel.getPluginRepositories() && !originalModel.getPluginRepositories().isEmpty()) {
                if (configuration.isStrict()) {
                    errors.add("The <pluginRepositories> block should not be present.");
                } else {
                    warnings.add("The <pluginRepositories> block should not be present.");
                }
            }
        }
        if (!warnings.isEmpty()) {
            if (configuration.isFailOnWarning()) {
                throw new PomCheckException(String.join(lineSeparator(), warnings));
            } else {
                warnings.forEach(log::warn);
            }
        }

        if (!errors.isEmpty()) {
            StringBuilder b = new StringBuilder(lineSeparator())
                .append("The POM file")
                .append(lineSeparator())
                .append(project.getFile().getAbsolutePath())
                .append(lineSeparator())
                .append("cannot be uploaded to Maven Central due to the following reasons:")
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
            log.info("POM {} passes all checks. It can be uploaded to Maven Central.", project.getFile().getAbsolutePath());
        }
    }

    /**
     * Checks if the model has been relocated.
     *
     * @param model the model to check
     * @return {@code true} if the model has been relocated.
     */
    private static boolean isRelocated(Model model) {
        return model.getDistributionManagement() != null && model.getDistributionManagement().getRelocation() != null;
    }

    private static String resolveParentName(File directory, Model fullModel) {
        Parent parent = fullModel.getParent();

        while (parent != null) {
            if (isNotBlank(parent.getRelativePath())) {
                File pomFile = new File(directory, parent.getRelativePath());
                if (!pomFile.getName().endsWith("pom.xml")) {
                    pomFile = new File(pomFile, "pom.xml");
                }

                if (pomFile.exists()) {
                    MavenProject parentProject = readProject(pomFile);
                    Model parentModel = parentProject.getModel();
                    if (isNotBlank(parentModel.getName())) {
                        return parentModel.getName();
                    } else {
                        directory = pomFile.getParentFile();
                        parent = parentModel.getParent();
                    }
                } else {
                    // parent should be available from a repository
                    // TODO: resolve parent
                    return null;
                }
            } else {
                // check 1 level up
                File pomFile = new File(directory, "../pom.xml");
                if (pomFile.exists()) {
                    MavenProject parentProject = readProject(pomFile);
                    Model parentModel = parentProject.getModel();
                    if (isNotBlank(parentModel.getName())) {
                        return parentModel.getName();
                    } else {
                        directory = pomFile.getParentFile();
                        parent = parentModel.getParent();
                    }
                } else {
                    // parent should be available from a repository
                    // TODO: resolve parent
                    return null;
                }
            }
        }

        return null;
    }

    private static MavenProject readProject(File pom) {
        try {
            FileReader reader = new FileReader(pom);
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            return new MavenProject(mavenReader.read(reader));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
