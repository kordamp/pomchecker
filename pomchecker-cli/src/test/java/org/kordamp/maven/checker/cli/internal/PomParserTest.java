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

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;
import org.kordamp.maven.checker.MavenProject;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PomParserTest {

    @Test
    void parseSingle() throws Exception {
        URL resource = getClass().getClassLoader().getResource("test-pom.xml");
        MavenProject mavenProject = PomParser.createMavenProject(new File(resource.toURI()), Collections.emptySet());
        assertEquals("quarkus-slack-parent", mavenProject.getEffectiveModel().get().getArtifactId());
    }

    @Test
    void parseWithLocalRepository() throws Exception {
        URL resource = getClass().getClassLoader().getResource("repository/com/acme/child/1.0.0/child-1.0.0.pom");
        URI uri = getClass().getClassLoader().getResource("repository").toURI();
        Set<Path> repositories = Collections.singleton(new File(uri).toPath());
        MavenProject mavenProject = PomParser.createMavenProject(new File(resource.toURI()), repositories);
        Model effectiveModel = mavenProject.getEffectiveModel().get();
        assertEquals("com.acme", effectiveModel.getGroupId());
        assertEquals("child", effectiveModel.getArtifactId());
        assertEquals("1.0.0", effectiveModel.getVersion());
    }
}
