/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package grgr.test.cxf.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;

public class SimpleProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        DefaultMessage defaultMessage = new DefaultMessage();

        defaultMessage.setBody(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));
        exchange.setIn(defaultMessage);
    }

}
