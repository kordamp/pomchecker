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
package org.kordamp.maven.checker.cli.internal;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.building.StringModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class NoopModelResolver implements ModelResolver {
    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
        return fakePom(groupId, artifactId, version);
    }

    @Override
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
        return fakePom(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
    }

    @Override
    public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
        return fakePom(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    @Override
    public void addRepository(Repository repository) throws InvalidRepositoryException {

    }

    @Override
    public void addRepository(Repository repository, boolean replace) throws InvalidRepositoryException {

    }

    @Override
    public ModelResolver newCopy() {
        return this;
    }

    private ModelSource fakePom(String groupId, String artifactId, String version) {
        return new StringModelSource("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>" + groupId + "</groupId>\n" +
            "    <artifactId>" + artifactId + "</artifactId>\n" +
            "    <version>" + version + "</version>");
    }
}
