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
package org.kordamp.maven.enforcer.checker;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.project.MavenProject;
import org.kordamp.maven.checker.BomChecker;
import org.kordamp.maven.checker.PomCheckException;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Checks if a POM file is a minimal BOM file.
 * Minimal BOM files contain the following elements:
 * <ul>
 * <li>&lt;groupId&gt;</li>
 * <li>&lt;artifactId&gt;</li>
 * <li>&lt;version&gt;</li>
 * <li>&lt;dependencyManagement&gt;</li>
 * </ul>
 *
 * @author Andres Almiray
 * @since 1.0.0
 */
@Named("checkBom")
public class CheckBom extends AbstractEnforcerRule {
    private boolean failOnError;

    public boolean isFailOnError() {
        return failOnError;
    }

    public CheckBom setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return this;
    }

    @Inject
    private MavenProject project;

    @Override
    public void execute() throws EnforcerRuleException {
        try {
            BomChecker.check(new MavenEnforcerLoggerAdapter(getLog()), new org.kordamp.maven.checker.MavenProject(project.getFile(), project.getOriginalModel(), project.getModel()), new BomChecker.Configuration()
                .withFailOnError(failOnError));
        } catch (PomCheckException e) {
            throw new EnforcerRuleException(e.getMessage());
        }
    }
}
