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

import com.datastax.oss.driver.api.core.MappedAsyncPagingIterable;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.lang.model.element.Modifier;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class DaoQueryMethodGeneratorTest extends DaoMethodGeneratorTest {

  @Test
  @Override
  @UseDataProvider("invalidSignatures")
  public void should_fail_with_expected_error(String expectedError, MethodSpec method) {
    super.should_fail_with_expected_error(expectedError, method);
  }

  @DataProvider
  public static Object[][] invalidSignatures() {
    // Not many error cases to cover, the return type/parameters are pretty open
    return new Object[][] {
      {
        "Invalid return type: Query methods must return void, boolean, Integer, Row, an entity "
            + "class, a result set, a mapped iterable, or a CompletionStage/CompletableFuture "
            + "of any of the above",
        MethodSpec.methodBuilder("select")
            .addAnnotation(
                AnnotationSpec.builder(Query.class)
                    .addMember("value", "$S", "SELECT * FROM whatever")
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(UUID.class)
            .build(),
      },
    };
  }

  @Test
  @UseDataProvider("validReturnTypes")
  public void should_succeed_without_warnings(TypeName returnType) {
    super.should_succeed_without_warnings(
        MethodSpec.methodBuilder("select")
            .addAnnotation(
                AnnotationSpec.builder(Query.class)
                    .addMember("value", "$S", "SELECT * FROM whatever")
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(returnType)
            .build());
  }

  @DataProvider
  public static Object[] validReturnTypes() {
    return new Object[] {
      TypeName.VOID,
      ParameterizedTypeName.get(CompletionStage.class, Void.class),
      TypeName.BOOLEAN,
      TypeName.LONG,
      ClassName.get(Boolean.class),
      ParameterizedTypeName.get(CompletableFuture.class, Boolean.class),
      ClassName.get(Long.class),
      ParameterizedTypeName.get(CompletionStage.class, Long.class),
      ClassName.get(Row.class),
      ParameterizedTypeName.get(CompletableFuture.class, Row.class),
      ENTITY_CLASS_NAME,
      ParameterizedTypeName.get(ClassName.get(Optional.class), ENTITY_CLASS_NAME),
      ParameterizedTypeName.get(ClassName.get(CompletionStage.class), ENTITY_CLASS_NAME),
      ClassName.get(ResultSet.class),
      ParameterizedTypeName.get(CompletionStage.class, AsyncResultSet.class),
      ParameterizedTypeName.get(ClassName.get(PagingIterable.class), ENTITY_CLASS_NAME),
      ParameterizedTypeName.get(
          ClassName.get(CompletionStage.class),
          ParameterizedTypeName.get(ClassName.get(Optional.class), ENTITY_CLASS_NAME)),
      ParameterizedTypeName.get(
          ClassName.get(CompletionStage.class),
          ParameterizedTypeName.get(
              ClassName.get(MappedAsyncPagingIterable.class), ENTITY_CLASS_NAME)),
    };
  }
}