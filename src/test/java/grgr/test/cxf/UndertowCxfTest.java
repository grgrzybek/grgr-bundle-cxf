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

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class UndertowCxfTest {

    @Test
    public void testUndertowServlet() throws Exception {

        ServletContainer servletContainer = Servlets.newContainer();

        // parent, webcontext-wide Spring context
        ClassPathXmlApplicationContext parent = new ClassPathXmlApplicationContext("/UndertowCxfTest-context.xml");

        // represents a servlet deployment - representing everything we can find in web.xml
        DeploymentInfo di = Servlets.deployment()
                .setClassLoader(UndertowCxfTest.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("ROOT.war")
                .setDisplayName("Default Application")
                .setUrlEncoding("UTF-8")
                .addServlets(
                        Servlets.servlet("cxf", CXFServlet.class,
                                new ImmediateInstanceFactory<>(new CXFServlet())).addMapping("/*")
                );

        DeploymentManager dm = servletContainer.addDeployment(di);
        dm.deploy();
        HttpHandler handler = dm.start();

        // parent, webcontext-wide Spring web context
        GenericWebApplicationContext wac = new GenericWebApplicationContext();
        wac.setParent(parent);
        wac.setServletContext(dm.getDeployment().getServletContext());

        wac.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
        wac.refresh();

        PathHandler path = Handlers.path()
                .addPrefixPath(di.getContextPath(), Handlers.requestDump(handler));

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

}
