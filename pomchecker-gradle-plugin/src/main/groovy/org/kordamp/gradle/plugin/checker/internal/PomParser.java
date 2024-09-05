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
package org.kordamp.gradle.plugin.checker.internal;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import eu.maveniverse.maven.mima.context.Context;
import eu.maveniverse.maven.mima.context.ContextOverrides;
import eu.maveniverse.maven.mima.context.Runtimes;
import eu.maveniverse.maven.mima.extensions.mmr.MavenModelReader;
import eu.maveniverse.maven.mima.extensions.mmr.ModelRequest;
import eu.maveniverse.maven.mima.extensions.mmr.ModelResponse;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionResolutionException;
import org.kordamp.maven.checker.MavenProject;

import java.io.File;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This class seems 100% copy from pomchecker-core? wth
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
public class PomParser {
    /**
     * Creates {@link MavenProject} object carrying "raw" and "effective" models of file pointed at.
     * <p>
     * Important: to successfully build an effective model for given POM file points at, all the relevant POMs
     * (parent, BOMs imported, etc) MUST BE RESOLVABLE. Hence, there is possibility to provide "extra" remote
     * repositories.
     */
    public static MavenProject createMavenProject(File pomFile, Collection<Path> extraRepositories) {
        requireNonNull(pomFile);
        ContextOverrides.Builder contextOverridesBuilder = ContextOverrides.create().withUserSettings(true).userProperties(detectOsProperties());
        if (extraRepositories != null && !extraRepositories.isEmpty()) {
            AtomicInteger counter = new AtomicInteger(0);
            contextOverridesBuilder
                    .addRepositoriesOp(ContextOverrides.AddRepositoriesOp.APPEND)
                    .repositories(extraRepositories.stream()
                            .map(r -> toArtifactRepository("pomchecker_repository_" + (counter.incrementAndGet()), r.toUri().toString()))
                            .collect(Collectors.toList()));
        }
        try (Context context = Runtimes.INSTANCE.getRuntime().create(contextOverridesBuilder.build())) {
            return createMavenProject(pomFile, context, new MavenModelReader(context));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static MavenProject createMavenProject(File pomFile, Context context, MavenModelReader mavenModelReader) throws ArtifactResolutionException, VersionResolutionException, ArtifactDescriptorException {
        ModelRequest request = ModelRequest.builder().setPomFile(pomFile.toPath()).setRequestContext("pomchecker").build();
        ModelResponse response = mavenModelReader.readModel(request);
        return new MavenProject(
                pomFile,
                response.getRawModel(),
                response.getEffectiveModel()
        );
    }

    private static final CharMatcher LOWER_ALPHA_NUMERIC =
            CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('0', '9'));

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

    private static RemoteRepository toArtifactRepository(String id, String url) {
        RepositoryPolicy policy = new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_NEVER, RepositoryPolicy.CHECKSUM_POLICY_WARN);
        return new RemoteRepository.Builder(id, "default", url).setReleasePolicy(policy).setSnapshotPolicy(policy).build();
    }
}