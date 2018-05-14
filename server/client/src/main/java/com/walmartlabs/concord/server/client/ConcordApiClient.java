package com.walmartlabs.concord.server.client;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Wal-Mart Store, Inc.
 * -----
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
 * =====
 */

import com.walmartlabs.concord.server.ApiClient;
import com.walmartlabs.concord.server.auth.ApiKeyAuth;

public class ConcordApiClient extends ApiClient {

    public void setSessionToken(String token) {
        ApiKeyAuth apiKey = (ApiKeyAuth)getAuthentications().get("session_key");
        if (apiKey == null) {
            throw new RuntimeException("No Session token authentication configured!");
        }
        apiKey.setApiKey(token);
    }
}