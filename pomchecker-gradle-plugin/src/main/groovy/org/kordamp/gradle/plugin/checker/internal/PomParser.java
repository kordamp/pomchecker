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

import eu.maveniverse.maven.mima.context.Context;
import eu.maveniverse.maven.mima.context.ContextOverrides;
import eu.maveniverse.maven.mima.context.Runtimes;
import eu.maveniverse.maven.mima.extensions.mmr.MavenModelReader;
import eu.maveniverse.maven.mima.extensions.mmr.ModelLevel;
import eu.maveniverse.maven.mima.extensions.mmr.ModelResponse;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionResolutionException;
import org.kordamp.maven.checker.MavenProject;

import java.io.File;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

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
    public static MavenProject createMavenProject(File pomFile, RemoteRepository... repositories) {
        requireNonNull(pomFile);
        ContextOverrides.Builder contextOverridesBuilder = ContextOverrides.create().withUserSettings(true);
        if (repositories.length > 0) {
            contextOverridesBuilder
                    .addRepositoriesOp(ContextOverrides.AddRepositoriesOp.APPEND)
                    .repositories(Arrays.asList(repositories));
        }
        try (Context context = Runtimes.INSTANCE.getRuntime().create(contextOverridesBuilder.build())) {
            return createMavenProject(pomFile, context, new MavenModelReader(context));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static MavenProject createMavenProject(File pomFile, Context context, MavenModelReader mavenModelReader) throws ArtifactResolutionException, VersionResolutionException, ArtifactDescriptorException {
        ArtifactDescriptorRequest request = new ArtifactDescriptorRequest(new DefaultArtifact("irrelevant:irrelevant:irrelevant").setFile(pomFile), context.remoteRepositories(), "pomchecker");
        ModelResponse response = mavenModelReader.readModel(request);
        return new MavenProject(
                pomFile,
                response.toModel(ModelLevel.RAW),
                response.toModel(ModelLevel.EFFECTIVE)
        );
    }
}