/**
 *
 * Copyright (c) 2006-2016, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.fika.codegen.internal.java.value;

import com.speedment.fika.codegen.Generator;
import com.speedment.fika.codegen.Transform;
import com.speedment.fika.codegen.internal.model.value.EnumValue;
import static com.speedment.fika.codegen.internal.util.Formatting.*;
import static java.util.Objects.requireNonNull;
import java.util.Optional;

/**
 * Transforms from an {@link EnumValue} to java code.
 * 
 * @author Emil Forslund
 */
public final class EnumValueView implements Transform<EnumValue, String> {
    
    /**
     * {@inheritDoc}
     */
	@Override
	public Optional<String> transform(Generator gen, EnumValue model) {
        requireNonNull(gen);
        requireNonNull(model);
        
		return Optional.of(
			gen.on(model.getType()).orElse(EMPTY) + DOT +
			model.getValue()
		);
	}
}