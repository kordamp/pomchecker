/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 Andres Almiray.
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
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Checks if a POM complies with the rules for uploading to Maven Central.
 *
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
 *
 * All previous blocks may be supplied by a parent POM with the exception of &lt;artifactId&gt;.
 *
 * The following blocks are forbidden if {@code strict = true}
 * <ul>
 *   <li>&lt;repositories&gt;</li>
 *   <li>&lt;pluginRepositories&gt;</li>
 * </ul>
 *
 * @see <a href="http://maven.apache.org/repository/guide-central-repository-upload.html">http://maven.apache.org/repository/guide-central-repository-upload.html</a>
 * @see <a href="https://central.sonatype.org/pages/requirements.html">https://central.sonatype.org/pages/requirements.html</a>
 *
 * @author Andres Almiray
 * @since 1.0.0
 */
public class MavenCentralChecker {
    /**
     * Checks the resolved model of the given MaveProject for compliance.
     *
     * @param log     the logger to use.
     * @param project the project to be checked.
     * @param release if {@code true} checks if version is not -SNAPSHOT.
     * @param strict  if {@code true} checks that &lt;repositories&gt; and &lt;pluginRepositories&gt; are not present
     * @throws PomCheckException if the POM is invalid
     */
    public static void check(Logger log, MavenProject project, boolean release, boolean strict) throws PomCheckException {
        Model fullModel = project.getModel();
        Model originalModel = project.getOriginalModel();

        List<String> errors = new ArrayList<>();

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
        }

        log.debug("Checking <name>");
        if (isBlank(fullModel.getName())) {
            String parentName = resolveParentName(project.getFile().getParentFile(), fullModel);
            if (isBlank(parentName)) {
                errors.add("<name> can not be blank.");
            } else {
                log.warn("<name> is not defined in POM. Will use value from parent: " +
                    System.lineSeparator() + "\t" + parentName);
            }
        }

        log.debug("Checking <description>");
        if (isBlank(fullModel.getDescription())) {
            errors.add("<description> can not be blank.");
        }
        if (isBlank(originalModel.getDescription())) {
            log.warn("<description> is not defined in POM. Will use value from parent: " +
                System.lineSeparator() + "\t" + fullModel.getDescription());
        }

        log.debug("Checking <url>");
        if (isBlank(fullModel.getUrl())) {
            errors.add("<url> can not be blank.");
        }
        if (isBlank(originalModel.getUrl())) {
            log.warn("<url> is not defined in POM. Will use computed value from parent: " +
                System.lineSeparator() + "\t" + fullModel.getUrl());
        }

        if (release) log.debug("Checking if version is not snapshot");
        if (release && fullModel.getVersion().endsWith("-SNAPSHOT")) {
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
            if (strict) {
                errors.add("The <repositories> block should not be present.");
            } else {
                log.warn("The <repositories> block should not be present.");
            }
        }

        log.debug("Checking <pluginRepositories>");
        if (null != originalModel.getPluginRepositories() && !originalModel.getPluginRepositories().isEmpty()) {
            if (strict) {
                errors.add("The <pluginRepositories> block should not be present.");
            } else {
                log.warn("The <pluginRepositories> block should not be present.");
            }
        }

        if (!errors.isEmpty()) {
            StringBuilder b = new StringBuilder("\nThe POM file\n")
                .append(project.getFile().getAbsolutePath())
                .append("\ncannot be uploaded to Maven Central due to the following reasons:\n");
            for (String s : errors) {
                b.append(" * ").append(s).append("\n");
            }

            throw new PomCheckException(b.toString());
        } else {
            log.info("POM {} passes all checks. It be uploaded to Maven Central.", project.getFile().getAbsolutePath());
        }
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
}
