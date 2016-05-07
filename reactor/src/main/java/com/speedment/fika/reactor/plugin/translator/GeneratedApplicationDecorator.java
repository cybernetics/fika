package com.speedment.fika.reactor.plugin.translator;

import com.speedment.code.JavaClassTranslator;
import static com.speedment.code.Translator.Phase.POST_MAKE;
import com.speedment.code.TranslatorDecorator;
import com.speedment.code.TranslatorSupport;
import com.speedment.codegen.model.Class;
import com.speedment.codegen.model.Field;
import com.speedment.codegen.model.Generic;
import static com.speedment.codegen.model.Generic.BoundType.EXTENDS;
import com.speedment.codegen.model.Import;
import com.speedment.codegen.model.Method;
import com.speedment.codegen.model.Type;
import com.speedment.config.db.Column;
import com.speedment.config.db.PrimaryKeyColumn;
import com.speedment.config.db.Project;
import com.speedment.config.db.Table;
import com.speedment.exception.SpeedmentException;
import com.speedment.field.ComparableField;
import com.speedment.fika.reactor.MaterializedView;
import com.speedment.fika.reactor.Reactor;
import static com.speedment.internal.codegen.model.constant.DefaultAnnotationUsage.OVERRIDE;
import com.speedment.internal.codegen.model.constant.DefaultType;
import static com.speedment.internal.codegen.model.constant.DefaultType.VOID;
import static com.speedment.internal.codegen.model.constant.DefaultType.WILDCARD;
import static com.speedment.internal.codegen.util.Formatting.shortName;
import com.speedment.internal.util.document.DocumentDbUtil;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author Emil Forslund
 * @since  1.1.0
 */
public final class GeneratedApplicationDecorator implements TranslatorDecorator<Project, Class> {

    @Override
    public void apply(JavaClassTranslator<Project, Class> translator) {
        translator.onMake((file, builder) -> {
            System.out.println("Decorating...");
            
            // This should be done once:
            builder.forEveryProject(POST_MAKE, (clazz, project) -> {
                System.out.println("...Decorating project " + project.getName() + "...");
                
                // Generate new field '_reactors'
                clazz.add(Field.of("_reactors", 
                        DefaultType.list(Type.of(Reactor.class)
                            .add(Generic.of().add(WILDCARD))
                            .add(Generic.of().add(WILDCARD))
                        )
                    ).private_().final_()
                );
                
                // Set '_reactors' in constructor.
                file.add(Import.of(Type.of(LinkedList.class)));
                clazz.getConstructors().forEach(constr -> {
                    constr.add("this._reactors = new LinkedList<>();");
                });
                
                // Generate the 'onStop' method
                getOrCreate(clazz, "onStop").add("_reactors.forEach(Reactor::stop);");
                
                // Generate the 'newReactor' method
                clazz.add(Method.of("newReactor", VOID)
                    .private_()
                    .add(Generic.of().setLowerBound("E"))
                    .add(Generic.of().setLowerBound("T")
                        .setBoundType(EXTENDS)
                        .add(Type.of(Comparable.class)
                            .add(Generic.of().setLowerBound("T"))
                        )
                    )
                    .add(Field.of("entityType", Type.of(java.lang.Class.class)
                        .add(Generic.of().setLowerBound("E"))
                    ))
                    .add(Field.of("field", Type.of(ComparableField.class)
                        .add(Generic.of().setLowerBound("E"))
                        .add(Generic.of().add(WILDCARD))
                        .add(Generic.of().setLowerBound("T"))
                    ))
                    .add(Field.of("view", Type.of(MaterializedView.class)
                        .add(Generic.of().setLowerBound("E"))
                        .add(Generic.of().setLowerBound("T"))
                    ))
                    .call(() -> file.add(Import.of(Type.of(Consumer.class))))
                    .add(
                        "final Consumer<List<E>> con = view;",
                        "_reactors.add(Reactor.builder(speedment.managerOf(entityType), field)",
                        "    .withListener(con)",
                        "    .build());"
                    )
                );
                
                // This should be done for every view:
                DocumentDbUtil.traverseOver(project, Table.class).forEach(table -> {
                    final TranslatorSupport<Table> tableSupport = 
                        new TranslatorSupport<>(translator.getSupport().speedment(), table);
                    
                    System.out.println("...Decorating table " + table.getName() + "...");
                
                    final String tableTypeName = tableSupport.typeName(table);

                    // Generate fields for every materialized view.
                    final String viewName = tableSupport.variableName() + "View";
                    final Type viewType = Type.of(tableSupport.basePackageName() + "." + 
                        tableTypeName + "View");

                    clazz.add(Field.of(viewName, viewType)
                        .protected_().final_()
                    );

                    // Set the views in the constructor
                    clazz.getConstructors().forEach(constr -> {
                        constr.add("this." + viewName + " = new " + shortName(viewType.getName()) + "Impl();");
                    });
                    
                    file.add(Import.of(Type.of(tableSupport.basePackageName() + "." + 
                        tableTypeName + "ViewImpl")));

                    // Find the name of the primary key field.
                    final String pkName = table.primaryKeyColumns()
                        .map(PrimaryKeyColumn::findColumn)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(Column::getJavaName)
                        .map(translator.getNamer()::javaStaticFieldName)
                        .findFirst()
                        .orElseThrow(() -> new SpeedmentException(
                            "Error generating code. Table '" + table.getName() + 
                            "' does not appear to have a valid primary key."
                        ));

                    // Call 'newReactor' in the 'onLoad'-method for every view
                    getOrCreate(clazz, "onLoad")
                        .add("newReactor(" + tableTypeName + ".class, " +
                            tableTypeName + "." + pkName + ", " + viewName + ");"
                        );
                    
                    file.add(Import.of(Type.of(tableSupport.basePackageName() + "." + 
                        tableTypeName)));
                });
            });
        });
    }

    private static Method getOrCreate(Class clazz, String methodName) {
        return clazz.getMethods().stream()
            .filter(m -> m.getName().equals(methodName))
            .findAny().orElseGet(() -> {
                final Method method = Method.of(methodName, VOID)
                    .public_()
                    .add(OVERRIDE)
                    .add("super." + methodName + "();");

                clazz.add(method);

                return method;
            });
    }
}
