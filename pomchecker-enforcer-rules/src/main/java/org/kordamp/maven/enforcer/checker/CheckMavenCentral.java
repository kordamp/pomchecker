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
package org.kordamp.maven.enforcer.checker;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.project.MavenProject;
import org.kordamp.maven.checker.MavenCentralChecker;
import org.kordamp.maven.checker.PomCheckException;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Checks if a POM complies with the rules for uploading to Maven Central.
 *
 * @author Andres Almiray
 * @since 1.0.0
 */
@Named("checkMavenCentral")
public class CheckMavenCentral extends AbstractEnforcerRule {
    private boolean release = true;
    private boolean strict = true;
    private boolean failOnError = true;
    private boolean failOnWarning;

    public boolean isRelease() {
        return release;
    }

    public CheckMavenCentral setRelease(boolean release) {
        this.release = release;
        return this;
    }

    public boolean isStrict() {
        return strict;
    }

    public CheckMavenCentral setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public CheckMavenCentral setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return this;
    }

    public boolean isFailOnWarning() {
        return failOnWarning;
    }

    public CheckMavenCentral setFailOnWarning(boolean failOnWarning) {
        this.failOnWarning = failOnWarning;
        return this;
    }

    @Inject
    private MavenProject project;

    @Override
    public void execute() throws EnforcerRuleException {
        try {
            MavenCentralChecker.check(new MavenEnforcerLoggerAdapter(getLog()),
                    new org.kordamp.maven.checker.MavenProject(project.getFile().toPath(), project.getModel(), project.getOriginalModel()),
                    new MavenCentralChecker.Configuration()
                      .withRelease(release)
                      .withStrict(strict)
                      .withFailOnError(failOnError)
                      .withFailOnWarning(failOnWarning));
        } catch (PomCheckException e) {
            throw new EnforcerRuleException(e.getMessage());
        }
    }
}
