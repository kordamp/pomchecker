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
package org.kordamp.gradle.plugin.checker

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.tasks.TaskProvider
import org.kordamp.gradle.annotations.DependsOn
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.checker.internal.GradleLoggerAdapter
import org.kordamp.gradle.plugin.checker.internal.PomCheckerExtensionImpl
import org.kordamp.gradle.plugin.checker.tasks.CheckBomTask
import org.kordamp.gradle.plugin.checker.tasks.CheckMavenCentralTask

import javax.inject.Named

import static org.kordamp.gradle.listener.ProjectEvaluationListenerManager.addAllProjectsEvaluatedListener
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
class PomCheckerPlugin extends AbstractKordampPlugin {
    Project project

    PomCheckerPlugin() {
        super('org.kordamp.gradle.pomchecker')
    }

    void apply(Project project) {
        if (project.gradle.startParameter.logLevel != LogLevel.QUIET) {
            project.gradle.sharedServices
                .registerIfAbsent('pomchecker-banner', Banner, { spec -> })
                .get().display(project)
        }

        this.project = project

        configureRootProject(project)
        project.childProjects.values().each {
            configureProject(it)
        }
    }

    private void configureRootProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        configureProject(project)
        if (isRootProject(project)) {
            addAllProjectsEvaluatedListener(project, new PomCheckerProjectsEvaluatedListener())
        }
    }

    private void configureProject(Project project) {
        project.extensions
            .create(PomCheckerExtension, 'pomchecker', PomCheckerExtensionImpl, project)
    }

    @Named('pomchecker')
    @DependsOn(['publishing'])
    private class PomCheckerProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
        @Override
        void allProjectsEvaluated(Project rootProject) {
            configureTasks(project)
            project.childProjects.values().each {
                configureTasks(it)
            }
        }
    }

    private void configureTasks(Project project) {
        PublishingExtension publishingExtension = project.extensions.findByType(PublishingExtension)
        if (!publishingExtension) return

        PomCheckerExtensionImpl pomCheckerExtension = (PomCheckerExtensionImpl) project.extensions.findByType(PomCheckerExtension)

        GradleLoggerAdapter glogger = new GradleLoggerAdapter(project.logger)

        publishingExtension.publications.getAsMap().each { String publicationName, Publication publication ->
            if (publicationName.toLowerCase().contains('pluginmarker')) return

            GenerateMavenPom generateMavenPomTask = (GenerateMavenPom) project.tasks.findByName('generatePomFileFor' + publicationName.capitalize() + 'Publication')
            if (!generateMavenPomTask) return

            if (pomCheckerExtension.resolvedBom.get()) {
                registerCheckBomTask(project, glogger, publicationName, generateMavenPomTask, pomCheckerExtension)
            }
            registerCheckMavenCentralTask(project, glogger, publicationName, generateMavenPomTask, pomCheckerExtension)
        }
    }

    private void registerCheckBomTask(Project project,
                                      GradleLoggerAdapter glogger,
                                      String publicationName,
                                      GenerateMavenPom generateMavenPomTask,
                                      PomCheckerExtensionImpl pomCheckerExtension) {
        TaskProvider<CheckBomTask> checkBomTask = project.tasks.register("checkBom${publicationName.capitalize()}".toString(), CheckBomTask,
            new Action<CheckBomTask>() {
                @Override
                void execute(CheckBomTask t) {
                    t.group = 'Publishing'
                    t.description = "Checks if the ${publicationName} POM is a valid BOM"
                    t.pomFile.set(generateMavenPomTask.destination)
                    t.dependsOn(generateMavenPomTask)
                    t.enabled = pomCheckerExtension.resolvedEnabled.get()
                    t.noFailOnError.convention(!pomCheckerExtension.resolvedFailOnError.get())
                    t.glogger.set(glogger)
                }
            })
        generateMavenPomTask.finalizedBy(checkBomTask)
    }

    private void registerCheckMavenCentralTask(Project project,
                                               GradleLoggerAdapter glogger,
                                               String publicationName,
                                               GenerateMavenPom generateMavenPomTask,
                                               PomCheckerExtensionImpl pomCheckerExtension) {
        TaskProvider<CheckMavenCentralTask> checkMavenCentralTask = project.tasks.register("checkMavenCentral${publicationName.capitalize()}".toString(), CheckMavenCentralTask,
            new Action<CheckMavenCentralTask>() {
                @Override
                void execute(CheckMavenCentralTask t) {
                    t.group = 'Publishing'
                    t.description = "Checks if the ${publicationName} POM can be published to Maven Central"
                    t.pomFile.set(generateMavenPomTask.destination)
                    t.noRelease.convention(!pomCheckerExtension.resolvedRelease.get())
                    t.noStrict.convention(!pomCheckerExtension.resolvedStrict.get())
                    t.noFailOnError.convention(!pomCheckerExtension.resolvedFailOnError.get())
                    t.failOnWarning = pomCheckerExtension.resolvedFailOnWarning.get()
                    t.dependsOn(generateMavenPomTask)
                    t.enabled = pomCheckerExtension.resolvedEnabled.get()
                    t.glogger.set(glogger)
                }
            })
        generateMavenPomTask.finalizedBy(checkMavenCentralTask)
    }

    static void applyIfMissing(Project project) {
        if (!project.plugins.findPlugin(PomCheckerPlugin)) {
            project.pluginManager.apply(PomCheckerPlugin)
        }
    }
}