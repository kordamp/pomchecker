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
package org.kordamp.maven.enforcer.checker;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.kordamp.maven.checker.MavenCentralChecker;
import org.kordamp.maven.checker.PomCheckException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checks if a POM complies with the rules for uploading to Maven Central.
 *
 * @author Andres Almiray
 * @since 1.0.0
 */
public class CheckMavenCentral implements EnforcerRule {
    private boolean release = true;

    private boolean strict = true;

    @Override
    public void execute(@Nonnull EnforcerRuleHelper helper) throws EnforcerRuleException {
        try {
            MavenProject project = (MavenProject) helper.evaluate("${project}");
            MavenCentralChecker.check(helper.getLog(), project, release, strict);
        } catch (ExpressionEvaluationException e) {
            throw new EnforcerRuleException("Unable to lookup an expression " + e.getLocalizedMessage(), e);
        } catch (PomCheckException e) {
            throw new EnforcerRuleException(e.getMessage());
        }
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public boolean isResultValid(@Nonnull EnforcerRule enforcerRule) {
        return false;
    }

    @Nullable
    @Override
    public String getCacheId() {
        return "";
    }
}
