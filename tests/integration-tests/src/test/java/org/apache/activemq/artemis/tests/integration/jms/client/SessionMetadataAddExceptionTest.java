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
package org.apache.activemq.artemis.tests.integration.jms.client;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.server.ServerSession;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerPlugin;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.tests.util.JMSTestBase;
import org.junit.Test;

/**
 * Test that the client receives an exception if there is an error when metadata
 * is added
 */
public class SessionMetadataAddExceptionTest extends JMSTestBase {

   @Override
   protected Configuration createDefaultConfig(boolean netty) throws Exception {
      Configuration config = super.createDefaultConfig(netty);
      config.registerBrokerPlugin(new ActiveMQServerPlugin() {

         @Override
         public void beforeSessionMetadataAdded(ServerSession session, String key, String data)
               throws ActiveMQException {

            if (ClientSession.JMS_SESSION_CLIENT_ID_PROPERTY.equals(key)) {
               if ("invalid".equals(data)) {
                  throw new ActiveMQException("Invalid clientId");
               }
            }
         }

      });

      return config;
   }

   @Test(timeout = 5000, expected = JMSException.class)
   public void testInvalidClientIdSetConnection() throws Exception {
      Connection con = cf.createConnection();
      con.setClientID("invalid");
   }

   @Test(timeout = 5000, expected = JMSException.class)
   public void testInvalidClientIdSetFactory() throws Exception {
      ActiveMQConnectionFactory activeMQConnectionFactory = (ActiveMQConnectionFactory) cf;
      activeMQConnectionFactory.setClientID("invalid");
      cf.createConnection();
   }

   @Test(timeout = 5000)
   public void testValidIdSetConnection() throws Exception {
      Connection con = cf.createConnection();
      try {
         con.setClientID("valid");
         con.start();
      } finally {
         con.close();
      }
   }

   @Test(timeout = 5000)
   public void testValidClientIdSetFactory() throws Exception {
      ActiveMQConnectionFactory activeMQConnectionFactory = (ActiveMQConnectionFactory) cf;
      activeMQConnectionFactory.setClientID("valid");
      Connection con = cf.createConnection();
      try {
         con.start();
      } finally {
         con.close();
      }
   }
}
