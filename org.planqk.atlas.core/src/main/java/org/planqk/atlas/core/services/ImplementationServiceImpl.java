/*******************************************************************************
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

import java.util.NoSuchElementException;
import java.util.UUID;

import org.planqk.atlas.core.model.Implementation;
import org.planqk.atlas.core.repository.ImplementationRepository;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ImplementationServiceImpl implements ImplementationService {

    private final ImplementationRepository repository;
    private final TagService tagService;
    private final PublicationService publicationService;
    private final AlgorithmService algorithmService;

    @Transactional
    @Override
    public Implementation save(Implementation implementation) {
        tagService.createOrUpdateAll(implementation.getTags());

        publicationService.createOrUpdateAll(implementation.getPublications());

        if (implementation.getImplementedAlgorithm().getId() == null) {
            algorithmService.save(implementation.getImplementedAlgorithm());
        }

        return repository.save(implementation);
    }

    @Override
    public Page<Implementation> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Implementation findById(UUID implId) {
        return repository.findById(implId).orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    @Override
    public Implementation update(UUID id, Implementation implementation) {
        if (repository.existsImplementationById(id)) {
            implementation.setId(id);
            return save(implementation);
        }
        throw new NoSuchElementException();
    }
}
