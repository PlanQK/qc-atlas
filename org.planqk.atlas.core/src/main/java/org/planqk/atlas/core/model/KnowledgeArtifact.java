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

package org.planqk.atlas.core.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Base class for all knowledge artifacts.
 * <p>
 *     This base class implements the id column and generation and
 *     the relation to the discussion topics (see {@link DiscussionTopic}).
 * </p>
 * <p>
 *     The joined table inheritance strategy is used for this class.
 *     See ADR 0009-joined-table-for-knowledge-artifact.md for background
 *     information.
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@Data
@Audited
@AuditTable("knowledge_artifact_revisions")
public class KnowledgeArtifact extends HasId {

    private Date creationDate;

    private Date lastModifiedAt;

    @NotAudited
    @OneToMany(cascade = CascadeType.ALL,
               mappedBy = "knowledgeArtifact",
               orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    private Set<DiscussionTopic> discussionTopics = new HashSet<>();
}
