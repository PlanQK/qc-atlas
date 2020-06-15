package org.planqk.atlas.core.services;

import lombok.AllArgsConstructor;

import org.planqk.atlas.core.model.Backend;
import org.planqk.atlas.core.model.CloudService;
import org.planqk.atlas.core.repository.CloudServiceRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.planqk.atlas.core.model.CloudService;
import org.planqk.atlas.core.repository.CloudServiceRepository;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CloudServiceServiceImpl implements CloudServiceService {

    private final CloudServiceRepository cloudServiceRepository;
    private final BackendService backendService;

    @Override
    public CloudService save(CloudService cloudService) {
        backendService.saveOrUpdateAll(cloudService.getProvidedBackends());
        return this.cloudServiceRepository.save(cloudService);
    }

    @Override
    public Set<CloudService> createOrUpdateAll(Set<CloudService> cloudServices) {
        return cloudServices.stream().map(this::createOrUpdate).collect(Collectors.toSet());
    }

    @Override
    public CloudService createOrUpdate(CloudService cloudService) {
        if (cloudService.getId() == null) {
            return this.save(cloudService);
        }

        CloudService updatingCloudService = findById(cloudService.getId());
        updatingCloudService.setName(cloudService.getName());
        updatingCloudService.setProvider(cloudService.getProvider());
        updatingCloudService.setUrl(cloudService.getUrl());
        updatingCloudService.setCostModel(cloudService.getCostModel());
        updatingCloudService.setProvidedBackends(cloudService.getProvidedBackends());

        return this.save(updatingCloudService);
    }

    @Override
    public Page<CloudService> findAll(Pageable pageable) {
        return cloudServiceRepository.findAll(pageable);
    }

    @Override
    public CloudService findById(UUID cloudServiceId) {
        return cloudServiceRepository.findById(cloudServiceId).orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    @Override
    public void delete(UUID cloudServiceId) {
        cloudServiceRepository.deleteById(cloudServiceId);
    }
}
