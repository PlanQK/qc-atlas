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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.planqk.atlas.core.model.Algorithm;
import org.planqk.atlas.core.model.AlgorithmRelation;
import org.planqk.atlas.core.model.AlgorithmRelationType;
import org.planqk.atlas.core.model.ApplicationArea;
import org.planqk.atlas.core.model.ClassicAlgorithm;
import org.planqk.atlas.core.model.ComputationModel;
import org.planqk.atlas.core.model.Implementation;
import org.planqk.atlas.core.model.PatternRelation;
import org.planqk.atlas.core.model.PatternRelationType;
import org.planqk.atlas.core.model.ProblemType;
import org.planqk.atlas.core.model.Publication;
import org.planqk.atlas.core.model.QuantumAlgorithm;
import org.planqk.atlas.core.model.QuantumComputationModel;
import org.planqk.atlas.core.model.Tag;
import org.planqk.atlas.core.util.AtlasDatabaseTestBase;
import org.planqk.atlas.core.util.Constants;
import org.planqk.atlas.core.util.ServiceTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlgorithmServiceTest extends AtlasDatabaseTestBase {

    @Autowired
    private AlgorithmService algorithmService;

    @Autowired
    private LinkingService linkingService;

    @Autowired
    private ImplementationService implementationService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ApplicationAreaService applicationAreaService;

    @Autowired
    private ProblemTypeService problemTypeService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private AlgorithmRelationService algorithmRelationService;

    @Autowired
    private AlgorithmRelationTypeService algorithmRelationTypeService;

    @Autowired
    private PatternRelationService patternRelationService;

    @Autowired
    private PatternRelationTypeService patternRelationTypeService;

    @Test
    void createAlgorithm_Classic() {
        ClassicAlgorithm algorithm = (ClassicAlgorithm) getFullAlgorithm("classicAlgorithmName");

        ClassicAlgorithm storedAlgorithm = (ClassicAlgorithm) algorithmService.create(algorithm);

        assertThat(storedAlgorithm.getId()).isNotNull();
        assertThat(storedAlgorithm).isInstanceOf(ClassicAlgorithm.class);
        ServiceTestUtils.assertAlgorithmEquality(storedAlgorithm, algorithm);
    }

    @Test
    void createAlgorithm_Quantum() {
        QuantumAlgorithm algorithm = new QuantumAlgorithm();
        algorithm.setName("quantumAlgorithmName");
        algorithm.setQuantumComputationModel(QuantumComputationModel.QUANTUM_ANNEALING);
        algorithm.setSpeedUp("speedUp");
        algorithm.setNisqReady(true);

        QuantumAlgorithm storedAlgorithm = (QuantumAlgorithm) algorithmService.create(algorithm);

        assertThat(storedAlgorithm.getId()).isNotNull();
        assertThat(storedAlgorithm).isInstanceOf(QuantumAlgorithm.class);
        ServiceTestUtils.assertAlgorithmEquality(storedAlgorithm, algorithm);
    }

    @Test
    void createAlgorithm_Classic_DropOldestRevisionElement() {
        Algorithm algorithm = getFullAlgorithm("Shor");
        Algorithm storedAlgorithm = algorithmService.create(algorithm);

        for(int i=1; i<Constants.REVISIONS_COUNT; i++) {
            storedAlgorithm.setName("Shor " + i);
            algorithmService.update(storedAlgorithm);
        }

        var oldestRevisionToBeDroped = algorithmService.findAlgorithmRevision(storedAlgorithm.getId(), 1);
        var newOldestRevision = algorithmService.findAlgorithmRevision(storedAlgorithm.getId(), 2);

        storedAlgorithm.setName("Shor");
        algorithmService.update(storedAlgorithm);

        var revisions = algorithmService.findAlgorithmRevisions(storedAlgorithm.getId(), PageRequest.of(0,10));
        assertThat(revisions.getTotalElements()).isEqualTo(Constants.REVISIONS_COUNT);
        assertThat(revisions.getContent()).doesNotContain(oldestRevisionToBeDroped);
        assertThat(revisions.getContent().get(0).getRevisionInstant().get()).isAfter(oldestRevisionToBeDroped.getRevisionInstant().get());
        assertThat(revisions.getContent().get(0)).isEqualTo(newOldestRevision);
    }

    @Test
    void createAlgorithm_Quantum_DropOldestRevisionElement() {
        QuantumAlgorithm algorithm = new QuantumAlgorithm();
        algorithm.setName("Shor");
        algorithm.setComputationModel(ComputationModel.QUANTUM);
        algorithm.setNisqReady(false);
        algorithm.setSpeedUp("2");
        algorithm.setQuantumComputationModel(QuantumComputationModel.QUANTUM_ANNEALING);

        Algorithm storedAlgorithm = algorithmService.create(algorithm);

        for(int i=1; i<Constants.REVISIONS_COUNT; i++) {
            storedAlgorithm.setName("Shor " + i);
            algorithmService.update(storedAlgorithm);
        }

        var oldestRevisionToBeDroped = algorithmService.findAlgorithmRevision(storedAlgorithm.getId(), 1);
        var newOldestRevision = algorithmService.findAlgorithmRevision(storedAlgorithm.getId(), 2);

        storedAlgorithm.setName("Shor");
        algorithmService.update(storedAlgorithm);

        var revisions = algorithmService.findAlgorithmRevisions(storedAlgorithm.getId(), PageRequest.of(0,Constants.REVISIONS_COUNT + 1));
        assertThat(revisions.getTotalElements()).isEqualTo(Constants.REVISIONS_COUNT);
        assertThat(revisions.getContent()).doesNotContain(oldestRevisionToBeDroped);
        assertThat(revisions.getContent().get(0).getRevisionInstant().get()).isAfter(oldestRevisionToBeDroped.getRevisionInstant().get());
        assertThat(revisions.getContent().get(0)).isEqualTo(newOldestRevision);
    }

    @Test
    void findAllAlgorithms() {
        Algorithm algorithm1 = getFullAlgorithm("algorithmName1");
        algorithmService.create(algorithm1);
        Algorithm algorithm2 = getFullAlgorithm("algorithmName2");
        algorithmService.create(algorithm2);

        List<Algorithm> algorithms = algorithmService.findAll(Pageable.unpaged(), null).getContent();

        assertThat(algorithms.size()).isEqualTo(2);
    }

    @Test
    void findAlgorithmById_ElementNotFound() {
        assertThrows(NoSuchElementException.class, () ->
                algorithmService.findById(UUID.randomUUID()));
    }

    @Test
    void findAlgorithmById_ElementFound() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");

        Algorithm storedAlgorithm = algorithmService.create(algorithm);

        storedAlgorithm = algorithmService.findById(storedAlgorithm.getId());

        assertThat(storedAlgorithm.getId()).isNotNull();
        ServiceTestUtils.assertAlgorithmEquality(storedAlgorithm, algorithm);
        assertThat(storedAlgorithm).isInstanceOf(ClassicAlgorithm.class);
    }

    @Test
    void updateAlgorithm_ElementFound() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        Algorithm compareAlgorithm = getFullAlgorithm("algorithmName");

        Algorithm storedAlgorithm = algorithmService.create(algorithm);
        compareAlgorithm.setId(storedAlgorithm.getId());
        String editName = "editedAlgorithmName";
        storedAlgorithm.setName(editName);
        Algorithm editedAlgorithm = algorithmService.update(storedAlgorithm);

        assertThat(editedAlgorithm.getId()).isNotNull();
        assertThat(editedAlgorithm.getId()).isEqualTo(compareAlgorithm.getId());
        assertThat(editedAlgorithm.getName()).isNotEqualTo(compareAlgorithm.getName());
        assertThat(editedAlgorithm.getName()).isEqualTo(editName);
        assertThat(editedAlgorithm.getAcronym()).isEqualTo(algorithm.getAcronym());
        assertThat(editedAlgorithm.getIntent()).isEqualTo(algorithm.getIntent());
        assertThat(editedAlgorithm.getProblem()).isEqualTo(algorithm.getProblem());
        assertThat(editedAlgorithm.getInputFormat()).isEqualTo(algorithm.getInputFormat());
        assertThat(editedAlgorithm.getAlgoParameter()).isEqualTo(algorithm.getAlgoParameter());
        assertThat(editedAlgorithm.getOutputFormat()).isEqualTo(algorithm.getOutputFormat());
        assertThat(editedAlgorithm.getSolution()).isEqualTo(algorithm.getSolution());
        assertThat(editedAlgorithm.getAssumptions()).isEqualTo(algorithm.getAssumptions());
        assertThat(editedAlgorithm.getComputationModel()).isEqualTo(algorithm.getComputationModel());

        editedAlgorithm.getSketches().containsAll(algorithm.getSketches());
        algorithm.getSketches().containsAll(editedAlgorithm.getSketches());
    }

    @Test
    void updateAlgorithm_ElementNotFound() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        algorithm.setId(UUID.randomUUID());
        assertThrows(NoSuchElementException.class, () ->
                algorithmService.update(algorithm));
    }

    @Test
    void updateAlgorithm_Quantum() {
        QuantumAlgorithm algorithm = new QuantumAlgorithm();
        algorithm.setName("quantumAlgorithmName");
        algorithm.setComputationModel(ComputationModel.QUANTUM);
        algorithm.setNisqReady(false);
        algorithm.setSpeedUp("2");
        algorithm.setQuantumComputationModel(QuantumComputationModel.QUANTUM_ANNEALING);
        QuantumAlgorithm compareAlgorithm = new QuantumAlgorithm();
        compareAlgorithm.setName("quantumAlgorithmName");
        compareAlgorithm.setComputationModel(ComputationModel.QUANTUM);
        compareAlgorithm.setNisqReady(false);
        compareAlgorithm.setSpeedUp("2");
        compareAlgorithm.setQuantumComputationModel(QuantumComputationModel.QUANTUM_ANNEALING);

        QuantumAlgorithm storedAlgorithm = (QuantumAlgorithm) algorithmService.create(algorithm);
        compareAlgorithm.setId(storedAlgorithm.getId());
        String editName = "editedQuantumAlgorithmName";
        storedAlgorithm.setName(editName);
        QuantumAlgorithm editedAlgorithm = (QuantumAlgorithm) algorithmService.update(storedAlgorithm);

        assertThat(editedAlgorithm.getId()).isNotNull();
        assertThat(editedAlgorithm.getId()).isEqualTo(compareAlgorithm.getId());
        assertThat(editedAlgorithm.getName()).isNotEqualTo(compareAlgorithm.getName());
        assertThat(editedAlgorithm.getName()).isEqualTo(editName);
        assertThat(editedAlgorithm.getComputationModel()).isEqualTo(compareAlgorithm.getComputationModel());
        assertThat(editedAlgorithm.isNisqReady()).isEqualTo(compareAlgorithm.isNisqReady());
        assertThat(editedAlgorithm.getSpeedUp()).isEqualTo(compareAlgorithm.getSpeedUp());
        assertThat(editedAlgorithm.getQuantumComputationModel()).isEqualTo(compareAlgorithm.getQuantumComputationModel());
    }

    @Test
    void deleteAlgorithm_NoLinks() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");

        Algorithm storedAlgorithm = algorithmService.create(algorithm);

        assertDoesNotThrow(() -> algorithmService.findById(storedAlgorithm.getId()));

        algorithmService.delete(storedAlgorithm.getId());

        assertThrows(NoSuchElementException.class, () ->
                algorithmService.findById(storedAlgorithm.getId()));
    }

    @Test
    void deleteAlgorithm_WithLinks() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        algorithm = algorithmService.create(algorithm);

        // add Implementation
        Implementation implementation = new Implementation();
        implementation.setName("implementationName");
        implementationService.create(implementation, algorithm.getId());

        // add pattern Relation
        PatternRelation patternRelation = new PatternRelation();
        patternRelation.setDescription("description");
        PatternRelationType type = new PatternRelationType();
        type.setName("typeName");
        type = patternRelationTypeService.create(type);
        patternRelation.setPatternRelationType(type);
        try {
            patternRelation.setPattern(new URI("http://www.example.de"));
        } catch (URISyntaxException ignored) {
        }
        patternRelation.setAlgorithm(algorithm);
        patternRelationService.create(patternRelation);

        // add Tag
        Tag tag = new Tag();
        tag.setCategory("tagCategory");
        tag.setValue("tagValue");
        tagService.addTagToAlgorithm(algorithm.getId(), tag);

        // add ProblemType
        ProblemType problemType = new ProblemType();
        problemType.setName("problemTypeName");
        problemType.setParentProblemType(UUID.randomUUID());
        problemType = problemTypeService.create(problemType);
        linkingService.linkAlgorithmAndProblemType(algorithm.getId(), problemType.getId());

        // add Publication
        Publication publication = new Publication();
        publication.setTitle("publicationTitle");
        publication.setUrl("http://example.com");
        publication.setDoi("doi");
        List<String> publicationAuthors = new ArrayList<>();
        publicationAuthors.add("publicationAuthor1");
        publication.setAuthors(publicationAuthors);
        publication = publicationService.create(publication);
        linkingService.linkAlgorithmAndPublication(algorithm.getId(), publication.getId());

        // add ApplicationArea
        ApplicationArea applicationArea = new ApplicationArea();
        applicationArea.setName("applicationAreaName");
        applicationArea = applicationAreaService.create(applicationArea);
        linkingService.linkAlgorithmAndApplicationArea(algorithm.getId(), applicationArea.getId());

        Algorithm finalAlgorithm = algorithmService.findById(algorithm.getId());

        finalAlgorithm.getImplementations().forEach(i ->
                assertDoesNotThrow(() -> implementationService.findById(i.getId())));
        finalAlgorithm.getRelatedPatterns().forEach(p ->
                assertDoesNotThrow(() -> patternRelationService.findById(p.getId())));
        finalAlgorithm.getTags().forEach(t ->
                assertDoesNotThrow(() -> tagService.findByValue(t.getValue())));
        finalAlgorithm.getPublications().forEach(pub ->
                assertDoesNotThrow(() -> publicationService.findById(pub.getId())));
        finalAlgorithm.getProblemTypes().forEach(pt ->
                assertDoesNotThrow(() -> problemTypeService.findById(pt.getId())));
        finalAlgorithm.getApplicationAreas().forEach(area ->
                assertDoesNotThrow(() -> applicationAreaService.findById(area.getId())));

        algorithmService.delete(finalAlgorithm.getId());

        assertThrows(NoSuchElementException.class, () ->
                algorithmService.findById(finalAlgorithm.getId()));

        // check if algorithm links are removed
        finalAlgorithm.getImplementations().forEach(i ->
                assertThrows(NoSuchElementException.class, () ->
                        implementationService.findById(i.getId())));
        finalAlgorithm.getRelatedPatterns().forEach(p ->
                assertThrows(NoSuchElementException.class, () ->
                        patternRelationService.findById(p.getId())));
        finalAlgorithm.getTags().forEach(t ->
                assertThat(tagService.findByValue(t.getValue()).getAlgorithms().size()).isEqualTo(0));
        finalAlgorithm.getProblemTypes().forEach(pt ->
                assertThat(problemTypeService.findById(pt.getId()).getAlgorithms().size()).isEqualTo(0));
        finalAlgorithm.getPublications().forEach(pub ->
                assertThat(publicationService.findById(pub.getId()).getAlgorithms().size()).isEqualTo(0));
        finalAlgorithm.getApplicationAreas().forEach(area ->
                assertThat(applicationAreaService.findById(area.getId()).getAlgorithms().size()).isEqualTo(0));
    }

    @Test
    void deleteAlgorithm_RevisionsNotFound() {
        Algorithm algorithm = getFullAlgorithm("Shor");
        Algorithm storedAlgorithm = algorithmService.create(algorithm);

        algorithmService.delete(storedAlgorithm.getId());

        assertThrows(NoSuchElementException.class, () -> algorithmService.findAlgorithmRevisions(storedAlgorithm.getId(), Pageable.unpaged()));
    }

    @Test
    void findAlgorithmRevision_ElementFound() {
        Algorithm algorithm = getFullAlgorithm("Shor");
        Algorithm storedAlgorithm = algorithmService.create(algorithm);

        var algorithmRevision = algorithmService.findAlgorithmRevision(storedAlgorithm.getId(), 1);

        assertThat(algorithmRevision.getRevisionNumber().get()).isEqualTo(1);
        assertThat(algorithmRevision.getRevisionInstant()).isNotEmpty();
        assertThat(algorithmRevision.getEntity().getId()).isEqualTo(storedAlgorithm.getId());
        assertThat(algorithmRevision.getEntity().getName()).isEqualTo(algorithm.getName());
        assertThat(algorithmRevision.getEntity().getAcronym()).isEqualTo(algorithm.getAcronym());
        assertThat(algorithmRevision.getEntity().getIntent()).isEqualTo(algorithm.getIntent());
        assertThat(algorithmRevision.getEntity().getProblem()).isEqualTo(algorithm.getProblem());
        assertThat(algorithmRevision.getEntity().getInputFormat()).isEqualTo(algorithm.getInputFormat());
        assertThat(algorithmRevision.getEntity().getAlgoParameter()).isEqualTo(algorithm.getAlgoParameter());
        assertThat(algorithmRevision.getEntity().getOutputFormat()).isEqualTo(algorithm.getOutputFormat());
    }

    @Test
    void findAlgorithmRevisions_ElementNotFound() {
        assertThrows(NoSuchElementException.class, () ->
                algorithmService.findAlgorithmRevisions(UUID.randomUUID(), Pageable.unpaged()));
    }

    @Test
    void findAlgorithmRevisions_ElementsFound() {
        Algorithm algorithm = getFullAlgorithm("Shor");
        Algorithm storedAlgorithm = algorithmService.create(algorithm);

        storedAlgorithm.setName("Shor_Updated");
        algorithmService.update(storedAlgorithm);

        var algorithmRevisions = algorithmService.findAlgorithmRevisions(storedAlgorithm.getId(), PageRequest.of(0, 10));
        assertThat(algorithmRevisions.getTotalElements()).isEqualTo(2);
        assertThat(algorithmRevisions.getContent().get(0).getRevisionNumber().get()).isNotNull();
        assertThat(algorithmRevisions.getContent().get(0).getRevisionInstant()).isNotEmpty();
        assertThat(algorithmRevisions.getContent().get(0).getEntity().getId()).isNotNull();

        assertThat(algorithmRevisions.getContent().get(1).getRevisionNumber().get()).isNotNull();
        assertThat(algorithmRevisions.getContent().get(1).getRevisionInstant()).isNotEmpty();
        assertThat(algorithmRevisions.getContent().get(1).getEntity().getId()).isNotNull();
    }

    @Test
    void findLinkedPublications() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        algorithm = algorithmService.create(algorithm);

        Publication publication1 = new Publication();
        publication1.setTitle("publicationTitle1");
        publication1 = publicationService.create(publication1);
        linkingService.linkAlgorithmAndPublication(algorithm.getId(), publication1.getId());
        Publication publication2 = new Publication();
        publication2.setTitle("publicationTitle2");
        publication2 = publicationService.create(publication2);
        linkingService.linkAlgorithmAndPublication(algorithm.getId(), publication2.getId());

        var linkedPublications = algorithmService.findLinkedPublications(algorithm.getId(), Pageable.unpaged());
        assertThat(linkedPublications.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findLinkedProblemTypes() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        algorithm = algorithmService.create(algorithm);

        ProblemType problemType1 = new ProblemType();
        problemType1.setName("problemTypeName1");
        problemType1 = problemTypeService.create(problemType1);
        linkingService.linkAlgorithmAndProblemType(algorithm.getId(), problemType1.getId());
        ProblemType problemType2 = new ProblemType();
        problemType2.setName("problemTypeName2");
        problemType2 = problemTypeService.create(problemType2);
        linkingService.linkAlgorithmAndProblemType(algorithm.getId(), problemType2.getId());

        var linkedProblemTypes = algorithmService.findLinkedProblemTypes(algorithm.getId(), Pageable.unpaged());
        assertThat(linkedProblemTypes.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findLinkedApplicationAreas() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        algorithm = algorithmService.create(algorithm);

        ApplicationArea applicationArea1 = new ApplicationArea();
        applicationArea1.setName("applicationAreaName1");
        applicationArea1 = applicationAreaService.create(applicationArea1);
        linkingService.linkAlgorithmAndApplicationArea(algorithm.getId(), applicationArea1.getId());
        ApplicationArea applicationArea2 = new ApplicationArea();
        applicationArea2.setName("applicationAreaName2");
        applicationArea2 = applicationAreaService.create(applicationArea2);
        linkingService.linkAlgorithmAndApplicationArea(algorithm.getId(), applicationArea2.getId());

        var linkedApplicationAreas = algorithmService.findLinkedApplicationAreas(algorithm.getId(), Pageable.unpaged());
        assertThat(linkedApplicationAreas.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findLinkedPatternRelations() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        algorithm = algorithmService.create(algorithm);

        PatternRelationType patternRelationType = new PatternRelationType();
        patternRelationType.setName("patternRelationTypeName");
        patternRelationType = patternRelationTypeService.create(patternRelationType);

        PatternRelation patternRelation1 = new PatternRelation();
        patternRelation1.setPattern(URI.create("http://patternRelation1.com"));
        patternRelation1.setPatternRelationType(patternRelationType);
        patternRelation1.setAlgorithm(algorithm);
        patternRelationService.create(patternRelation1);

        PatternRelation patternRelation2 = new PatternRelation();
        patternRelation2.setPattern(URI.create("http://patternRelation2.com"));
        patternRelation2.setPatternRelationType(patternRelationType);
        patternRelation2.setAlgorithm(algorithm);
        patternRelationService.create(patternRelation2);

        var linkedPatternRelations = algorithmService.findLinkedPatternRelations(algorithm.getId(), Pageable.unpaged());
        assertThat(linkedPatternRelations.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findLinkedAlgorithmRelations() {
        Algorithm sourceAlgorithm = getFullAlgorithm("sourceAlgorithmName");
        sourceAlgorithm = algorithmService.create(sourceAlgorithm);
        Algorithm targetAlgorithm1 = getFullAlgorithm("targetAlgorithmName");
        targetAlgorithm1 = algorithmService.create(targetAlgorithm1);
        Algorithm targetAlgorithm2 = getFullAlgorithm("targetAlgorithmName");
        targetAlgorithm2 = algorithmService.create(targetAlgorithm2);

        var algorithmRelationType = new AlgorithmRelationType();
        algorithmRelationType.setName("relationName");
        algorithmRelationType = algorithmRelationTypeService.create(algorithmRelationType);

        var algorithmRelation1 = new AlgorithmRelation();
        algorithmRelation1.setDescription("algorithmRelationDescription1");
        algorithmRelation1.setSourceAlgorithm(sourceAlgorithm);
        algorithmRelation1.setTargetAlgorithm(targetAlgorithm1);
        algorithmRelation1.setAlgorithmRelationType(algorithmRelationType);
        algorithmRelation1 = algorithmRelationService.create(algorithmRelation1);

        var algorithmRelation2 = new AlgorithmRelation();
        algorithmRelation2.setDescription("algorithmRelationDescription2");
        algorithmRelation2.setSourceAlgorithm(sourceAlgorithm);
        algorithmRelation2.setTargetAlgorithm(targetAlgorithm2);
        algorithmRelation2.setAlgorithmRelationType(algorithmRelationType);
        algorithmRelation2 = algorithmRelationService.create(algorithmRelation2);

        var linkedAlgorithmRelationsSource1 = algorithmService.findLinkedAlgorithmRelations(
                algorithmRelation1.getSourceAlgorithm().getId(), Pageable.unpaged());

        var linkedAlgorithmRelationsSource2 = algorithmService.findLinkedAlgorithmRelations(
                algorithmRelation2.getSourceAlgorithm().getId(), Pageable.unpaged());

        assertThat(linkedAlgorithmRelationsSource1.getContent()
                .containsAll(linkedAlgorithmRelationsSource2.getContent())).isTrue();
        assertThat(linkedAlgorithmRelationsSource2.getContent()
                .containsAll(linkedAlgorithmRelationsSource1.getContent())).isTrue();
        assertThat(linkedAlgorithmRelationsSource1.getTotalElements()).isEqualTo(2);

        var linkedAlgorithmRelationsTarget1 = algorithmService.findLinkedAlgorithmRelations(
                algorithmRelation1.getTargetAlgorithm().getId(), Pageable.unpaged());

        var linkedAlgorithmRelationsTarget2 = algorithmService.findLinkedAlgorithmRelations(
                algorithmRelation2.getTargetAlgorithm().getId(), Pageable.unpaged());

        assertThat(linkedAlgorithmRelationsTarget1.getTotalElements()).isEqualTo(1);
        assertThat(linkedAlgorithmRelationsTarget2.getTotalElements()).isEqualTo(1);
        assertThat(linkedAlgorithmRelationsTarget1.getContent()
                .containsAll(linkedAlgorithmRelationsTarget2.getContent())).isFalse();

        assertThat(linkedAlgorithmRelationsSource1.getContent()
                .containsAll(linkedAlgorithmRelationsTarget1.getContent())).isTrue();
        assertThat(linkedAlgorithmRelationsSource1.getContent()
                .containsAll(linkedAlgorithmRelationsTarget2.getContent())).isTrue();
    }

    @Test
    void checkIfPublicationIsLinkedToAlgorithm_IsLinked() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        Algorithm persistedAlgorithm = algorithmService.create(algorithm);

        Publication publication = new Publication();
        publication.setTitle("publicationTitle");
        Publication persistedPublication = publicationService.create(publication);
        linkingService.linkAlgorithmAndPublication(persistedAlgorithm.getId(), persistedPublication.getId());

        assertDoesNotThrow(() -> algorithmService
                .checkIfPublicationIsLinkedToAlgorithm(persistedAlgorithm.getId(), persistedPublication.getId()));
    }

    @Test
    void checkIfPublicationIsLinkedToAlgorithm_IsNotLinked() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        Algorithm persistedAlgorithm = algorithmService.create(algorithm);

        Publication publication = new Publication();
        publication.setTitle("publicationTitle");
        Publication persistedPublication = publicationService.create(publication);

        assertThrows(NoSuchElementException.class, () -> algorithmService
                .checkIfPublicationIsLinkedToAlgorithm(persistedAlgorithm.getId(), persistedPublication.getId()));
    }

    @Test
    void checkIfProblemTypeIsLinkedToAlgorithm_IsLinked() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        Algorithm persistedAlgorithm = algorithmService.create(algorithm);

        ProblemType problemType = new ProblemType();
        problemType.setName("problemTypeName");
        ProblemType persistedProblemType = problemTypeService.create(problemType);
        linkingService.linkAlgorithmAndProblemType(persistedAlgorithm.getId(), persistedProblemType.getId());

        assertDoesNotThrow(() -> algorithmService
                .checkIfProblemTypeIsLinkedToAlgorithm(persistedAlgorithm.getId(), persistedProblemType.getId()));
    }

    @Test
    void checkIfProblemTypeIsLinkedToAlgorithm_IsNotLinked() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        Algorithm persistedAlgorithm = algorithmService.create(algorithm);

        ProblemType problemType = new ProblemType();
        problemType.setName("problemTypeName");
        ProblemType persistedProblemType = problemTypeService.create(problemType);

        assertThrows(NoSuchElementException.class, () -> algorithmService
                .checkIfProblemTypeIsLinkedToAlgorithm(persistedAlgorithm.getId(), persistedProblemType.getId()));
    }

    @Test
    void checkIfApplicationAreaIsLinkedToAlgorithm_IsLinked() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        Algorithm persistedAlgorithm = algorithmService.create(algorithm);

        ApplicationArea applicationArea = new ApplicationArea();
        applicationArea.setName("applicationAreaName");
        ApplicationArea persistedApplicationArea = applicationAreaService.create(applicationArea);
        linkingService.linkAlgorithmAndApplicationArea(persistedAlgorithm.getId(), persistedApplicationArea.getId());

        assertDoesNotThrow(() -> algorithmService
                .checkIfApplicationAreaIsLinkedToAlgorithm(persistedAlgorithm.getId(), persistedApplicationArea.getId()));
    }

    @Test
    void checkIfApplicationAreaIsLinkedToAlgorithm_IsNotLinked() {
        Algorithm algorithm = getFullAlgorithm("algorithmName");
        Algorithm persistedAlgorithm = algorithmService.create(algorithm);

        ApplicationArea applicationArea = new ApplicationArea();
        applicationArea.setName("applicationAreaName");
        ApplicationArea persistedApplicationArea = applicationAreaService.create(applicationArea);

        assertThrows(NoSuchElementException.class, () -> algorithmService
                .checkIfApplicationAreaIsLinkedToAlgorithm(persistedAlgorithm.getId(), persistedApplicationArea.getId()));
    }

    private Algorithm getFullAlgorithm(String name) {
        Algorithm algorithm = new ClassicAlgorithm();
        algorithm.setName(name);
        algorithm.setAcronym("testAcronym");
        algorithm.setIntent("testIntent");
        algorithm.setProblem("testProblem");
        algorithm.setInputFormat("testInputFormat");
        algorithm.setAlgoParameter("testAlgoParameter");
        algorithm.setOutputFormat("testOutputFormat");
        algorithm.setSketches(new ArrayList<>());
        algorithm.setSolution("testSolution");
        algorithm.setAssumptions("testAssumptions");
        algorithm.setComputationModel(ComputationModel.CLASSIC);
        return algorithm;
    }
}
