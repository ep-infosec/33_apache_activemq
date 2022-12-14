/**
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

package org.apache.activemq.usage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.ConfigurationException;
import org.junit.Test;

import org.apache.activemq.broker.BrokerService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.layout.MessageLayout;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StoreUsageLimitsTest {

    final int WAIT_TIME_MILLS = 20 * 1000;
    private static final String limitsLogLevel = "warn";
    final String toMatch = new String(Long.toString(Long.MAX_VALUE / (1024 * 1024)));

    protected BrokerService createBroker() throws Exception {
        BrokerService broker = new BrokerService();
        broker.setPersistent(false);
        broker.getSystemUsage().getMemoryUsage().setLimit(Long.MAX_VALUE);
        broker.getSystemUsage().setCheckLimitsLogLevel(limitsLogLevel);
        broker.deleteAllMessages();
        return broker;
    }

    @Test
    public void testCheckLimitsLogLevel() throws Exception {

        final CountDownLatch foundMessage = new CountDownLatch(1);
        final var logger = org.apache.logging.log4j.core.Logger.class.cast(LogManager.getRootLogger());
        final var appender = new AbstractAppender("testAppender", new AbstractFilter() {}, new MessageLayout(), false, new Property[0]) {
            @Override
            public void append(LogEvent event) {
                String message = event.getMessage().getFormattedMessage();
                if (message.contains(toMatch) && event.getLevel().equals(Level.WARN)) {
                    foundMessage.countDown();
                }
            }
        };
        appender.start();

        logger.get().addAppender(appender, Level.DEBUG, new AbstractFilter() {});
        logger.addAppender(appender);

        BrokerService brokerService = createBroker();
        brokerService.start();
        brokerService.stop();

        assertTrue("Fount log message", foundMessage.await(WAIT_TIME_MILLS, TimeUnit.MILLISECONDS));

        logger.removeAppender(appender);
    }

    @Test
    public void testCheckLimitsFailStart() throws Exception {

        final CountDownLatch foundMessage = new CountDownLatch(1);
        final var logger = org.apache.logging.log4j.core.Logger.class.cast(LogManager.getRootLogger());
        final var appender = new AbstractAppender("testAppender", new AbstractFilter() {}, new MessageLayout(), false, new Property[0]) {
            @Override
            public void append(LogEvent event) {
                String message = event.getMessage().getFormattedMessage();
                if (message.contains(toMatch) && event.getLevel().equals(Level.ERROR)) {
                    foundMessage.countDown();
                }
            }
        };
        appender.start();

        logger.get().addAppender(appender, Level.DEBUG, new AbstractFilter() {});
        logger.addAppender(appender);

         BrokerService brokerService = createBroker();
        brokerService.setAdjustUsageLimits(false);
        try {
            brokerService.start();
            fail("expect ConfigurationException");
        } catch (ConfigurationException expected) {
            assertTrue("exception message match", expected.getLocalizedMessage().contains(toMatch));
        }
        brokerService.stop();

        assertTrue("Fount log message", foundMessage.await(WAIT_TIME_MILLS, TimeUnit.MILLISECONDS));

        logger.removeAppender(appender);
    }
}
