/**
 * redpen: a text inspection tool
 * Copyright (c) 2014-2015 Recruit Technologies Co., Ltd. and contributors
 * (see CONTRIBUTORS.md)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.redpen.util;

import cc.redpen.RedPenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * load dictionary data from input source
 */
public final class DictionaryLoader<E> {
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryLoader.class);

    private final Supplier<E> supplier;
    private final BiConsumer<E, String> loader;

    private DictionaryLoader(Supplier<E> supplier, BiConsumer<E, String> loader) {
        this.supplier = supplier;
        this.loader = loader;
    }

    /**
     * Resource Extractor loads key-value dictionary
     */
    public final static DictionaryLoader<Map<String, String>> KEY_VALUE =
            new DictionaryLoader<>(HashMap::new, (map, line) -> {
                String[] result = line.split("\t");
                if (result.length == 2) {
                    map.put(result[0], result[1]);
                } else {
                    LOG.error("Skip to load line... Invalid line: " + line);
                }
            });

    /**
     * Resource Extractor loads word list
     */
    public final static DictionaryLoader<Set<String>> WORD =
            new DictionaryLoader<>(HashSet::new, Set::add);

    /**
     * Resource Extractor loads word list while lowercasing lines
     */
    public final static DictionaryLoader<Set<String>> WORD_LOWERCASE =
            new DictionaryLoader<>(HashSet::new, (set, line) -> set.add(line.toLowerCase()));

    /**
     * Given a input stream, load the contents.
     *
     * @param inputStream input stream
     * @throws IOException when failed to create reader from the specified input stream
     */
    E load(InputStream inputStream) throws IOException {
        E e = supplier.get();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                StandardCharsets.UTF_8))) {
            bufferedReader.lines().forEach(line -> loader.accept(e, line));
        }
        return e;
    }

    /**
     * Load a given input file combined with jar package.
     *
     * @param resourcePath resource path
     * @throws IOException when resource is not found
     */
    private E loadFromResource(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Failed to load input " + resourcePath);
            }
            return load(inputStream);
        }
    }

    /**
     * Load a given input file combined with jar package.
     *
     * @param filePath file path
     * @throws IOException when input stream is null
     */
    public E loadFromFile(String filePath) throws IOException {
        return load(new FileInputStream(filePath));
    }

    private final Map<String, E> resourceCache = new HashMap<>();

    /**
     * returns word list loaded from resource
     *
     * @param path           resource path
     * @param dictionaryName name of the resource
     * @return word list
     * @throws RedPenException
     */
    public E loadCachedFromResource(String path, String dictionaryName) throws RedPenException {
        E strings = resourceCache.computeIfAbsent(path, e -> {
            try {
                return loadFromResource(path);
            } catch (IOException ioe) {
                LOG.error(ioe.getMessage());
                return null;
            }
        });
        if (strings == null) {
            throw new RedPenException("Failed to load " + dictionaryName + ":" + path);
        }
        LOG.info("Succeeded to load " + dictionaryName + ".");
        return strings;
    }
}
