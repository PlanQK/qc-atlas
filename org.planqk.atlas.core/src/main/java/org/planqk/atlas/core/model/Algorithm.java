/*******************************************************************************
 * Copyright (c) 2020-2021 the qc-atlas contributors.
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

package org.planqk.atlas.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Entity representing a quantum algorithm, e.g., Shors factorization algorithm.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AuditTable("algorithm_revisions")
@Audited
public class Algorithm extends KnowledgeArtifact {

    private String name;

    private String acronym;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "algorithm_publication",
               joinColumns = @JoinColumn(name = "algorithm_id"),
               inverseJoinColumns = @JoinColumn(name = "publication_id")
    )
    @EqualsAndHashCode.Exclude
    @NotAudited
    private Set<Publication> publications = new HashSet<>();

    @Column(columnDefinition = "text")
    private String intent;

    @Column(columnDefinition = "text")
    private String problem;

    @Column(columnDefinition = "text")
    private String inputFormat;

    @Column(columnDefinition = "text")
    private String outputFormat;

    @OneToMany(fetch = FetchType.LAZY,
               cascade = {CascadeType.ALL},
               mappedBy = "sourceAlgorithm",
               orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @NotAudited
    private Set<AlgorithmRelation> sourceAlgorithmRelations = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY,
               cascade = {CascadeType.ALL},
               mappedBy = "targetAlgorithm",
               orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @NotAudited
    private Set<AlgorithmRelation> targetAlgorithmRelations = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY,
               cascade = CascadeType.ALL,
               mappedBy = "algorithm",
               orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @NotAudited
    private Set<ComputeResourceProperty> requiredComputeResourceProperties = new HashSet<>();

    @Column(columnDefinition = "text")
    private String algoParameter;

    @OneToMany(mappedBy = "algorithm", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @NotAudited
    private List<Sketch> sketches = new ArrayList<>();

    @Column(columnDefinition = "text")
    private String solution;

    private String assumptions;

    private ComputationModel computationModel;

    @OneToMany(fetch = FetchType.LAZY,
               cascade = CascadeType.ALL,
               mappedBy = "algorithm",
               orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @NotAudited
    private Set<PatternRelation> relatedPatterns = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "algorithm_problem_type",
               joinColumns = @JoinColumn(name = "algorithm_id"),
               inverseJoinColumns = @JoinColumn(name = "problem_type_id"))
    @EqualsAndHashCode.Exclude
    @NotAudited
    private Set<ProblemType> problemTypes = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "algorithm_application_area",
               joinColumns = @JoinColumn(name = "algorithm_id"),
               inverseJoinColumns = @JoinColumn(name = "application_area_id"))
    @EqualsAndHashCode.Exclude
    @NotAudited
    private Set<ApplicationArea> applicationAreas = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "algorithm_tag",
               joinColumns = @JoinColumn(name = "algorithm_id"),
               inverseJoinColumns = @JoinColumn(name = "tag_value"))
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "algorithm_implementation",
            joinColumns = @JoinColumn(name = "algorithm_id"),
            inverseJoinColumns = @JoinColumn(name = "implementation_id"))
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    private Set<Implementation> implementations = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "algorithm_learning_method",
               joinColumns = @JoinColumn(name = "algorithm_id"),
               inverseJoinColumns = @JoinColumn(name = "learning_method_id"))
    @EqualsAndHashCode.Exclude
    @NotAudited
    private Set<LearningMethod> learningMethods = new HashSet<>();

    public void addTag(@NonNull Tag tag) {
        if (tags.contains(tag)) {
            return;
        }
        this.tags.add(tag);
        tag.addAlgorithm(this);
    }

    public void removeTag(@NonNull Tag tag) {
        if (!tags.contains(tag)) {
            return;
        }
        this.tags.remove(tag);
        tag.removeAlgorithm(this);
    }

    public void addAlgorithmRelation(@NonNull AlgorithmRelation algorithmRelation) {
        if (algorithmRelation.getSourceAlgorithm().getId() == this.getId()) {
            sourceAlgorithmRelations.add(algorithmRelation);
            algorithmRelation.setSourceAlgorithm(this);
        } else if (algorithmRelation.getTargetAlgorithm().getId() == this.getId()) {
            targetAlgorithmRelations.add(algorithmRelation);
            algorithmRelation.setTargetAlgorithm(this);
        }
    }

    public void removeAlgorithmRelation(@NonNull AlgorithmRelation algorithmRelation) {
        if (algorithmRelation.getSourceAlgorithm().getId() == this.getId()) {
            sourceAlgorithmRelations.remove(algorithmRelation);
            algorithmRelation.setSourceAlgorithm(null);
        } else if (algorithmRelation.getTargetAlgorithm().getId() == this.getId()) {
            targetAlgorithmRelations.remove(algorithmRelation);
            algorithmRelation.setTargetAlgorithm(null);
        }
    }

    public Set<AlgorithmRelation> getAlgorithmRelations() {
        final Set<AlgorithmRelation> algorithmRelations = new HashSet<>(sourceAlgorithmRelations);
        algorithmRelations.addAll(targetAlgorithmRelations);
        return algorithmRelations;
    }

    public void setRelatedPatterns(@NonNull Set<PatternRelation> relatedPatterns) {
        this.relatedPatterns.clear();
        if (relatedPatterns != null) {
            this.relatedPatterns.addAll(relatedPatterns);
        }
    }

    public void addPublication(@NonNull Publication publication) {
        if (publications.contains(publication)) {
            return;
        }
        publications.add(publication);
        publication.addAlgorithm(this);
    }

    public void removePublication(@NonNull Publication publication) {
        if (!publications.contains(publication)) {
            return;
        }
        publications.remove(publication);
        publication.removeAlgorithm(this);
    }

    public void addApplicationArea(@NonNull ApplicationArea applicationArea) {
        if (applicationAreas.contains(applicationArea)) {
            return;
        }
        applicationAreas.add(applicationArea);
        applicationArea.addAlgorithm(this);
    }

    public void removeApplicationArea(@NonNull ApplicationArea applicationArea) {
        if (!applicationAreas.contains(applicationArea)) {
            return;
        }
        applicationAreas.remove(applicationArea);
        applicationArea.removeAlgorithm(this);
    }

    public void addProblemType(@NonNull ProblemType problemType) {
        if (problemTypes.contains(problemType)) {
            return;
        }
        problemTypes.add(problemType);
        problemType.addAlgorithm(this);
    }

    public void removeProblemType(@NonNull ProblemType problemType) {
        if (!problemTypes.contains(problemType)) {
            return;
        }
        problemTypes.remove(problemType);
        problemType.removeAlgorithm(this);
    }

    public void setSketches(List<Sketch> sketches) {
        this.sketches.clear();
        if (sketches != null) {
            sketches.forEach(sketch -> this.addSketch(sketch));
        }
    }

    public Algorithm addSketch(Sketch sketch) {
        sketches.add(sketch);
        sketch.setAlgorithm(this);
        return this;
    }

    public Algorithm removeSketch(Sketch sketch) {
        sketches.remove(sketch);
        sketch.setAlgorithm(null);
        return this;
    }

    public void addLearningMethod(@NonNull LearningMethod learningMethod) {
        if (learningMethods.contains(learningMethod)) {
            return;
        }
        learningMethods.add(learningMethod);
        learningMethod.getAlgorithms().add(this);
    }

    public void removeLearningMethod(@NonNull LearningMethod learningMethod) {
        if (!learningMethods.contains(learningMethod)) {
            return;
        }
        learningMethods.remove(learningMethod);
        learningMethod.getAlgorithms().remove(this);
    }

    public void addImplementation(@NonNull Implementation implementation) {
        if (!implementations.contains(implementation)) {
            implementations.add(implementation);
            implementation.getImplementedAlgorithms().add(this);
        }
    }

    public void removeImplementation(@NonNull Implementation implementation) {
        if (implementations.contains(implementation)) {
            implementations.remove(implementation);
            implementation.getImplementedAlgorithms().remove(this);
        }
    }
}
