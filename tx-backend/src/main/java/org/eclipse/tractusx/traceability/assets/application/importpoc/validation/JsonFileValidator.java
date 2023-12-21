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
package org.eclipse.tractusx.traceability.assets.application.importpoc.validation;

import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;
import net.jimblackler.jsonschemafriend.Validator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonFileValidator {

    private final String VALIDATION_SCHEMA_PATH = "/validation/schema_V1.json";

    public List<String> isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return List.of();
        }

        try {
            SchemaStore schemaStore = new SchemaStore();
            final List<String> errors = new ArrayList<>();
            Validator validator = new Validator();

            URL url = getClass().getResource(VALIDATION_SCHEMA_PATH);

            Schema schema = schemaStore.loadSchema(url);
            File tempFile = File.createTempFile("temp", null);
            Files.copy(file.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            validator.validate(schema, tempFile, validationError -> errors.add(validationError.getMessage()));

            Files.delete(Path.of(tempFile.getPath()));
            if (!errors.isEmpty()) {
                return errors;
            }

        } catch (GenerationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return List.of();
    }
}
