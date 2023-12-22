/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.traceability.assets.domain.importpoc;

import java.time.OffsetDateTime;
import java.util.List;

public record SingleLevelBomAsPlannedRequest(String catenaXId, List<ChildItem> childItems) {

    public record ChildItem(
            ValidityPeriod validityPeriod,
            String catenaXId,
            Quantity quantity,
            String businessPartner,
            String createdOn,
            String lastModifiedOn
    ) {
    }

    public record ValidityPeriod(
            OffsetDateTime validFrom,
            OffsetDateTime validTo
    ) {
    }

    public record Quantity(
            double quantityNumber,
            String measurementUnit
    ) {
    }
}

