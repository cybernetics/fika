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
package com.speedment.fika.codegen.internal.java;

import com.speedment.fika.codegen.Generator;
import com.speedment.fika.codegen.Transform;
import com.speedment.fika.codegen.internal.java.trait.HasAnnotationUsageView;
import com.speedment.fika.codegen.internal.java.trait.HasCodeView;
import com.speedment.fika.codegen.internal.java.trait.HasFieldsView;
import com.speedment.fika.codegen.internal.java.trait.HasGenericsView;
import com.speedment.fika.codegen.internal.java.trait.HasJavadocView;
import com.speedment.fika.codegen.internal.java.trait.HasModifiersView;
import com.speedment.fika.codegen.internal.java.trait.HasNameView;
import com.speedment.fika.codegen.internal.java.trait.HasThrowsView;
import com.speedment.fika.codegen.internal.java.trait.HasTypeView;
import com.speedment.fika.codegen.model.Method;
import static java.util.Objects.requireNonNull;
import java.util.Optional;

/**
 * Transforms from a {@link Method} to java code.
 * 
 * @author Emil Forslund
 */
public final class MethodView implements Transform<Method, String>,
        HasNameView<Method>, 
        HasTypeView<Method>,
        HasThrowsView<Method>,
        HasGenericsView<Method>,
        HasFieldsView<Method>,
        HasJavadocView<Method>, 
        HasAnnotationUsageView<Method>,
        HasCodeView<Method>, 
        HasModifiersView<Method> {

    /**
     * {@inheritDoc}
     */
	@Override
	public Optional<String> transform(Generator gen, Method model) {
        requireNonNull(gen);
        requireNonNull(model);
        
        return Optional.of(
            renderJavadoc(gen, model) +
            renderAnnotations(gen, model) +
            renderModifiers(gen, model) +
            renderGenerics(gen, model) +
            renderType(gen, model) +
            renderName(gen, model) + "(" +
            renderFields(gen, model) + ") " +
            renderThrows(gen, model) + 
            renderCode(gen, model)
        );
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String fieldSeparator() {
        return ", ";
    }
}