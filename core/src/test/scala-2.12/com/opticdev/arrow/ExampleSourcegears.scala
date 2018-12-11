package com.opticdev.arrow

import better.files.File
import com.opticdev.arrow.index.IndexSourceGear
import com.opticdev.common.{PackageRef, SchemaRef}
import com.opticdev.core.sourcegear.context.FlatContext
import com.opticdev.core.sourcegear.graph.ProjectGraph
import com.opticdev.core.sourcegear.project.config.ProjectFile
import com.opticdev.core.sourcegear.{LensSet, SGConstructor, SourceGear}
import com.opticdev.sdk.descriptions.transformation.Transformation
import com.opticdev.sdk.skills_sdk.schema.OMSchema
import play.api.libs.json.JsObject

import scala.concurrent.duration._
import scala.concurrent.Await

object ExampleSourcegears {

  lazy val sgWithTransformations = new {
    val schemaModel = OMSchema(SchemaRef(Some(PackageRef("optic:test", "0.1.0")), "model"), JsObject.empty)
    val schemaRoute = OMSchema(SchemaRef(Some(PackageRef("optic:test", "0.1.0")), "route"), JsObject.empty)
    val schemaForm = OMSchema(SchemaRef(Some(PackageRef("optic:test", "0.1.0")), "form"), JsObject.empty)
    val schemaFetch = OMSchema(SchemaRef(Some(PackageRef("optic:test", "0.1.0")), "fetch"), JsObject.empty)

    val transformationPackage = PackageRef("optic:test-transform")

    val sourceGear = new SourceGear {
      override val configHash: String = "test-rig"
      override val parsers = Set()
      override val lensSet = new LensSet()
      override val schemas = Set(schemaModel, schemaRoute, schemaForm, schemaFetch)
      override val transformations = Set(
        Transformation("Model -> Route", "m2r", transformationPackage,  schemaModel.schemaRef, Some(schemaRoute.schemaRef), Transformation.emptyAskSchema, Transformation.emptyAskSchema, ""),
        Transformation("Route -> Form", "r2f", transformationPackage, schemaRoute.schemaRef, Some(schemaForm.schemaRef), Transformation.emptyAskSchema, Transformation.emptyAskSchema, ""),
        Transformation("Route -> Fetch", "r2fe", transformationPackage, schemaRoute.schemaRef, Some(schemaFetch.schemaRef), Transformation.emptyAskSchema, Transformation.emptyAskSchema, "")
      )
      override val flatContext: FlatContext = FlatContext(None, Map(
        "optic:test" -> FlatContext(Some(PackageRef("optic:test", "0.1.0")), Map(
          "model" -> schemaModel,
          "route" -> schemaRoute,
          "form" -> schemaForm,
          "fetch" -> schemaFetch,
        ))
      ))
      override val connectedProjectGraphs: Set[ProjectGraph] = Set()
    }

    val knowledgeGraph = IndexSourceGear.runFor(sourceGear)

  }

  lazy val exampleProjectSG = new {

    val sourceGear = {
      val future = SGConstructor.fromProjectFile(new ProjectFile(File("test-examples/resources/example_packages/express/optic.yml")))
      Await.result(future, 10 seconds).inflate
    }

    val knowledgeGraph = IndexSourceGear.runFor(sourceGear)

  }
}
