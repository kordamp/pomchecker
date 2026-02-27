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
package org.kordamp.maven.checker.cli.internal;

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
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class PomParser {
    public static MavenProject createMavenProject(File pomFile, Set<Path> repositories) {
        try (Context context = Runtimes.INSTANCE.getRuntime().create(ContextOverrides.create().withUserSettings(true).build())) {
            List<RemoteRepository> remoteRepositories = context.remoteRepositories()
                    .stream().map(r -> toArtifactRepository(r.getId(), r.getUrl())).collect(Collectors.toList());
            int i = 0;
            for (Path repository : repositories) {
                remoteRepositories.add(toArtifactRepository("pomchecker_repository_" + (i++), repository.toUri().toString()));
            }

            ModelResponse modelResponse = new MavenModelReader(context)
                    .readModel(ModelRequest.builder()
                            .setPomFile(pomFile.toPath())
                            .setRepositories(remoteRepositories)
                            .build());
            return new MavenProject(pomFile.toPath(), modelResponse.getEffectiveModel(), modelResponse.getRawModel());
        } catch (ArtifactResolutionException | VersionResolutionException | ArtifactDescriptorException e) {
            throw new IllegalStateException(e);
        }
    }

    private static RemoteRepository toArtifactRepository(String id, String url) {
        return new RemoteRepository.Builder(id, "default", url)
                .setReleasePolicy(new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_NEVER, RepositoryPolicy.CHECKSUM_POLICY_WARN))
                .setSnapshotPolicy(new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_NEVER, RepositoryPolicy.CHECKSUM_POLICY_WARN))
                .build();
    }
}
