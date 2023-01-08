/*
 * Copyright (©) 2022 Dr. Ing. h.c. F. Porsche AG

 * This code is protected by intellectual property rights.
 *  Dr. Ing. h.c. F. Porsche AG owns exclusive rights of use.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */
package de.porsche.oso.bdi.spring.support;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Configuration
public @interface LocalTestConfiguration {
}