/********************************************************************************
 * Copyright (c) 2022, 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2022, 2023 ZF Friedrichshafen AG
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.traceability.infrastructure.edc.notificationcontract.service.contract.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.traceability.infrastructure.edc.notificationcontract.service.asset.model.CreateEdcAssetException;
import org.eclipse.tractusx.traceability.infrastructure.edc.notificationcontract.service.asset.model.EdcContext;
import org.eclipse.tractusx.traceability.infrastructure.edc.notificationcontract.service.contract.model.CreateEdcContractDefinitionException;
import org.eclipse.tractusx.traceability.infrastructure.edc.notificationcontract.service.contract.model.EdcContractDefinitionCriteria;
import org.eclipse.tractusx.traceability.infrastructure.edc.notificationcontract.service.contract.model.EdcCreateContractDefinitionRequest;
import org.eclipse.tractusx.traceability.infrastructure.edc.properties.EdcProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.eclipse.tractusx.traceability.infrastructure.edc.notificationcontract.configuration.EdcConstants.EDC_CONTEXT;
import static org.eclipse.tractusx.traceability.infrastructure.edc.notificationcontract.configuration.EdcRestTemplateConfiguration.EDC_REST_TEMPLATE;

@Slf4j
@Component
public class EdcContractDefinitionService {

    private static final String ASSET_SELECTOR_ID = "https://w3id.org/edc/v0.0.1/ns/id";
    private static final String ASSET_SELECTOR_EQUALITY_OPERATOR = "=";
    private static final String ASSET_SELECTOR_TYPE = "CriterionDTO";

    private final RestTemplate restTemplate;
    private final EdcProperties edcProperties;

    @Autowired
    public EdcContractDefinitionService(@Qualifier(EDC_REST_TEMPLATE) RestTemplate restTemplate, EdcProperties edcProperties) {
        this.restTemplate = restTemplate;
        this.edcProperties = edcProperties;
    }

    public String createContractDefinition(String notificationAssetId, String accessPolicyId) {
        EdcContractDefinitionCriteria edcContractDefinitionCriteria = EdcContractDefinitionCriteria
                .builder()
                .type(ASSET_SELECTOR_TYPE)
                .operandLeft(ASSET_SELECTOR_ID)
                .operandRight(notificationAssetId)
                .operator(ASSET_SELECTOR_EQUALITY_OPERATOR)
                .build();

        String contractPolicyId = UUID.randomUUID().toString();

        EdcContext edcContext = new EdcContext(EDC_CONTEXT);
        EdcCreateContractDefinitionRequest createContractDefinitionRequest = EdcCreateContractDefinitionRequest.builder()
                .contractPolicyId(contractPolicyId)
                .edcContext(edcContext)
                .accessPolicyId(accessPolicyId)
                .id(accessPolicyId)
                .criteria(List.of(edcContractDefinitionCriteria))
                .build();

        final ResponseEntity<String> createContractDefinitionResponse;

        try {
            createContractDefinitionResponse = restTemplate.postForEntity(edcProperties.getNegotiationPath(), createContractDefinitionRequest, String.class);
        } catch (RestClientException e) {
            log.error("Failed to create edc contract definition for {} notification asset and {} policy definition id. Reason: ", notificationAssetId, accessPolicyId, e);

            throw new CreateEdcContractDefinitionException(e);
        }

        HttpStatusCode responseCode = createContractDefinitionResponse.getStatusCode();

        if (responseCode.value() == 409) {
            log.info("{} asset contract definition already exists in the EDC", notificationAssetId);

            throw new CreateEdcContractDefinitionException("Asset contract definition already exists in the EDC");
        }

        if (responseCode.value() == 200) {
            return contractPolicyId;
        }

        log.error("Failed to create EDC contract definition for {} notification asset id. Body: {}, status: {}", notificationAssetId, createContractDefinitionResponse.getBody(), createContractDefinitionResponse.getStatusCode());

        throw new CreateEdcAssetException("Failed to create EDC contract definition for %s notification asset id".formatted(notificationAssetId));
    }
}
