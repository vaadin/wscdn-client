/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vaadin.wscdn.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vaadin.wscdn.client.Connection;
import com.vaadin.wscdn.client.WidgetSetRequest;
import com.vaadin.wscdn.client.WidgetSetResponse;

/**
 *
 * @author Sami Ekblad
 */
public class TestClient {

    private final String compileStyle = "OBF";
    private final String vaadinVersion = "7.6.7";
    private final List<MavenArtifact> testArtifacts = Arrays.asList(
            new MavenArtifact("org.vaadin.addons", "ratingstars", "2.1")
            );

    public TestClient() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void compileAsync() {

        WidgetSetRequest wsReq = getWsRequest();

        Connection conn = new Connection();
        WidgetSetResponse wsRes = conn.queryRemoteWidgetSet(wsReq, true);

        assertNotNull("Widgetset compile request failed", wsRes);
        assertNotNull("Missing widgetset URL", wsRes.getWidgetSetUrl());
        assertTrue("Invalid widgetset URL", wsRes.getWidgetSetUrl().contains("ws.vaadin.com"));
    }


    @Test
    public void compileDownload() {
        WidgetSetRequest wsReq = getWsRequest();

        Connection conn = new Connection();
        File tempDir;
        try {
            tempDir = createTempDirectory("wsdl");
            String res = conn.downloadRemoteWidgetSet(wsReq, tempDir);
            File resDir = new File(tempDir,res);
            assertNotNull("Widgetset compile request failed", res);
            assertTrue("",resDir.isDirectory());
        } catch (IOException ex) {
            Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }


    private WidgetSetRequest getWsRequest() {
        // Create test request
        WidgetSetRequest wsReq = new WidgetSetRequest();
        for (MavenArtifact a : testArtifacts) {
            wsReq.addon(a.getGroupId(), a.getArtifactId(), a.
                    getVersion());
        }
        wsReq.setCompileStyle(compileStyle);
        wsReq.setVaadinVersion(vaadinVersion);
        return wsReq;
    }


    public static class MavenArtifact {

        private final String artifactId;
        private final String groupId;
        private final String version;

        private MavenArtifact(String groupId, String artifactId, String version) {
            this.artifactId = artifactId;
            this.groupId = groupId;
            this.version = version;
        }

        /**
         * Get the value of version
         *
         * @return the value of version
         */
        public String getVersion() {
            return version;
        }

        /**
         * Get the value of groupId
         *
         * @return the value of groupId
         */
        public String getGroupId() {
            return groupId;
        }

        /**
         * Get the value of artifactId
         *
         * @return the value of artifactId
         */
        public String getArtifactId() {
            return artifactId;
        }

    }
    
    private static File createTempDirectory(String prefix) throws IOException {
        final File temp;

        temp = File.createTempFile(prefix, Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException(
                    "Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: "
                    + temp.getAbsolutePath());
        }

        return (temp);
    }
}
