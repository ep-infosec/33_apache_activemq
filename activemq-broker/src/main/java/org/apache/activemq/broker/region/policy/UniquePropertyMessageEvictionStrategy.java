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
package org.apache.activemq.broker.region.policy;

import org.apache.activemq.broker.region.MessageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An eviction strategy which evicts the oldest message within messages with the same property value
 *
 *
 * @org.apache.xbean.XBean
 *
 */
public class UniquePropertyMessageEvictionStrategy extends MessageEvictionStrategySupport {

    protected String propertyName;

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public MessageReference[] evictMessages(LinkedList messages) throws IOException {
        List<MessageReference> messageReferences = new LinkedList<>(messages);
        HashMap<Object, MessageReference> pivots = new HashMap<>();

        for (MessageReference reference : messageReferences) {
            if (propertyName != null && reference.getMessage().getProperty(propertyName) != null) {
                Object key = reference.getMessage().getProperty(propertyName);
                if (pivots.containsKey(key)) {
                    MessageReference pivot = pivots.get(key);
                    if (reference.getMessage().getTimestamp() > pivot.getMessage().getTimestamp()) {
                         pivots.put(key, reference);
                    }
                } else {
                    pivots.put(key, reference);
                }
            }
        }

        if (pivots.isEmpty() || pivots.values().size() == messageReferences.size()) {
            return new MessageReference[]{(MessageReference) messages.removeFirst()};
        } else {
            messages.removeIf(message -> !pivots.containsValue(message));
            messageReferences.removeAll(pivots.values());
            return messageReferences.toArray(new MessageReference[0]);
        }
    }

}
