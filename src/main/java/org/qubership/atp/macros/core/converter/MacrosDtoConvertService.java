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

package org.qubership.atp.macros.core.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MacrosDtoConvertService {

    private final ModelMapper modelMapper = new ModelMapper();

    public MacrosDtoConvertService() {
        configureModelMapper();
    }

    /**
     * Get modelMapper for additional configulations
     * @return modelMapper.
     */
    public ModelMapper getModelMapper() {
        return modelMapper;
    }

    /**
     * Convert t.
     *
     * @param <T>  the type parameter
     * @param from the from
     * @param to   the to
     * @return the t
     */
    public <T> T convert(Object from, Class<T> to) {
        return modelMapper.map(from, to);
    }

    /**
     * Convert list to list.
     *
     * @param <T>  the type parameter
     * @param from the from
     * @param to   the to
     * @return the set
     */
    public <T> List<T> convertList(List from, Class<T> to) {
        if (from == null) {
            return new ArrayList<>();
        }
        return (List<T>) from.stream().map(o -> convert(o, to)).collect(Collectors.toList());
    }

    /**
     * Convert set to list.
     *
     * @param <T>  the type parameter
     * @param from the from
     * @param to   the to
     * @return the set
     */
    public <T> List<T> convertSetToList(Set from, Class<T> to) {
        if (from == null) {
            return new ArrayList<>();
        }
        return (List<T>) from.stream().map(o -> convert(o, to)).collect(Collectors.toList());
    }

    private void configureModelMapper() {
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.getConfiguration().getConverters().add(new Date2OffsetDateTimeConverter());
        modelMapper.getConfiguration().getConverters().add(new OffsetDateTime2DateConverter());
    }
}
