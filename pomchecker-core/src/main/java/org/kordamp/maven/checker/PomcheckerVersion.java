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

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Andres Almiray
 * @since 1.9.0
 */
public class PomcheckerVersion {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(PomcheckerVersion.class.getName());
    private static final String JRELEASER_VERSION = BUNDLE.getString("pomchecker_version");
    private static final String BUILD_TIMESTAMP = BUNDLE.getString("build_timestamp");
    private static final String BUILD_REVISION = BUNDLE.getString("build_revision");
    private static final String SEPARATOR = "------------------------------------------------------------%n";
    private static final String POMCHECKER_VERSION_FORMATTED = "pomchecker %s%n";

    public static String getPlainVersion() {
        return JRELEASER_VERSION;
    }

    public static void banner(PrintStream out) {
        banner(out, true);
    }

    public static void banner(PrintStream out, boolean full) {
        banner(newPrintWriter(out), full);
    }

    public static void banner(PrintWriter out) {
        banner(out, true);
    }

    public static void banner(PrintWriter out, boolean full) {
        if (full) {
            out.printf(SEPARATOR);
            out.printf(POMCHECKER_VERSION_FORMATTED, JRELEASER_VERSION);

            String jvm = System.getProperty("java.version") + " (" +
                System.getProperty("java.vendor") + " " +
                System.getProperty("java.vm.version") + ")";

            out.printf(SEPARATOR);
            out.printf("Build timestamp: %s%n", BUILD_TIMESTAMP);
            out.printf("Revision:        %s%n", BUILD_REVISION);
            out.printf("JVM:             %s%n", jvm);
            out.printf(SEPARATOR);
        } else {
            out.printf(POMCHECKER_VERSION_FORMATTED, JRELEASER_VERSION);
        }
    }

    private static PrintWriter newPrintWriter(OutputStream out) {
        return newPrintWriter(out, true);
    }

    private static PrintWriter newPrintWriter(OutputStream out, boolean autoFlush) {
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, UTF_8)), autoFlush);
    }
}
