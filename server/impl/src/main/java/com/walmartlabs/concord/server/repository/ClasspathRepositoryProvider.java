package com.walmartlabs.concord.server.repository;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Walmart Inc.
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

import com.google.common.io.Resources;
import com.walmartlabs.concord.common.IOUtils;
import com.walmartlabs.concord.repository.RepositoryException;
import com.walmartlabs.concord.repository.RepositoryInfo;
import com.walmartlabs.concord.repository.RepositoryProvider;
import com.walmartlabs.concord.repository.Snapshot;
import com.walmartlabs.concord.sdk.Secret;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class ClasspathRepositoryProvider implements RepositoryProvider {

    private static final String URL_PREFIX = "classpath://";

    @Override
    public boolean canHandle(String url) {
        return url.startsWith(URL_PREFIX);
    }

    @Override
    public String getBranchOrDefault(String branch) {
        return branch;
    }

    @Override
    public void fetch(String repoUrl, String branch, String commitId, Secret secret, Path dst) {
        URL resUrl = Resources.getResource(normalizeUrl(repoUrl));

        try {
            if (!Files.exists(dst)) {
                Files.createDirectories(dst);
            }

            String fileName = repoUrl.substring(repoUrl.lastIndexOf('/') + 1);
            Path dstFile = dst.resolve(fileName);

            try (OutputStream out = Files.newOutputStream(dstFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                Resources.copy(resUrl, out);
            }
        } catch (IOException e) {
            throw new RepositoryException("Error while fetching a repository", e);
        }
    }

    @Override
    public Snapshot export(Path src, Path dst, List<String> ignorePatterns) throws IOException {
        IOUtils.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);

        return new Snapshot() {

            @Override
            public boolean isModified(Path path, BasicFileAttributes attrs) {
                return true;
            }

            @Override
            public boolean contains(Path path) {
                return true;
            }
        };
    }

    @Override
    public RepositoryInfo getInfo(Path path) {
        return null;
    }

    private static String normalizeUrl(String url) {
        return url.substring(URL_PREFIX.length());
    }
}
