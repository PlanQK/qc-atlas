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

package org.planqk.atlas.core.repository;

import java.util.UUID;

import org.planqk.atlas.core.model.Implementation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/**
 * Repository to access {@link Implementation}s available in the data base with different queries.
 */
@Repository
@RepositoryRestResource(exported = false)
public interface ImplementationRepository extends RevisionRepository<Implementation, UUID, Integer>, JpaRepository<Implementation, UUID> {

    @Query("SELECT impl " +
                   "FROM Implementation impl " +
                   "JOIN impl.implementedAlgorithms alg " +
                   "WHERE  alg.id = :algId")
    Page<Implementation> findByImplementedAlgorithmId(@Param("algId") UUID implementedAlgorithmId, Pageable pageable);

    @Query("SELECT impl " +
                   "FROM Implementation impl " +
                   "JOIN impl.publications pub " +
                   "WHERE  pub.id = :pubId")
    Page<Implementation> findImplementationsByPublicationId(@Param("pubId") UUID publicationId, Pageable pageable);

    @Query("SELECT i " +
                   "FROM Implementation i " +
                   "JOIN i.softwarePlatforms sp " +
                   "WHERE sp.id = :spId")
    Page<Implementation> findImplementationsBySoftwarePlatformId(@Param("spId") UUID softwarePlatformId, Pageable pageable);

    @Modifying()
    @Query(value = "DELETE FROM implementation_revisions WHERE id = :implId", nativeQuery = true)
    void deleteAllImplementationRevisions(@Param("implId") UUID implementationId);

    @Modifying()
    @Query(value = "DELETE FROM implementation_revisions WHERE rev = :revId AND id = :implId", nativeQuery = true)
    void deleteImplementationRevision(@Param("revId") Integer revisionId, @Param("implId") UUID implementationId);

    @Modifying()
    @Query(value = "DELETE FROM classic_implementation_revisions WHERE rev = :revId AND id = :implId", nativeQuery = true)
    void deleteClassicImplementationRevision(@Param("revId") Integer revisionId, @Param("implId") UUID implementationId);

    @Modifying()
    @Query(value = "DELETE FROM classic_implementation_revisions WHERE id = :implId", nativeQuery = true)
    void deleteAllClassicImplementationRevisions(@Param("implId") UUID implementationId);

    @Modifying()
    @Query(value = "DELETE FROM quantum_implementation_revisions WHERE rev = :revId AND id = :implId", nativeQuery = true)
    void deleteQuantumImplementationRevision(@Param("revId") Integer revisionId, @Param("implId") UUID implementationId);

    @Modifying()
    @Query(value = "DELETE FROM quantum_implementation_revisions WHERE id = :implId", nativeQuery = true)
    void deleteAllQuantumImplementationRevisions(@Param("implId") UUID implementationId);
}
