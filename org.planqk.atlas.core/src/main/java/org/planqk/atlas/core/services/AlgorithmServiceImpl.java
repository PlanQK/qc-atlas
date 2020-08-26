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

import java.util.Objects;
import java.util.UUID;

import org.planqk.atlas.core.model.Algorithm;
import org.planqk.atlas.core.model.AlgorithmRelation;
import org.planqk.atlas.core.model.ApplicationArea;
import org.planqk.atlas.core.model.PatternRelation;
import org.planqk.atlas.core.model.ProblemType;
import org.planqk.atlas.core.model.Publication;
import org.planqk.atlas.core.model.QuantumAlgorithm;
import org.planqk.atlas.core.model.Sketch;
import org.planqk.atlas.core.repository.AlgorithmRelationRepository;
import org.planqk.atlas.core.repository.AlgorithmRepository;
import org.planqk.atlas.core.repository.ApplicationAreaRepository;
import org.planqk.atlas.core.repository.PatternRelationRepository;
import org.planqk.atlas.core.repository.ProblemTypeRepository;
import org.planqk.atlas.core.repository.PublicationRepository;
import org.planqk.atlas.core.repository.SketchRepository;
import org.planqk.atlas.core.util.ServiceUtils;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class AlgorithmServiceImpl implements AlgorithmService {

    private final AlgorithmRepository algorithmRepository;

    private final SketchRepository sketchRepository;

    private final AlgorithmRelationRepository algorithmRelationRepository;

    private final ImplementationService implementationService;

    private final PublicationRepository publicationRepository;

    private final ProblemTypeRepository problemTypeRepository;

    private final ApplicationAreaRepository applicationAreaRepository;

    private final PatternRelationService patternRelationService;
    private final PatternRelationRepository patternRelationRepository;

    @Override
    @Transactional
    public Algorithm create(Algorithm algorithm) {
        return algorithmRepository.save(algorithm);
    }

    @Override
    public Page<Algorithm> findAll(@NonNull Pageable pageable, String search) {
        if (!Objects.isNull(search) && !search.isEmpty()) {
            return algorithmRepository.findAll(search, pageable);
        }
        return algorithmRepository.findAll(pageable);
    }

    @Override
    public Algorithm findById(@NonNull UUID algorithmId) {
        return ServiceUtils.findById(algorithmId, Algorithm.class, algorithmRepository);
    }

    @Override
    @Transactional
    public Algorithm update(@NonNull Algorithm algorithm) {
        Algorithm persistedAlgorithm = findById(algorithm.getId());

        // remove all attached sketches
        persistedAlgorithm.removeSketches(persistedAlgorithm.getSketches());
        if (algorithm.getSketches() != null) {
            algorithm.getSketches().forEach(sketch -> {
                if (sketch.getId() == null) {
                    // add sketches one by one
                    final Sketch savedSketch = sketchRepository.save(sketch);
                    persistedAlgorithm.addSketch(savedSketch);
                }
            });
        }

        persistedAlgorithm.setName(algorithm.getName());
        persistedAlgorithm.setAcronym(algorithm.getAcronym());
        persistedAlgorithm.setIntent(algorithm.getIntent());
        persistedAlgorithm.setProblem(algorithm.getProblem());
        persistedAlgorithm.setInputFormat(algorithm.getInputFormat());
        persistedAlgorithm.setAlgoParameter(algorithm.getAlgoParameter());
        persistedAlgorithm.setOutputFormat(algorithm.getOutputFormat());
        persistedAlgorithm.setSolution(algorithm.getSolution());
        persistedAlgorithm.setAssumptions(algorithm.getAssumptions());
        persistedAlgorithm.setComputationModel(algorithm.getComputationModel());

        if (algorithm instanceof QuantumAlgorithm) {
            QuantumAlgorithm quantumAlgorithm = (QuantumAlgorithm) algorithm;
            QuantumAlgorithm persistedQuantumAlgorithm = (QuantumAlgorithm) persistedAlgorithm;

            persistedQuantumAlgorithm.setNisqReady(quantumAlgorithm.isNisqReady());
            persistedQuantumAlgorithm.setQuantumComputationModel(quantumAlgorithm.getQuantumComputationModel());
            persistedQuantumAlgorithm.setSpeedUp(quantumAlgorithm.getSpeedUp());

            return algorithmRepository.save(persistedQuantumAlgorithm);
        } else {
            return algorithmRepository.save(persistedAlgorithm);
        }
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID algorithmId) {
        Algorithm algorithm = findById(algorithmId);

        removeReferences(algorithm);

        algorithmRepository.deleteById(algorithmId);
    }

    private void removeReferences(@NonNull Algorithm algorithm) {
        // delete related implementations
        algorithm.getImplementations().forEach(
                implementation -> implementationService.delete(implementation.getId()));

        // delete algorithm relations
        algorithm.getAlgorithmRelations().forEach(algorithmRelationRepository::delete);

        // delete related pattern relations
        algorithm.getRelatedPatterns().forEach(
                patternRelation -> patternRelationService.delete(patternRelation.getId()));

        // delete all references to publications
        algorithm.getPublications().forEach(
                publication -> publication.removeAlgorithm(algorithm));
    }

    @Override
    public Page<Publication> findPublications(@NonNull UUID algorithmId, @NonNull Pageable pageable) {
        ServiceUtils.throwIfNotExists(algorithmId, Algorithm.class, algorithmRepository);

        return publicationRepository.findPublicationsByAlgorithmId(algorithmId, pageable);
    }

    @Override
    public Page<ProblemType> findProblemTypes(@NonNull UUID algorithmId, @NonNull Pageable pageable) {
        ServiceUtils.throwIfNotExists(algorithmId, Algorithm.class, algorithmRepository);

        return problemTypeRepository.findProblemTypesByAlgorithmId(algorithmId, pageable);
    }

    @Override
    public Page<ApplicationArea> findApplicationAreas(@NonNull UUID algorithmId, @NonNull Pageable pageable) {
        ServiceUtils.throwIfNotExists(algorithmId, Algorithm.class, algorithmRepository);

        return applicationAreaRepository.findApplicationAreasByAlgorithmId(algorithmId, pageable);
    }

    @Override
    public Page<PatternRelation> findPatternRelations(@NonNull UUID algorithmId, @NonNull Pageable pageable) {
        ServiceUtils.throwIfNotExists(algorithmId, Algorithm.class, algorithmRepository);

        return patternRelationRepository.findByAlgorithmId(algorithmId, pageable);
    }

    @Override
    public Page<AlgorithmRelation> findAlgorithmRelations(@NonNull UUID algorithmId, @NonNull Pageable pageable) {
        ServiceUtils.throwIfNotExists(algorithmId, Algorithm.class, algorithmRepository);

        return getAlgorithmRelations(algorithmId, pageable);
    }

    private Page<AlgorithmRelation> getAlgorithmRelations(@NonNull UUID algorithmId, @NonNull Pageable pageable) {
        return algorithmRelationRepository.findBySourceAlgorithmIdOrTargetAlgorithmId(algorithmId, algorithmId, pageable);
    }
}
