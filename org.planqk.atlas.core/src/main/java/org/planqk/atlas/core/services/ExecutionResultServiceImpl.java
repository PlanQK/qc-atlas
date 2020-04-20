/********************************************************************************
 * Copyright (c) 2020 University of Stuttgart
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.planqk.atlas.core.services;

import java.util.List;
import java.util.Optional;

import org.planqk.atlas.core.model.ExecutionResult;
import org.planqk.atlas.core.model.Implementation;
import org.planqk.atlas.core.repository.ExecutionResultRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ExecutionResultServiceImpl implements ExecutionResultService {

    private ExecutionResultRepository executionResultRepository;

    public ExecutionResultServiceImpl(ExecutionResultRepository executionResultRepository) {
        this.executionResultRepository = executionResultRepository;
    }

    @Override
    public ExecutionResult save(ExecutionResult executionResult) {
        return executionResultRepository.save(executionResult);
    }

    @Override
    public Optional<ExecutionResult> findById(Long id) {
        return executionResultRepository.findById(id);
    }

    @Override
    public List<ExecutionResult> findByImplementation(Implementation implementation) {
        return executionResultRepository.findByExecutedImplementation(implementation);
    }

    @Override
    public List<ExecutionResult> findAll() {
        return executionResultRepository.findAll();
    }
}
