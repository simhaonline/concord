package com.walmartlabs.concord.runtime.v2.runner;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2020 Walmart Inc.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmartlabs.concord.runtime.v2.model.ProcessConfiguration;
import com.walmartlabs.concord.sdk.Constants;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Waits for the process state files to appear in the  working directory and tries to
 * load the process' configuration from it.
 */
@Singleton
public class DefaultProcessConfigurationProvider implements Provider<ProcessConfiguration> {

    private final Path workDir;

    public DefaultProcessConfigurationProvider(Path workDir) {
        this.workDir = workDir;
    }

    @Override
    public ProcessConfiguration get() {
        try {
            UUID instanceId = waitForInstanceId(workDir);
            // TODO pass instanceId directly in _main.json?
            return readProcessConfiguration(instanceId, workDir);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Waits until an instanceId file appears in the specified directory
     * then reads it and parses as UUID.
     */
    private static UUID waitForInstanceId(Path workDir) throws IOException {
        Path p = workDir.resolve(Constants.Files.INSTANCE_ID_FILE_NAME);
        while (true) {
            if (Files.exists(p)) {
                String s = new String(Files.readAllBytes(p));
                return UUID.fromString(s.trim());
            }

            // TODO maybe use something more sophisticated, like a file watcher
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static ProcessConfiguration readProcessConfiguration(UUID instanceId, Path workDir) throws IOException {
        Path p = workDir.resolve(Constants.Files.REQUEST_DATA_FILE_NAME);
        if (!Files.exists(p)) {
            return ProcessConfiguration.builder()
                    .instanceId(instanceId)
                    .build();
        }

        // TODO singleton?
        ObjectMapper om = new ObjectMapper();
        try (InputStream in = Files.newInputStream(p)) {
            return ProcessConfiguration.builder()
                    .from(om.readValue(in, ProcessConfiguration.class))
                    .instanceId(instanceId)
                    .build();
        }
    }
}
