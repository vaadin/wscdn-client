package com.vaadin.wscdn.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.codec.digest.DigestUtils;

public class Connection {

    public static final String COMPILE_SERVICE_URL = "https://wsc.vaadin.com/";
    public static final String COMPILE_SERVICE_URL_LOCAL = "http://localhost:8080/vwscdn";
    public static final String PARAM_VWSCDN_LOCAL = "vwscdn.local";
    public static final String QUERY_PARAM_ASYNC_COMPILE = "compile.async";

    public static String getDefaultServiceUrl() {
        return System.getProperty(PARAM_VWSCDN_LOCAL) != null ? COMPILE_SERVICE_URL_LOCAL : COMPILE_SERVICE_URL;
    }

    private Client client;
    private WebTarget target;
    private String serviceName;
    private String serviceVersion;
    private String buildTime;
    private final WebTarget downloadTarget;
    private String JERSEY_FOLLOW_REDIRECTS ="jersey.config.client.followRedirects";
    
    public Connection() {
        this(getDefaultServiceUrl());
    }

    public Connection(String vwscdnUrl) {
        vwscdnUrl = vwscdnUrl == null ? getDefaultServiceUrl() : vwscdnUrl;
        this.client = ClientBuilder.newClient();
        vwscdnUrl = vwscdnUrl.endsWith("/") ? vwscdnUrl : vwscdnUrl + "/";
        this.target = client.target(vwscdnUrl + "api/compiler/compile");
        this.downloadTarget = client.target(vwscdnUrl + "api/compiler/download");

        try {
            Properties props = new Properties();
            props.load(Connection.class.getResourceAsStream(
                    "/service.properties"));
            this.serviceName = props.getProperty("service.name");
            this.serviceVersion = props.getProperty("service.version");
            this.buildTime = props.getProperty("build.time");
        } catch (Exception ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE,
                    "Failed to load /service.properties", ex);
        }

    }

    public WidgetSetResponse queryRemoteWidgetSet(WidgetSetRequest request,
            boolean asynchronous) {
        try {
            return target.queryParam(QUERY_PARAM_ASYNC_COMPILE, asynchronous)
                    .request(MediaType.APPLICATION_JSON).header("User-Agent",
                            getUA())
                    .post(Entity.json(request), WidgetSetResponse.class);

        } catch (Exception ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE,
                    "Failed to connect service " + target.getUri() + "", ex);
        }
        return null;
    }

    private String getUA() {
        return serviceName + "-" + serviceVersion
                + " (" + System.getProperty("os.name") + "/"
                + System.getProperty("os.arch") + "/"
                + System.getProperty("os.version") + "; "
                + System.getProperty("java.runtime.name") + "/"
                + System.getProperty("java.version") + "; "
                + DigestUtils.md5Hex(System.getProperty("user.name"))
                + ")";
    }

    public String downloadRemoteWidgetSet(WidgetSetRequest wsReq,
            File targetDirectory) throws FileNotFoundException, IOException {
        Response response = downloadTarget
                .request("application/x-zip").header("User-Agent",
                        getUA())
                .property(JERSEY_FOLLOW_REDIRECTS, Boolean.FALSE)
                .post(Entity.json(wsReq));

        // redirect?
        if (response.getStatus() == Status.TEMPORARY_REDIRECT.getStatusCode()) {
            // Follow redirect
            WebTarget newDownloadTarget = client.target(response.getHeaderString("Location"));
            // Resteasy requires this
            response.close();
            response = newDownloadTarget.request("application/x-zip")
                    .header("User-Agent", getUA()).get();
        }
        
        String wsName = response.getHeaderString("x-amz-meta-wsid");
        if (wsName == null) {
            wsName = response.getHeaderString("wsId");
        }
        targetDirectory = new File(targetDirectory, wsName);
        System.out.println("Downloading widgetset to " + targetDirectory);
        InputStream inputstream = response.readEntity(InputStream.class);

        ZipInputStream zipInputStream = new ZipInputStream(inputstream);
        ZipEntry ze = null;
        while ((ze = zipInputStream.getNextEntry()) != null) {
            final File outfile = new File(targetDirectory, ze.getName());
            if (!ze.isDirectory()) {
                outfile.getParentFile().mkdirs();
                outfile.createNewFile();
                FileOutputStream fout = new FileOutputStream(outfile);
                for (int c = zipInputStream.read(); c != -1; c = zipInputStream.
                        read()) {
                    fout.write(c);
                }
                fout.close();
            }
            zipInputStream.closeEntry();
        }
        zipInputStream.close();
        
        // Resteasy requires this
        response.close();
        
        return wsName;

    }

}
