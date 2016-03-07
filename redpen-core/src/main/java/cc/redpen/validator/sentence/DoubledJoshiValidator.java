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
package cc.redpen.validator.sentence;

import cc.redpen.model.Sentence;
import cc.redpen.tokenizer.TokenElement;
import cc.redpen.validator.Validator;

import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

/**
 * DoubledJoshiValidator checks if the input texts has duplicated Kakujoshi words in one setnences.
 * <br>
 * Note: this validator works only for Japanese texts.
 */
public class DoubledJoshiValidator extends Validator {
    public DoubledJoshiValidator() {
        super("list", emptySet());
    }

    @Override
    public void validate(Sentence sentence) {
        Map<String, Integer> counts = new HashMap<>();
        for (TokenElement tokenElement : sentence.getTokens()) {
            if (tokenElement.getTags().get(0).equals("助詞")) {
                if (!counts.containsKey(tokenElement.getSurface())) {
                    counts.put(tokenElement.getSurface(), 0);
                }
                counts.put(tokenElement.getSurface(),
                        counts.get(tokenElement.getSurface())+1);
            }
        }
        Set<String> skipList = getSetAttribute("list");
        counts.entrySet().stream()
                .filter(e -> e.getValue() >= 2 && !skipList.contains(e.getKey()))
                .forEach(e -> addLocalizedError(sentence, e.getKey()));
    }

    @Override
    public List<String> getSupportedLanguages() {
        return singletonList(Locale.JAPANESE.getLanguage());
    }
}
