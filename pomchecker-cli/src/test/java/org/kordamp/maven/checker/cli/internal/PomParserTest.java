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
package org.kordamp.maven.checker.cli.internal;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PomParserTest {

    @Test
    void parse() throws Exception {
        URL resource = getClass().getClassLoader().getResource("test-pom.xml");
        MavenProject mavenProject = PomParser.createMavenProject(new File(resource.toURI()));
        assertEquals("quarkus-slack-parent",mavenProject.getArtifactId());
    }
}
