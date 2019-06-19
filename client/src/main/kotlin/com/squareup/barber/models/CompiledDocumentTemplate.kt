package com.squareup.barber.models

import com.github.mustachejava.Mustache
import kotlin.reflect.KClass

/**
 * An intermediary data class used in processing [DocumentTemplate] that permits for null values in
 *  fields and pre-compilation of Mustache templates in fields.
 * This allows for a [CompiledDocumentTemplate].fields to contain the same keys as the target
 *  [Document] (even for [Document] keys that are nullable) and improve Mustache execution runtime.
 */
data class CompiledDocumentTemplate(
  val fields: Map<String, Mustache?>,
  val source: KClass<out DocumentData>,
  val targets: Set<KClass<out Document>>,
  val locale: Locale
)