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

import org.planqk.atlas.core.model.CloudService;
import org.planqk.atlas.core.model.ComputeResource;
import org.planqk.atlas.core.model.SoftwarePlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for operations related to interacting and modifying {@link CloudService}s in the database.
 */
public interface CloudServiceService {

    /**
     * Retrieve multiple {@link CloudService} entries from the database where their name matches the name search
     * parameter. If there are no matches found an empty {@link Page} will be returned.
     * <p>
     * The amount of entries is based on the given {@link Pageable} parameter. If the {@link Pageable} is unpaged a
     * {@link Page} with all entries is queried.
     *
     * @param name     The string based on which a search for {@link CloudService}s with a matching name will be
     *                 executed
     * @param pageable The page information, namely page size and page number, of the page we want to retrieve
     * @return The page of queried {@link CloudService} entries which match the search name
     */
    Page<CloudService> searchAllByName(String name, Pageable pageable);

    /**
     * Creates a new database entry for a given {@link CloudService} and save it to the database.
     * <p>
     * The ID of the {@link CloudService} parameter should be null, since the ID will be generated by the database when
     * creating the entry. The validation for this is done by the Controller layer, which will reject {@link
     * CloudService}s with a given ID in its create path.
     *
     * @param cloudService The {@link CloudService} that should be saved to the database
     * @return The {@link CloudService} object that represents the saved status from the database
     */
    @Transactional
    CloudService create(CloudService cloudService);

    /**
     * Retrieve multiple {@link CloudService} entries from the database.
     * <p>
     * The amount of entries is based on the given {@link Pageable} parameter. If the {@link Pageable} is unpaged a
     * {@link Page} with all entries is queried.
     *
     * @param pageable The page information, namely page size and page number, of the page we want to retrieve
     * @return The page of queried {@link CloudService} entries
     */
    Page<CloudService> findAll(Pageable pageable);

    /**
     * Find a database entry of a {@link CloudService} that is already saved in the database. This search is based on
     * the ID the database has given the {@link CloudService} object when it was created and first saved to the
     * database.
     * <p>
     * If there is no entry found in the database this method will throw a {@link java.util.NoSuchElementException}.
     *
     * @param cloudServiceId The ID of the {@link CloudService} we want to find
     * @return The {@link CloudService} with the given ID
     */
    CloudService findById(UUID cloudServiceId);

    /**
     * Update an existing {@link CloudService} database entry by saving the updated {@link CloudService} object to the
     * the database.
     * <p>
     * The ID of the {@link CloudService} parameter has to be set to the ID of the database entry we want to update. The
     * validation for this ID to be set is done by the Controller layer, which will reject {@link CloudService}s without
     * a given ID in its update path. This ID will be used to query the existing {@link CloudService} entry we want to
     * update. If no {@link CloudService} entry with the given ID is found this method will throw a {@link
     * java.util.NoSuchElementException}.
     *
     * @param cloudService The {@link CloudService} we want to update with its updated properties
     * @return the updated {@link CloudService} object that represents the updated status of the database
     */
    @Transactional
    CloudService update(CloudService cloudService);

    /**
     * Delete an existing {@link CloudService} entry from the database. This deletion is based on the ID the database
     * has given the {@link CloudService} when it was created and first saved to the database.
     * <p>
     * Objects that can be related to multiple {@link CloudService}s will not be deleted. Only the reference to the
     * deleted {@link CloudService} will be removed from these objects. These include {@link SoftwarePlatform}s and
     * {@link ComputeResource}s.
     * <p>
     * If no entry with the given ID is found this method will throw a {@link java.util.NoSuchElementException}.
     *
     * @param cloudServiceId The ID of the {@link CloudService} we want to delete
     */
    @Transactional
    void delete(UUID cloudServiceId);

    /**
     * Retrieve multiple {@link SoftwarePlatform}s entries from the database of {@link SoftwarePlatform}s that are
     * linked to the given {@link CloudService}. If no entries are found an empty page is returned.
     * <p>
     * The amount of entries is based on the given {@link Pageable} parameter. If the {@link Pageable} is unpaged a
     * {@link Page} with all entries is queried.
     * <p>
     * The given {@link CloudService} is identified through its ID given as a parameter. If no {@link CloudService} with
     * the given ID can be found a {@link java.util.NoSuchElementException} is thrown.
     *
     * @param cloudServiceId The ID of the {@link CloudService} we want find linked {@link SoftwarePlatform}s for
     * @param pageable       The page information, namely page size and page number, of the page we want to retrieve
     * @return The page of queried {@link SoftwarePlatform} entries which are linked to the {@link CloudService}
     */
    Page<SoftwarePlatform> findLinkedSoftwarePlatforms(UUID cloudServiceId, Pageable pageable);

    /**
     * Retrieve multiple {@link ComputeResource}s entries from the database of {@link ComputeResource}s that are linked
     * to the given {@link CloudService}. If no entries are found an empty page is returned.
     * <p>
     * The amount of entries is based on the given {@link Pageable} parameter. If the {@link Pageable} is unpaged a
     * {@link Page} with all entries is queried.
     * <p>
     * The given {@link CloudService} is identified through its ID given as a parameter. If no {@link CloudService} with
     * the given ID can be found a {@link java.util.NoSuchElementException} is thrown.
     *
     * @param cloudServiceId The ID of the {@link CloudService} we want find linked {@link ComputeResource}s for
     * @param pageable       The page information, namely page size and page number, of the page we want to retrieve
     * @return The page of queried {@link ComputeResource} entries which are linked to the {@link CloudService}
     */
    Page<ComputeResource> findLinkedComputeResources(UUID cloudServiceId, Pageable pageable);
}
