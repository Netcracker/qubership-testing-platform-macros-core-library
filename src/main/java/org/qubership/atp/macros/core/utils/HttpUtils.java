/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.macros.core.utils;

import static feign.form.ContentProcessor.CONTENT_TYPE_HEADER;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.google.common.base.Strings;

public class HttpUtils {
    private HttpUtils() {
    }

    /**
     * Create {@link HttpEntity}.
     *
     * @return created {@link HttpEntity}
     */
    public static HttpEntity<String> getHttpEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE_HEADER, MediaType.APPLICATION_JSON_VALUE);
        return Strings.isNullOrEmpty(body) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
    }

    public static HttpEntity<String> getHttpEntity() {
        return getHttpEntity(null);
    }
}
