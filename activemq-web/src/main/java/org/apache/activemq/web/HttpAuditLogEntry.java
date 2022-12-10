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
package org.apache.activemq.web;

import org.apache.activemq.broker.util.AuditLogEntry;

import java.util.Arrays;
import java.util.Map;

public class HttpAuditLogEntry extends AuditLogEntry {
    @Override
    public String toString() {
        String formattedParams = "";
        Map<String, String[]> params = (Map<String, String[]>)parameters.get("params");
        if (params != null) {
            for (String paramName : params.keySet()) {
                formattedParams += paramName + "='" + Arrays.toString(params.get(paramName)) + "' ";
            }
        }

        return user + " requested " + operation + " [" + formattedParams + "] from  " + remoteAddr +" at " + getFormattedTime();
    }
}
