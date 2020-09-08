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

package org.planqk.atlas.web.controller;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.planqk.atlas.core.exceptions.EntityReferenceConstraintViolationException;
import org.planqk.atlas.core.model.ComputeResource;
import org.planqk.atlas.core.model.ComputeResourceProperty;
import org.planqk.atlas.core.model.ComputeResourcePropertyDataType;
import org.planqk.atlas.core.model.ComputeResourcePropertyType;
import org.planqk.atlas.core.services.ComputeResourcePropertyService;
import org.planqk.atlas.core.services.ComputeResourceService;
import org.planqk.atlas.web.Constants;
import org.planqk.atlas.web.controller.util.ObjectMapperUtils;
import org.planqk.atlas.web.dtos.ComputeResourceDto;
import org.planqk.atlas.web.linkassembler.EnableLinkAssemblers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@WebMvcTest(ComputeResourceController.class)
@ExtendWith(MockitoExtension.class)
@EnableLinkAssemblers
@AutoConfigureMockMvc
public class ComputeResourceControllerTest {

    @MockBean
    private ComputeResourceService computeResourceService;
    @MockBean
    private ComputeResourcePropertyService computeResourcePropertyService;

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper mapper = ObjectMapperUtils.newTestMapper();
    private final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/");

    private final int page = 0;
    private final int size = 2;
    private final Pageable pageable = PageRequest.of(page, size);

    @Test
    public void addComputeResource_returnBadRequest() throws Exception {
        var resource = new ComputeResourceDto();
        resource.setId(UUID.randomUUID());

        mockMvc.perform(
                post(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class).createComputeResource(null)
                        ).toUriString()
                ).content(mapper.writeValueAsString(resource))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void addComputeResource_returnCreated() throws Exception {
        var resource = new ComputeResourceDto();
        resource.setName("Hello World");

        var returnedResource = new ComputeResource();
        returnedResource.setName(resource.getName());
        returnedResource.setId(UUID.randomUUID());

        doReturn(returnedResource).when(computeResourceService).create(any());

        mockMvc.perform(
                post(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class).createComputeResource(null)
                        ).toUriString()
                ).content(mapper.writeValueAsString(resource))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(returnedResource.getId().toString()))
                .andExpect(jsonPath("$.name").value(returnedResource.getName()));
    }

    @Test
    public void updateComputeResource_returnNotFound() throws Exception {
        var resource = new ComputeResourceDto();
        resource.setId(UUID.randomUUID());
        resource.setName("Hello World");

        doThrow(new NoSuchElementException()).when(computeResourceService).update(any());

        mockMvc.perform(
                put(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class).updateComputeResource(UUID.randomUUID(), null)
                        ).toUriString()
                ).content(mapper.writeValueAsString(resource))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void updateComputeResource_returnBadRequest() throws Exception {
        var resource = new ComputeResourceDto();
        resource.setId(UUID.randomUUID());

        mockMvc.perform(
                put(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class).updateComputeResource(UUID.randomUUID(), null)
                        ).toUriString()
                ).content(mapper.writeValueAsString(resource))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void updateComputeResource_returnOk() throws Exception {
        var resource = new ComputeResourceDto();
        resource.setId(UUID.randomUUID());
        resource.setName("Hello World");

        var returnedResource = new ComputeResource();
        returnedResource.setName(resource.getName());
        returnedResource.setId(resource.getId());

        doReturn(returnedResource).when(computeResourceService).update(any());

        mockMvc.perform(
                put(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class).updateComputeResource(UUID.randomUUID(), null)
                        ).toUriString()
                ).content(mapper.writeValueAsString(resource))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(resource.getId().toString()))
                .andExpect(jsonPath("$.name").value(resource.getName()));
    }

    @Test
    void getComputeResource_returnOk() throws Exception {
        var resource = new ComputeResource();
        resource.setId(UUID.randomUUID());
        resource.setName("Test");

        doReturn(resource).when(computeResourceService).findById(any());

        mockMvc.perform(
                get(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class).getComputeResource(resource.getId())
                        ).toUriString()
                ).accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resource.getId().toString()))
                .andExpect(jsonPath("$.name").value(resource.getName()));
    }

    @Test
    void getComputeResource_returnNotFound() throws Exception {
        doThrow(new NoSuchElementException()).when(computeResourceService).findById(any());

        mockMvc.perform(
                get(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class).getComputeResource(UUID.randomUUID())
                        ).toUriString()
                ).accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void listComputeResources_empty() throws Exception {
        doReturn(Page.empty()).when(computeResourceService).findAll(any());

        var mvcResult = mockMvc.perform(
                get(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class)
                                        .getComputeResources(null)
                        ).toUriString()
                ).queryParam(Constants.PAGE, Integer.toString(page))
                        .queryParam(Constants.SIZE, Integer.toString(size))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        var page = ObjectMapperUtils.getPageInfo(mvcResult.getResponse().getContentAsString());

        assertThat(page.getSize()).isEqualTo(0);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void searchComputeResources_empty() throws Exception {
        doReturn(Page.empty()).when(computeResourceService).searchAllByName(any(), any());

        var mvcResult = mockMvc.perform(
                get(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class)
                                        .getComputeResources(null)
                        ).toUriString()
                ).queryParam(Constants.PAGE, Integer.toString(page))
                        .queryParam(Constants.SIZE, Integer.toString(size))
                        .queryParam(Constants.SEARCH, "hello")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        var page = ObjectMapperUtils.getPageInfo(mvcResult.getResponse().getContentAsString());

        assertThat(page.getSize()).isEqualTo(0);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void listComputeResources_notEmpty() throws Exception {
        var inputList = new ArrayList<ComputeResource>();
        for (int i = 0; i < 50; i++) {
            var element = new ComputeResource();
            element.setName("Test Element " + i);
            element.setId(UUID.randomUUID());
            inputList.add(element);
        }
        doReturn(new PageImpl<>(inputList)).when(computeResourceService).findAll(any());

        var mvcResult = mockMvc.perform(
                get(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class)
                                        .getComputeResources(null)
                        ).toUriString()
                ).queryParam(Constants.PAGE, Integer.toString(page))
                        .queryParam(Constants.SIZE, Integer.toString(size))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        var dtoElements = ObjectMapperUtils.mapResponseToList(
                mvcResult.getResponse().getContentAsString(),
                "computeResources",
                ComputeResourceDto.class
        );
        assertThat(dtoElements.size()).isEqualTo(inputList.size());
        // Ensure every element in the input array also exists in the output array.
        inputList.forEach(e -> {
            assertThat(dtoElements.stream().filter(dtoElem -> e.getId().equals(dtoElem.getId())).count()).isEqualTo(1);
        });
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void listComputationResourceProperties_empty() throws Exception {
        doReturn(Page.empty()).when(computeResourcePropertyService)
                .findComputeResourcePropertiesOfComputeResource(any(), any());

        var mvcResult = mockMvc.perform(
                get(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class)
                                        .getComputingResourcePropertiesOfComputeResource(UUID.randomUUID(), null)
                        ).toUriString()
                ).queryParam(Constants.PAGE, Integer.toString(page))
                        .queryParam(Constants.SIZE, Integer.toString(size))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        var page = ObjectMapperUtils.getPageInfo(mvcResult.getResponse().getContentAsString());

        assertThat(page.getSize()).isEqualTo(0);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void listComputationResourceProperties_notEmpty() throws Exception {
        var inputList = new ArrayList<ComputeResourceProperty>();
        var type = new ComputeResourcePropertyType();
        type.setId(UUID.randomUUID());
        type.setName("test");
        type.setDatatype(ComputeResourcePropertyDataType.STRING);
        for (int i = 0; i < 50; i++) {
            var element = new ComputeResourceProperty();
            element.setValue("Test Element " + i);
            element.setId(UUID.randomUUID());
            element.setComputeResourcePropertyType(type);
            inputList.add(element);
        }
        doReturn(new PageImpl<>(inputList)).when(computeResourcePropertyService)
                .findComputeResourcePropertiesOfComputeResource(any(), any());

        var mvcResult = mockMvc.perform(
                get(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class)
                                        .getComputingResourcePropertiesOfComputeResource(UUID.randomUUID(), null)
                        ).toUriString()
                ).queryParam(Constants.PAGE, Integer.toString(page))
                        .queryParam(Constants.SIZE, Integer.toString(size))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        var dtoElements = ObjectMapperUtils.mapResponseToList(
                mvcResult.getResponse().getContentAsString(),
                "computeResourceProperties",
                ComputeResourceDto.class
        );
        assertThat(dtoElements.size()).isEqualTo(inputList.size());
        // Ensure every element in the input array also exists in the output array.
        inputList.forEach(e -> {
            assertThat(dtoElements.stream().filter(dtoElem -> e.getId().equals(dtoElem.getId())).count()).isEqualTo(1);
        });
    }

    @Test
    void deleteCloudService_returnNotFound() throws Exception {
        doThrow(new NoSuchElementException()).when(computeResourceService).delete(any());
        mockMvc.perform(
                delete(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class).deleteComputeResource(UUID.randomUUID())
                        ).toUriString()
                ).accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    void deleteCloudService_returnNoContent() throws Exception {
        doNothing().when(computeResourceService).delete(any());
        mockMvc.perform(
                delete(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class).deleteComputeResource(UUID.randomUUID())
                        ).toUriString()
                ).accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }

    @Test
    void deleteCloudService_returnBadRequest() throws Exception {
        doThrow(new EntityReferenceConstraintViolationException("")).when(computeResourceService).delete(any());
        mockMvc.perform(
                delete(
                        fromMethodCall(uriBuilder,
                                on(ComputeResourceController.class).deleteComputeResource(UUID.randomUUID())
                        ).toUriString()
                ).accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }
}