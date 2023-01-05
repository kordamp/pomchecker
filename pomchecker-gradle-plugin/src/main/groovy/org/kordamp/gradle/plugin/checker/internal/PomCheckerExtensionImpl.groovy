/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 Andres Almiray.
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
package org.kordamp.gradle.plugin.checker.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.kordamp.gradle.plugin.checker.PomCheckerExtension
import org.kordamp.gradle.property.BooleanState
import org.kordamp.gradle.property.SimpleBooleanState

/**
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
class PomCheckerExtensionImpl implements PomCheckerExtension {
    private final BooleanState enabled
    private final BooleanState bom
    private final BooleanState release
    private final BooleanState strict

    PomCheckerExtensionImpl(Project project) {
        bom = SimpleBooleanState.of(project, this, 'pomchecker.bom', false)
        enabled = SimpleBooleanState.of(project, this, 'pomchecker.enabled', true)
        release = SimpleBooleanState.of(project, this, 'pomchecker.release', true)
        strict = SimpleBooleanState.of(project, this, 'pomchecker.strict', true)
    }

    @Override
    Property<Boolean> getBom() {
        bom.property
    }

    @Override
    Property<Boolean> getEnabled() {
        enabled.property
    }

    @Override
    Property<Boolean> getRelease() {
        release.property
    }

    @Override
    Property<Boolean> getStrict() {
        strict.property
    }

    Provider<Boolean> getResolvedBom() {
        bom.provider
    }

    Provider<Boolean> getResolvedEnabled() {
        enabled.provider
    }

    Provider<Boolean> getResolvedRelease() {
        release.provider
    }

    Provider<Boolean> getResolvedStrict() {
        strict.provider
    }
}
