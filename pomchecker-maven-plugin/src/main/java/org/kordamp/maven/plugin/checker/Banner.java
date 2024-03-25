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
package org.kordamp.maven.plugin.checker;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public final class Banner {
    private static final String ORG_KORDAMP_BANNER = "org.kordamp.banner";
    private static final Banner INSTANCE = new Banner();
    private final ResourceBundle bundle = ResourceBundle.getBundle(Banner.class.getName());
    private final String productVersion = bundle.getString("product.version");
    private final String productId = bundle.getString("product.id");
    private final String productName = bundle.getString("product.name");
    private final String message = MessageFormat.format(bundle.getString("product.banner"), productName, productVersion);
    private final List<String> visited = new ArrayList<>();

    private Banner() {
        // noop
    }

    private File getMarkerFile(File parent) {
        return new File(parent,
            "kordamp" +
                File.separator +
                productId +
                File.separator +
                productVersion +
                File.separator +
                "marker.txt");
    }

    public static void display(MavenProject project, Log log) {
        if (INSTANCE.visited.contains(project.getName())) {
            return;
        }

        INSTANCE.visited.add(project.getName());

        boolean quiet = log.isErrorEnabled() &&
            !log.isWarnEnabled() &&
            !log.isInfoEnabled() &&
            !log.isDebugEnabled();

        boolean printBanner = null == System.getProperty(ORG_KORDAMP_BANNER) || Boolean.getBoolean(ORG_KORDAMP_BANNER);

        try {
            File parent = new File(System.getProperty("user.home"), ".m2/caches");
            File markerFile = INSTANCE.getMarkerFile(parent);
            if (!markerFile.exists()) {
                if (printBanner && !quiet) System.out.println(INSTANCE.message);
                markerFile.getParentFile().mkdirs();
                PrintStream out = new PrintStream(new FileOutputStream(markerFile));
                out.println("1");
                out.close();
                writeQuietly(markerFile, "1");
            } else {
                try {
                    int count = Integer.parseInt(readQuietly(markerFile));
                    if (count < 3) {
                        if (printBanner && !quiet) System.out.println(INSTANCE.message);
                    }
                    writeQuietly(markerFile, (count + 1) + "");
                } catch (NumberFormatException e) {
                    writeQuietly(markerFile, "1");
                    if (printBanner && !quiet) System.out.println(INSTANCE.message);
                }
            }
        } catch (IOException ignored) {
            // noop
        }
    }

    private static void writeQuietly(File file, String text) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(file));
            out.println(text);
            out.close();
        } catch (IOException ignored) {
            // ignored
        }
    }

    private static String readQuietly(File file) {
        try {
            Scanner in = new Scanner(new FileInputStream(file));
            return in.next();
        } catch (Exception ignored) {
            return "";
        }
    }
}
