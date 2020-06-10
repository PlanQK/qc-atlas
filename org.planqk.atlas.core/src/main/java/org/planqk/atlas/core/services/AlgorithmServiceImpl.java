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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.planqk.atlas.core.model.AlgoRelationType;
import org.planqk.atlas.core.model.Algorithm;
import org.planqk.atlas.core.model.AlgorithmRelation;
import org.planqk.atlas.core.model.QuantumAlgorithm;
import org.planqk.atlas.core.repository.AlgorithmRelationRepository;
import org.planqk.atlas.core.repository.AlgorithmRepository;
import org.planqk.atlas.core.repository.QuantumResourceRepository;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@AllArgsConstructor
public class AlgorithmServiceImpl implements AlgorithmService {

    private final static Logger LOG = LoggerFactory.getLogger(AlgorithmServiceImpl.class);

    private final AlgorithmRepository algorithmRepository;
    private final AlgorithmRelationRepository algorithmRelationRepository;
    private final AlgoRelationTypeService relationTypeService;
    private final QuantumResourceRepository resourceRepository;

    private final TagService tagService;
    private final ProblemTypeService problemTypeService;
    private final AlgoRelationTypeService algoRelationTypeService;
    private PublicationService publicationService;

    public void detachResourcesFromAlgorithm(UUID algoId) {
        var algorithm = algorithmRepository.findById(algoId).orElseThrow(NoSuchElementException::new);
        if (algorithm instanceof QuantumAlgorithm) {
            var items = this.resourceRepository.findAllByAlgorithm_Id(algoId)
                    .stream()
                    .peek(resource -> resource.setAlgorithm(null))
                    .collect(Collectors.toList());
            this.resourceRepository.saveAll(items);
        }
    }

    @Override
    public Algorithm save(Algorithm algorithm) {
        // Persist Tags separately
        algorithm.setTags(tagService.createOrUpdateAll(algorithm.getTags()));
        // Persist ProblemTypes separately
        algorithm.setProblemTypes(problemTypeService.createOrUpdateAll(algorithm.getProblemTypes()));
        // Persist Publications separately
        algorithm.setPublications(publicationService.createOrUpdateAll(algorithm.getPublications()));

        return algorithmRepository.save(algorithm);
    }

    @Override
    public Algorithm update(UUID id, Algorithm algorithm) {
    	LOG.info("Trying to update algorithm");
        Algorithm persistedAlg = algorithmRepository.findById(id).orElseThrow(NoSuchElementException::new);

        persistedAlg.setName(algorithm.getName());
        persistedAlg.setAcronym(algorithm.getAcronym());
        // persistedAlg.setPublications(algorithm.getPublications());
        persistedAlg.setIntent(algorithm.getIntent());
        persistedAlg.setProblem(algorithm.getProblem());
        // persistedAlg.setAlgorithmRelations(algorithm.getAlgorithmRelations());
        persistedAlg.setInputFormat(algorithm.getInputFormat());
        persistedAlg.setAlgoParameter(algorithm.getAlgoParameter());
        persistedAlg.setOutputFormat(algorithm.getOutputFormat());
        persistedAlg.setSketch(algorithm.getSketch());
        persistedAlg.setSolution(algorithm.getSolution());
        persistedAlg.setAssumptions(algorithm.getAssumptions());
        persistedAlg.setComputationModel(algorithm.getComputationModel());
        // persistedAlg.setRelatedPatterns(algorithm.getRelatedPatterns());
        persistedAlg.setProblemTypes(algorithm.getProblemTypes());
        persistedAlg.setApplicationAreas(algorithm.getApplicationAreas());
        persistedAlg.setTags(algorithm.getTags());

        // If QuantumAlgorithm adjust Quantum fields
        if (algorithm instanceof QuantumAlgorithm) {
        	QuantumAlgorithm quantumAlgorithm = (QuantumAlgorithm) algorithm;
        	QuantumAlgorithm persistedQuantumAlg = (QuantumAlgorithm) persistedAlg;

        	persistedQuantumAlg.setNisqReady(quantumAlgorithm.isNisqReady());
        	persistedQuantumAlg.setQuantumComputationModel(quantumAlgorithm.getQuantumComputationModel());
        	// persistedQuantumAlg.setRequiredQuantumResources(quantumAlgorithm.getRequiredQuantumResources());
        	persistedQuantumAlg.setSpeedUp(quantumAlgorithm.getSpeedUp());

        	return algorithmRepository.save(persistedQuantumAlg);
        } else {
        	// Else if ClassicAlgorithm no more fields to adjust
        	return algorithmRepository.save(persistedAlg);
        }
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        Set<AlgorithmRelation> linkedAsTargetRelations = algorithmRelationRepository.findByTargetAlgorithmId(id);
        for (AlgorithmRelation relation : linkedAsTargetRelations) {
            deleteAlgorithmRelation(relation.getSourceAlgorithm().getId(), relation.getId());
        }
        detachResourcesFromAlgorithm(id);

        //Remove all publications and associations that refer only to this single algorithm
        Set<String> publicationIds = algorithmRepository.getPublicationIdsOfAlgorithm(id);
        algorithmRepository.deleteAssociationsOfAlgorithm(id);
        publicationIds.stream().map(publicationId -> UUID.fromString(publicationId)).forEach(publicationService::deleteById);

        algorithmRepository.deleteById(id);
    }

    @Override
    public Page<Algorithm> findAll(Pageable pageable) {
        return algorithmRepository.findAll(pageable);
    }

    @Override
    public Algorithm findById(UUID algoId) {
        return findOptionalById(algoId).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public Optional<Algorithm> findOptionalById(UUID algoId) {
        return algorithmRepository.findById(algoId);
    }

    @Override
    public AlgorithmRelation addOrUpdateAlgorithmRelation(UUID sourceAlgorithmId, AlgorithmRelation relation) {
        // Read involved Algorithms from database
        Algorithm sourceAlgorithm = findById(sourceAlgorithmId);
        Algorithm targetAlgorithm = findById(relation.getTargetAlgorithm().getId());

        if (relation.getAlgoRelationType().getId() == null) {
            algoRelationTypeService.save(relation.getAlgoRelationType());
        }
        Optional<AlgoRelationType> relationTypeOpt = relationTypeService
                .findOptionalById(relation.getAlgoRelationType().getId());

        // Create relation type if not exists
        AlgoRelationType relationType = relationTypeOpt.isEmpty()
                ? algoRelationTypeService.save(relation.getAlgoRelationType())
                : relationTypeOpt.get();

        // Check if relation with those two algorithms and the relation type already
        // exists
        Optional<AlgorithmRelation> persistedRelationOpt = algorithmRelationRepository
                .findBySourceAlgorithmIdAndTargetAlgorithmIdAndAlgoRelationTypeId(sourceAlgorithm.getId(),
                        targetAlgorithm.getId(), relationType.getId());

        // If relation between the two algorithms already exists, update it
        if (persistedRelationOpt.isPresent()) {
            AlgorithmRelation persistedRelation = persistedRelationOpt.get();
            persistedRelation.setDescription(relation.getDescription());
            // Return updated relation
            return save(persistedRelation);
        }

        // Set Relation Objects with referenced database objects
        relation.setId(null);
        relation.setSourceAlgorithm(sourceAlgorithm);
        relation.setTargetAlgorithm(targetAlgorithm);
        relation.setAlgoRelationType(relationType);

        sourceAlgorithm.addAlgorithmRelation(relation);
        // Save updated Algorithm -> CASCADE will save Relation
        sourceAlgorithm = save(sourceAlgorithm);

        return relation;
    }

    private AlgorithmRelation save(AlgorithmRelation current) {
        return algorithmRelationRepository.save(current);
    }

    @Override
    public void deleteAlgorithmRelation(UUID algoId, UUID relationId) {
        // Get involved Objects from database
        Algorithm sourceAlgorithm = algorithmRepository.findById(algoId)
                .orElseThrow(() -> new NoSuchElementException("Algorithm does not exist!"));
        AlgorithmRelation relation = algorithmRelationRepository.findById(relationId)
                .orElseThrow(() -> new NoSuchElementException("Relation does not exist!"));

        Set<AlgorithmRelation> algorithmRelations = sourceAlgorithm.getAlgorithmRelations();
        algorithmRelations.remove(relation);
        algorithmRepository.save(sourceAlgorithm);
    }

    @Override
    public Set<AlgorithmRelation> getAlgorithmRelations(UUID sourceAlgorithmId) {
        return algorithmRelationRepository.findBySourceAlgorithmId(sourceAlgorithmId);
    }
}
