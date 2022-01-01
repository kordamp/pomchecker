/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 Andres Almiray.
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
package org.kordamp.maven.checker.cli;

import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class Versions implements CommandLine.IVersionProvider {
    private static final ResourceBundle bundle = ResourceBundle.getBundle(Versions.class.getName());
    private static final String POMCHECKER_VERSION = bundle.getString("pomchecker_version");

    @Override
    public String[] getVersion() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        banner(new PrintStream(baos));
        return baos.toString().split("\n");
    }

    public static void banner(PrintStream out) {
        Manifest manifest = findMyManifest();
        if (null != manifest) {
            String version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            String buildTimestamp = manifest.getMainAttributes().getValue("Build-Timestamp");
            String buildRevision = manifest.getMainAttributes().getValue("Build-Revision");
            boolean additionalInfo = isNotBlank(buildTimestamp) || isNotBlank(buildRevision);

            out.println("------------------------------------------------------------");
            out.println("PomChecker " + version);
            out.println("------------------------------------------------------------");
            if (additionalInfo) {
                if (isNotBlank(buildTimestamp)) {
                    out.println("Build timestamp: " + buildTimestamp);
                }
                if (isNotBlank(buildRevision)) out.println("Revision:        " + buildRevision);
                out.println("------------------------------------------------------------");
            }
        } else {
            out.println(POMCHECKER_VERSION);
        }
    }

    private static Manifest findMyManifest() {
        try {
            Enumeration<URL> urls = Versions.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                Manifest manifest = new Manifest(url.openStream());
                if (manifest.getMainAttributes().containsKey(Attributes.Name.IMPLEMENTATION_TITLE)) {
                    String specificationTitle = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_TITLE);
                    if ("pomchecker-cli".equals(specificationTitle)) {
                        return manifest;
                    }
                }
            }
        } catch (IOException e) {
            // well, this sucks
        }

        return null;
    }

    public static boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
