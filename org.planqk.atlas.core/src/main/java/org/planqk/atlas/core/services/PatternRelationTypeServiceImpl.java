/*******************************************************************************
 * Copyright (c) 2020 the qc-atlas contributors.
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

import java.util.UUID;
import javax.transaction.Transactional;

import org.planqk.atlas.core.exceptions.EntityReferenceConstraintViolationException;
import org.planqk.atlas.core.model.PatternRelationType;
import org.planqk.atlas.core.repository.PatternRelationRepository;
import org.planqk.atlas.core.repository.PatternRelationTypeRepository;
import org.planqk.atlas.core.util.ServiceUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class PatternRelationTypeServiceImpl implements PatternRelationTypeService {

    private final PatternRelationTypeRepository patternRelationTypeRepository;

    private final PatternRelationRepository patternRelationRepository;

    @Override
    @Transactional
    public PatternRelationType create(@NonNull PatternRelationType patternRelationType) {
        return patternRelationTypeRepository.save(patternRelationType);
    }

    @Override
    public PatternRelationType findById(@NonNull UUID patternRelationTypeId) {
        return ServiceUtils.findById(patternRelationTypeId, PatternRelationType.class, patternRelationTypeRepository);
    }

    @Override
    public Page<PatternRelationType> findAll(@NonNull Pageable pageable) {
        return patternRelationTypeRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public PatternRelationType update(@NonNull PatternRelationType patternRelationType) {
        final PatternRelationType persistedPatternRelationType = findById(patternRelationType.getId());

        persistedPatternRelationType.setName(patternRelationType.getName());

        return patternRelationTypeRepository.save(persistedPatternRelationType);
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID patternRelationTypeId) {
        ServiceUtils.throwIfNotExists(patternRelationTypeId, PatternRelationType.class, patternRelationTypeRepository);

        if (patternRelationRepository.countByPatternRelationTypeId(patternRelationTypeId) > 0) {
            throw new EntityReferenceConstraintViolationException("PatternRelationType with ID \""
                    + patternRelationTypeId + "\" cannot be deleted, because it is still in use");
        }

        patternRelationTypeRepository.deleteById(patternRelationTypeId);
    }
}
