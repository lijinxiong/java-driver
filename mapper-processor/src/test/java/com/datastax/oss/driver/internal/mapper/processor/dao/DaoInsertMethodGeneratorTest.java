/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.mapper.processor.dao;

import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.lang.model.element.Modifier;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class DaoInsertMethodGeneratorTest extends DaoMethodGeneratorTest {

  @Test
  @Override
  @UseDataProvider("invalidSignatures")
  public void should_fail_with_expected_error(String expectedError, MethodSpec method) {
    super.should_fail_with_expected_error(expectedError, method);
  }

  @DataProvider
  public static Object[][] invalidSignatures() {
    return new Object[][] {
      {
        "Wrong number of parameters: Insert methods must have at least one",
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .build(),
      },
      {
        "Invalid parameter type: Insert methods must take the entity to insert as the first parameter",
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(String.class, "a").build())
            .build(),
      },
      {
        "Invalid return type: Insert methods must return either void or the entity class "
            + "(possibly wrapped in a CompletionStage/CompletableFuture)",
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(TypeName.INT)
            .build(),
      },
    };
  }

  @Test
  @Override
  @UseDataProvider("validSignatures")
  public void should_succeed_without_warnings(MethodSpec method) {
    super.should_succeed_without_warnings(method);
  }

  @DataProvider
  public static Object[][] validSignatures() {
    return new Object[][] {
      // Returns void, or a future thereof:
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .build()
      },
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(ParameterizedTypeName.get(CompletionStage.class, Void.class))
            .build()
      },
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(ParameterizedTypeName.get(CompletableFuture.class, Void.class))
            .build()
      },
      // Returns the entity class, or a future thereof:
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(ENTITY_CLASS_NAME)
            .build()
      },
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(
                ParameterizedTypeName.get(ClassName.get(CompletionStage.class), ENTITY_CLASS_NAME))
            .build()
      },
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get(CompletableFuture.class), ENTITY_CLASS_NAME))
            .build()
      },
      // Returns an optional of the entity class, or a future thereof:
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), ENTITY_CLASS_NAME))
            .build()
      },
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get(CompletionStage.class),
                    ParameterizedTypeName.get(ClassName.get(Optional.class), ENTITY_CLASS_NAME)))
            .build()
      },
      // Extra parameters in addition to the entity (to bind into the request):
      {
        MethodSpec.methodBuilder("insert")
            .addAnnotation(Insert.class)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .addParameter(ParameterSpec.builder(String.class, "param1").build())
            .addParameter(ParameterSpec.builder(Integer.class, "param2").build())
            .build()
      },
    };
  }
}