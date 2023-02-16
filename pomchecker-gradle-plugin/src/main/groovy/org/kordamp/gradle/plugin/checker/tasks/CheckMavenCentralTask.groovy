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
package org.kordamp.gradle.plugin.checker.tasks

import groovy.transform.CompileStatic
import org.apache.maven.project.MavenProject
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
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
    final Property<Boolean> noRelease

    @Input
    final Property<Boolean> noStrict

    @Input
    final Property<Boolean> noFailOnError

    @Input
    final Property<Boolean> failOnWarning

    @Internal
    final Property<GradleLoggerAdapter> glogger

    @Inject
    CheckMavenCentralTask(ObjectFactory objects) {
        pomFile = objects.fileProperty()
        noRelease = objects.property(Boolean).convention(false)
        noStrict = objects.property(Boolean).convention(false)
        noFailOnError = objects.property(Boolean).convention(false)
        failOnWarning = objects.property(Boolean).convention(false)
        glogger = objects.property(GradleLoggerAdapter)
    }

    @Option(option = 'no-release', description = 'Allows `-SNAPSHOT` versions if set to `true`')
    void setNoRelease(boolean noRelease) {
        this.noRelease.set(noRelease)
    }

    @Option(option = 'no-strict', description = 'Allows `<repositories>` and `<pluginRepositories>` if set to `true`')
    void setNoStrict(boolean noStrict) {
        this.noStrict.set(noStrict)
    }

    @Option(option = 'no-fail-on-error', description = 'Skips failing the build if set')
    void setNoFailOnError(boolean noFailOnError) {
        this.noFailOnError.set(noFailOnError)
    }

    @Option(option = 'fail-on-warning', description = 'Fails the build if set to `true`')
    void setFailOnWarning(boolean failOnWarning) {
        this.failOnWarning.set(failOnWarning)
    }

    @TaskAction
    void check() {
        MavenProject mavenProject = PomParser.createMavenProject(pomFile.getAsFile().get())
        MavenCentralChecker.check(glogger.get(), mavenProject, new MavenCentralChecker.Configuration()
            .withRelease(!noRelease.get())
            .withStrict(!noStrict.get())
            .withFailOnError(!noFailOnError.get())
            .withFailOnWarning(failOnWarning.get()))
    }
}