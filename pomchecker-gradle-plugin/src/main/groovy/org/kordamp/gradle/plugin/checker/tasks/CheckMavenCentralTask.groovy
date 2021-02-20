/*
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
package org.kordamp.gradle.plugin.checker.tasks

import groovy.transform.CompileStatic
import org.apache.maven.project.MavenProject
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.kordamp.gradle.plugin.checker.internal.GradleLoggerAdapter
import org.kordamp.gradle.plugin.checker.internal.PomParser
import org.kordamp.maven.checker.MavenCentralChecker

import javax.inject.Inject

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
class CheckMavenCentralTask extends DefaultTask {
    @InputFile
    final RegularFileProperty pomFile

    @Input
    final Property<Boolean> release

    @Input
    final Property<Boolean> strict

    @Inject
    CheckMavenCentralTask(ObjectFactory objects) {
        pomFile = objects.fileProperty()
        release = objects.property(Boolean).convention(true)
        strict = objects.property(Boolean).convention(true)
    }

    @Option(option = 'release', description = 'Disallows `-SNAPSHOT` versions if set to `true`')
    void setRelease(boolean release) {
        getRelease().set(!release)
    }

    @Option(option = 'strict', description = 'Disallows `<repositories>` and `<pluginRepositories>` if set to `true`')
    void setStrict(boolean strict) {
        getStrict().set(!strict)
    }

    @TaskAction
    void check() {
        MavenProject mavenProject = PomParser.createMavenProject(pomFile.getAsFile().get())
        MavenCentralChecker.check(new GradleLoggerAdapter(project.logger), mavenProject, release.get(), strict.get())
    }
}