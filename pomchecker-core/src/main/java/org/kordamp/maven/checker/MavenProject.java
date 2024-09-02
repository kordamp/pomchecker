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

import org.apache.maven.model.Model;

import java.io.File;

import static java.util.Objects.requireNonNull;

public final class MavenProject {
    private final File pomFile;
    private final Model rawModel;
    private final Model effectiveModel;

    public MavenProject(File pomFile, Model rawModel, Model effectiveModel) {
        this.pomFile = requireNonNull(pomFile);
        this.rawModel = requireNonNull(rawModel);
        this.effectiveModel = effectiveModel;
    }

    public File getPomFile() {
        return pomFile;
    }

    public Model getRawModel() {
        return rawModel;
    }

    public Model getEffectiveModel() {
        return effectiveModel;
    }
}
