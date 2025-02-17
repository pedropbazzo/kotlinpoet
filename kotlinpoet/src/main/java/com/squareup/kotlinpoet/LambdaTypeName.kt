/*
 * Copyright (C) 2021 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.kotlinpoet

import kotlin.reflect.KClass

public class LambdaTypeName private constructor(
  public val receiver: TypeName? = null,
  parameters: List<ParameterSpec> = emptyList(),
  public val returnType: TypeName = UNIT,
  nullable: Boolean = false,
  public val isSuspending: Boolean = false,
  annotations: List<AnnotationSpec> = emptyList(),
  tags: Map<KClass<*>, Any> = emptyMap()
) : TypeName(nullable, annotations, TagMap(tags)) {
  public val parameters: List<ParameterSpec> = parameters.toImmutableList()

  init {
    for (param in parameters) {
      require(param.annotations.isEmpty()) { "Parameters with annotations are not allowed" }
      require(param.modifiers.isEmpty()) { "Parameters with modifiers are not allowed" }
      require(param.defaultValue == null) { "Parameters with default values are not allowed" }
    }
  }

  override fun copy(
    nullable: Boolean,
    annotations: List<AnnotationSpec>,
    tags: Map<KClass<*>, Any>
  ): LambdaTypeName {
    return copy(nullable, annotations, this.isSuspending, tags)
  }

  public fun copy(
    nullable: Boolean = this.isNullable,
    annotations: List<AnnotationSpec> = this.annotations.toList(),
    suspending: Boolean = this.isSuspending,
    tags: Map<KClass<*>, Any> = this.tags.toMap()
  ): LambdaTypeName {
    return LambdaTypeName(receiver, parameters, returnType, nullable, suspending, annotations, tags)
  }

  override fun emit(out: CodeWriter): CodeWriter {
    if (isNullable) {
      out.emit("(")
    }

    if (isSuspending) {
      out.emit("suspend ")
    }

    receiver?.let {
      if (it.isAnnotated) {
        out.emitCode("(%T).", it)
      } else {
        out.emitCode("%T.", it)
      }
    }

    parameters.emit(out)
    out.emitCode(if (returnType is LambdaTypeName) " -> (%T)" else " -> %T", returnType)

    if (isNullable) {
      out.emit(")")
    }
    return out
  }

  public companion object {
    /** Returns a lambda type with `returnType` and parameters listed in `parameters`. */
    @JvmStatic public fun get(
      receiver: TypeName? = null,
      parameters: List<ParameterSpec> = emptyList(),
      returnType: TypeName
    ): LambdaTypeName = LambdaTypeName(receiver, parameters, returnType)

    /** Returns a lambda type with `returnType` and parameters listed in `parameters`. */
    @JvmStatic public fun get(
      receiver: TypeName? = null,
      vararg parameters: TypeName = emptyArray(),
      returnType: TypeName
    ): LambdaTypeName {
      return LambdaTypeName(
        receiver,
        parameters.toList().map { ParameterSpec.unnamed(it) },
        returnType
      )
    }

    /** Returns a lambda type with `returnType` and parameters listed in `parameters`. */
    @JvmStatic public fun get(
      receiver: TypeName? = null,
      vararg parameters: ParameterSpec = emptyArray(),
      returnType: TypeName
    ): LambdaTypeName = LambdaTypeName(receiver, parameters.toList(), returnType)
  }
}
