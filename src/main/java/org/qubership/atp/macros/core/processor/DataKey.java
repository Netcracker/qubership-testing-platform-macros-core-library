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

package org.qubership.atp.macros.core.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

public class DataKey<T> {
    private static final Map<String, DataKey> ourDataKeyIndex = new HashMap<String, DataKey>();

    private final String myName;

    protected DataKey(@Nonnull String name) {
        myName = name;
    }

    /**
     * Create a new data key.
     * @param name name
     * @param <T> type of data key
     * @return data key
     */
    public static <T> DataKey<T> create(@Nonnull String name) {
        //noinspection unchecked
        DataKey<T> key = ourDataKeyIndex.get(name);
        if (key != null) {
            return key;
        }
        key = new DataKey<>(name);
        ourDataKeyIndex.put(name, key);
        return key;
    }

    @Nonnull
    public String getName() {
        return myName;
    }

    /**
     * For short, use MY_KEY.is(dataId) instead of MY_KEY.getName().equals(dataId)
     *
     * @param dataId key name
     * @return {@code true} if name of DataKey equals to {@code dataId},
     * {@code false} otherwise
     */
    public final boolean is(String dataId) {
        return myName.equals(dataId);
    }

    /**
     * Get data.
     * @param dataContext data context
     * @return data
     */
    @Nullable
    public T get(@Nonnull AbstractContext<?> dataContext) {
        //noinspection unchecked
        return dataContext.getData(this);
    }

    /**
     * Get non null data.
     * @param dataContext data context
     * @return data
     */
    @Nonnull
    public T getNonnull(@Nonnull AbstractContext dataContext) {
        T result = get(dataContext);
        Preconditions.checkNotNull(result, "Value for [%s] should not be null in [%s]", getName(),
                dataContext);
        return result;
    }

    public T set(@Nonnull AbstractContext<?> dataContext, @Nonnull T data) {
        return dataContext.setData(this, data);
    }

    public T remove(@Nonnull AbstractContext<?> dataContext) {
        return dataContext.removeData(this);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataKey<?> dataKey = (DataKey<?>) o;
        return Objects.equals(myName, dataKey.myName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myName);
    }
}
