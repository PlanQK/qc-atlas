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
import java.util.Optional;
import java.util.UUID;

import org.planqk.atlas.core.model.Provider;
import org.planqk.atlas.core.model.exceptions.NotFoundException;
import org.planqk.atlas.core.repository.ProviderRepository;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class ProviderServiceImpl implements ProviderService {

	private ProviderRepository repository;

	@Override
	public Provider save(Provider provider) {
		return repository.save(provider);
	}

	@Override
	public Page<Provider> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@Override
	public Provider findById(UUID providerId) throws NotFoundException {
		Optional<Provider> providerOptional = Objects.isNull(providerId) ? Optional.empty()
				: repository.findById(providerId);
		if (providerOptional.isPresent())
			return providerOptional.get();
		throw new NotFoundException("The provider does not exist!");
	}
}
