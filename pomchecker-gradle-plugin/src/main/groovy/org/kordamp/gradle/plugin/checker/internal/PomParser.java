/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 Andres Almiray.
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
package org.kordamp.gradle.plugin.checker.internal;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import eu.maveniverse.maven.mima.context.Context;
import eu.maveniverse.maven.mima.context.ContextOverrides;
import eu.maveniverse.maven.mima.context.Runtimes;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.util.Locale;
import java.util.Properties;

import java.util.stream.Collectors;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class PomParser {
    private static final CharMatcher LOWER_ALPHA_NUMERIC =
        CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('0', '9'));

    public static MavenProject createMavenProject(File pomFile) {
        // HACK: MIMA provides sisu runtime, but we need Maven components as well,
        // that are Plexus still. Hence, we "wrap" and boot Plexus around MIMA, and this
        // awakens MIMA eager singleton activator.
        ClassWorld classWorld =
            new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
        ContainerConfiguration containerConfiguration =
            new DefaultContainerConfiguration()
                .setClassWorld(classWorld)
                .setRealm(classWorld.getClassRealm("plexus.core"))
                .setClassPathScanning(PlexusConstants.SCANNING_INDEX)
                .setAutoWiring(true)
                .setJSR250Lifecycle(true)
                .setName("pom-reader");
        try {
            PlexusContainer container = new DefaultPlexusContainer(containerConfiguration);
            try (Context context = Runtimes.INSTANCE.getRuntime().create(ContextOverrides.create().withUserSettings(true).build())) {
                return createMavenProject(pomFile, context, container);
            }
        } catch (PlexusContainerException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static MavenProject createMavenProject(File pomFile, Context context, PlexusContainer plexusContainer) {
        try {
            MavenExecutionRequest mavenExecutionRequest = new DefaultMavenExecutionRequest();
            ProjectBuildingRequest projectBuildingRequest =
                mavenExecutionRequest.getProjectBuildingRequest();

            projectBuildingRequest.setRepositorySession(context.repositorySystemSession());
            projectBuildingRequest.setRemoteRepositories(context.remoteRepositories()
                .stream().map(PomParser::toArtifactRepository).collect(Collectors.toList()));

            // Profile activation needs properties such as JDK version
            Properties properties = new Properties(); // allowing duplicate entries
            properties.putAll(projectBuildingRequest.getSystemProperties());
            properties.putAll(detectOsProperties());
            properties.putAll(System.getProperties());
            projectBuildingRequest.setSystemProperties(properties);

            ProjectBuilder projectBuilder = plexusContainer.lookup(ProjectBuilder.class);
            ProjectBuildingResult projectBuildingResult =
                projectBuilder.build(pomFile, projectBuildingRequest);
            return projectBuildingResult.getProject();
        } catch (ComponentLookupException | ProjectBuildingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static ImmutableMap<String, String> detectOsProperties() {
        return ImmutableMap.of(
            "os.detected.name",
            osDetectedName(),
            "os.detected.arch",
            osDetectedArch(),
            "os.detected.classifier",
            osDetectedName() + "-" + osDetectedArch());
    }

    private static String osDetectedName() {
        String osNameNormalized =
            LOWER_ALPHA_NUMERIC.retainFrom(System.getProperty("os.name").toLowerCase(Locale.ENGLISH));

        if (osNameNormalized.startsWith("macosx") || osNameNormalized.startsWith("osx")) {
            return "osx";
        } else if (osNameNormalized.startsWith("windows")) {
            return "windows";
        }
        // Since we only load the dependency graph, not actually use the
        // dependency, it doesn't matter a great deal which one we pick.
        return "linux";
    }

    private static String osDetectedArch() {
        String osArchNormalized =
            LOWER_ALPHA_NUMERIC.retainFrom(System.getProperty("os.arch").toLowerCase(Locale.ENGLISH));
        switch (osArchNormalized) {
            case "x8664":
            case "amd64":
            case "ia32e":
            case "em64t":
            case "x64":
                return "x86_64";
            default:
                return "x86_32";
        }
    }

    private static MavenArtifactRepository toArtifactRepository(RemoteRepository remoteRepository) {
        MavenArtifactRepository mavenArtifactRepository = new MavenArtifactRepository();
        mavenArtifactRepository.setId(remoteRepository.getId());
        mavenArtifactRepository.setUrl(remoteRepository.getUrl());
        mavenArtifactRepository.setLayout(new DefaultRepositoryLayout());
        return mavenArtifactRepository;
    }
}