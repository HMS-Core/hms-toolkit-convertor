/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import sun.applet.Main;

class gradle_download {
    private static final String MAVEN_BASE_URL = "http://szg1.artifactory.inhuawei.com/artifactory/sz-maven-public";
    private static final PrintStream Out = System.out;

    public static void showHelper() {
        final String mainClass = Main.class.getName();

        Out.println("*** " + mainClass + " ***");
        Out.println();
        Out.println("USAGE:");
        Out.println("  Used to download archives from maven repositories.");
        Out.println();
        Out.println("  ~ " + mainClass + " <group> <artifact> <version> [destDir]");
        Out.println("  ~ " + mainClass + " <group>:<artifact>:<version>[:classifier] [destDir]");
        Out.println();
    }

    public static void main(String[] args) throws Exception {
        String[] fixedArgs = new String[5];
        if (args.length == 1 || args.length == 2) {
            // <group>:<artifact>:<version>[:classifier] [destDir]

            String[] gavc = args[0].split(":");
            if (gavc.length == 3 || gavc.length == 4) {
                fixedArgs[0] = gavc[0];
                fixedArgs[1] = gavc[1];
                fixedArgs[2] = gavc[2];
                fixedArgs[3] = gavc.length == 4 ? gavc[3] : "";
                fixedArgs[4] = args.length == 2 ? args[1] : ".";

                main0(fixedArgs);
                return;
            }
        } else if (args.length == 3 || args.length == 4) {
            // <group> <artifact> <version> [destDir]

            fixedArgs[0] = args[0];
            fixedArgs[1] = args[1];
            fixedArgs[2] = args[2];
            fixedArgs[3] = "";
            fixedArgs[4] = args.length == 4 ? args[3] : ".";

            main0(fixedArgs);
            return;
        }

        showHelper();
        return;
    }

    public static void main0(String[] args) throws Exception {
        String group = args[0];
        String artifact = args[1];
        String version = args[2];
        String classifier = args[3];
        String destDir = args[4];

        long start = System.currentTimeMillis();
        String result;
        try {
            doWork(group, artifact, version, classifier, destDir);
            result = "[SUCCESS]";
        } catch (Exception e) {
            e.printStackTrace();
            result = "[FAILURE]";
        }
        long end = System.currentTimeMillis();

        String mavenTarget;
        if (!classifier.isEmpty()) {
            mavenTarget = group + ':' + artifact + ':' + version + ':' + classifier;
        } else {
            mavenTarget = group + ':' + artifact + ':' + version;
        }
        Out.println(result + " " + (end - start) + "ms, to get '" + mavenTarget + "'.");
    }

    static File doWork(String group, String artifact, String version, String classifier, String destDir)
            throws Exception {
        InputStream is = null;
        try {
            String pomUrl = getUrl(group, artifact, version, classifier, "pom");
            Document doc = getDocument(is = new URL(pomUrl).openStream());
            String packaging = getStringValue(doc, "project/packaging/text()");
            if (packaging == null || packaging.isEmpty() || packaging.equals("bundle")) {
                packaging = "jar";
            }

            File localFile = new File(destDir, getFileName(group, artifact, version, classifier, packaging));
            if (localFile.getParentFile().isDirectory() || localFile.getParentFile().mkdirs()) {
                if (localFile.exists()) {
                    localFile.delete();
                }
            } else {
                throw new IOException("Failed to create the directory.");
            }

            getFile(getUrl(group, artifact, version, classifier, packaging), localFile);

            return localFile;
        } finally {
            closeQuiet(is);
        }
    }

    static void getFile(String url, File file) throws Exception {
        InputStream is = null;
        OutputStream os = null;
        try {
            BufferedInputStream bis = new BufferedInputStream(is = new URL(url).openStream());
            BufferedOutputStream bos = new BufferedOutputStream(os = new FileOutputStream(file));

            byte dataBuffer[] = new byte[2048];
            int bytesRead;
            while ((bytesRead = bis.read(dataBuffer, 0, 2048)) != -1) {
                bos.write(dataBuffer, 0, bytesRead);
            }
            bos.flush();
            closeQuiet(bos);
            closeQuiet(bis);
        } finally {
            closeQuiet(is);
            closeQuiet(os);
        }
    }

    static String getSnapsotVersion(String group, String artifact, String version) throws Exception {
        String metadataUrl = MAVEN_BASE_URL //
                + '/' + group.replace('.', '/') //
                + '/' + artifact //
                + '/' + version //
                + '/' + "maven-metadata.xml";

        InputStream is = null;
        try {
            Document doc = getDocument(is = new URL(metadataUrl).openStream());

            String timestamp = getStringValue(doc, "metadata/versioning/snapshot/timestamp/text()");
            String buildNumber = getStringValue(doc, "metadata/versioning/snapshot/buildNumber/text()");

            return version.replace("SNAPSHOT", timestamp + '-' + buildNumber);
        } finally {
            closeQuiet(is);
        }
    }

    static String getUrl(String group, String artifact, String version, String classifier, String packaging)
            throws Exception {
        String v = version;
        if (version.endsWith("SNAPSHOT")) {
            v = getSnapsotVersion(group, artifact, version);
        }

        String c = "";
        if (!"pom".equals(packaging) && !classifier.isEmpty()) {
            c = '-' + classifier;
        }

        return MAVEN_BASE_URL //
                + '/' + group.replace('.', '/') //
                + '/' + artifact //
                + '/' + version //
                + '/' + artifact + '-' + v + c + '.' + packaging;
    }

    static String getFileName(String group, String artifact, String version, String classifier, String packaging) {
        if (classifier.isEmpty()) {
            return group + '-' + artifact + '-' + version + '.' + packaging;
        } else {
            return group + '-' + artifact + '-' + version + '-' + classifier + '.' + packaging;
        }
    }

    static Document getDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }

    static String getStringValue(Document doc, String xPath) throws XPathExpressionException {
        XPath path = XPathFactory.newInstance().newXPath();
        XPathExpression expr = path.compile(xPath);
        return (String) expr.evaluate(doc, XPathConstants.STRING);
    }

    static void closeQuiet(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }
}
