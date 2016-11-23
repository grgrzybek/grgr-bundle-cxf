/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grgr.test.cxf;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.util.Headers;
import org.junit.jupiter.api.Test;

public class UndertowTest {

    @Test
    public void testUndertowHandler() throws Exception {
        createAndStartServlet(8000, exchange -> {
            exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain; charset=utf-8");
            exchange.getResponseSender().send("ąćęłńóśżź", Charset.forName("utf-8"));
        });
    }

    @Test
    public void testUndertowServlet() throws Exception {

        // a container of DeploymentManager instances (mapped by name and path)
        ServletContainer servletContainer1 = Servlets.newContainer();
        ServletContainer servletContainer2 = Servlets.newContainer();

        // represents a servlet deployment - representing everything we can find in web.xml
        DeploymentInfo di1 = Servlets.deployment()
                .setClassLoader(UndertowTest.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("ROOT.war")
                .setDisplayName("Default Application")
                .setUrlEncoding("UTF-8")
                .addServlets(
                        Servlets.servlet("s1", MyServlet.class).addMapping("/s1/*"),
                        Servlets.servlet("s2", MyServlet.class).addMapping("/s2/*")
                );
        DeploymentInfo di2 = Servlets.deployment()
                .setClassLoader(UndertowTest.class.getClassLoader())
                .setContextPath("/app")
                .setDeploymentName("test.war")
                .setDisplayName("Test Application")
                .setUrlEncoding("UTF-8")
                .addServlets(
                        Servlets.servlet("s1", MyServlet.class).addMapping("/s1/*"),
                        Servlets.servlet("s2", MyServlet.class).addMapping("/s2/*")
                );

        // deployment manager is responsible for managing lifecycle of servlet deployment within ServletContainer
        DeploymentManager m1 = servletContainer1.addDeployment(di1);
        DeploymentManager m2 = servletContainer2.addDeployment(di2);
        // configures everything that is set up on servlet deployment (servlets, filters, extensions, welcome pages, ...)
        m1.deploy();
        m2.deploy();
        // creates http handler that allows handling requests using servlets/webapps
        HttpHandler application1 = m1.start();
        HttpHandler application2 = m2.start();

//        PathHandler path = Handlers.path(Handlers.redirect("/app")).addPrefixPath("/app", ); // with default handler
        PathHandler path = Handlers.path()
//                .addPrefixPath("/", Handlers.redirect("/app")) // equivalent of using default handler
                .addPrefixPath(di1.getContextPath(), Handlers.requestDump(application1))
                .addPrefixPath(di2.getContextPath(), Handlers.requestDump(application2));

        createAndStartServlet(8000, path);
    }

    private void createAndStartServlet(int port, HttpHandler handler) throws IOException {
        Undertow server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(handler)
                .build();
        server.start();
        System.in.read();
    }

    public static class MyServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            getServletContext().log(String.format("%s: %s", getServletName(), "hello"));

            resp.setContentType("text/plain; charset=utf-8");
            resp.getWriter().println("Hello! " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS").format(LocalDateTime.now()));
            resp.getWriter().println("Path: " + req.getServletContext().getContextPath());
            resp.getWriter().println("Req Path: " + req.getRequestURI());
            resp.getWriter().close();
        }
    }

}
