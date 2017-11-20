package com.opticdev.core.compiler.helpers

import com.opticdev.opm.DependencyTree
import com.opticdev.opm.context.PackageContext
import com.opticdev.sdk.descriptions.{Schema, SchemaId}

object SchemaIdImplicits {
  implicit class SchemaIdResolver(schemaId: SchemaId) {
    def resolve()(implicit packageContext: PackageContext) : Option[Schema] = {
      val resolvedOption = packageContext(schemaId.id)
      if (resolvedOption.isDefined && resolvedOption.get.isInstanceOf[Schema]) resolvedOption.asInstanceOf[Option[Schema]]
      else None
    }
  }
}