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
package org.kordamp.maven.checker;

import org.apache.maven.model.Model;

import java.nio.file.Path;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class MavenProject {
    private final Path pom;
    private final Model effectiveModel;
    private final Model rawModel;

    public MavenProject(Path pom, Model effectiveModel, Model rawModel) {
        this.pom = requireNonNull(pom);
        this.effectiveModel = effectiveModel;
        this.rawModel = rawModel;
    }

    public Path getPom() {
        return pom;
    }

    public Optional<Model> getEffectiveModel() {
        return Optional.ofNullable(effectiveModel);
    }

    public Optional<Model> getRawModel() {
        return Optional.ofNullable(rawModel);
    }
}
