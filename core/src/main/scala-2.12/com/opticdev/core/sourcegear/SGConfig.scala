package com.opticdev.core.sourcegear

import com.opticdev.SupportedParsers
import com.opticdev.common.{ParserRef, SchemaRef}
import com.opticdev.core.sourcegear.context.FlatContext
import com.opticdev.core.sourcegear.graph.objects.ObjectNode
import com.opticdev.core.sourcegear.graph.{ProjectGraph, SerializeProjectGraph}
import com.opticdev.core.sourcegear.project.config.options.ConstantObject

import scala.util.hashing.MurmurHash3
import com.opticdev.core.sourcegear.serialization.PickleImplicits._
import com.opticdev.opm.PackageManager
import com.opticdev.opm.context.TreeContext
import com.opticdev.opm.storage.ParserStorage
import com.opticdev.parsers.SourceParserManager
import com.opticdev.sdk.descriptions.transformation.Transformation
import com.opticdev.sdk.skills_sdk.OMParser
import com.opticdev.sdk.skills_sdk.schema.{OMSchema, OMSchemaColdStorage}
import play.api.libs.json.{JsObject, JsString, Json}

case class SGConfig(hashInt: Int,
                    _flatContext: FlatContext,
                    parserIds : Set[ParserRef],
                    compiledLenses : Set[SGExportableLens],
                    schemas : Set[OMSchemaColdStorage],
                    transformations: Set[Transformation],
                    connectedProjects: Set[String],
                    constantObjects: Vector[ObjectNode]) {

  def hashString : String = Integer.toHexString(hashInt)

  lazy val inflatedSchemas = schemas.map(i=> {
    val json = Json.parse(i.data).as[JsObject]
    val schemaRef = SchemaRef.fromString(i.schemaRefAsString).get
    OMParser.parseSchema(Json.parse(i.data).as[JsObject])(schemaRef).get
  })

  def inflate : SourceGear = {

    val inflatedTransformations = transformations

    val parserCollections = PackageManager.collectParsers(parserIds.toSeq:_*)

    if (!parserCollections.foundAll) throw new Error("Unable to resolve parsers "+ parserCollections.notFound.map(_.full))

    val connectedGraphs = connectedProjects.map(projectName=> {
      SerializeProjectGraph.loadProjectGraphFor(projectName)
    }).collect{case n if n.isSuccess => n.get}

    val simulatedGraph = {
      SerializeProjectGraph.generateFromConstants(constantObjects, "constants_")
    }

    //from parsers
    val (parserGenerators, parserAbstractions) = {
      val allSkills = parserCollections.found.map(SupportedParsers.getSkills)
      val generators = allSkills.flatMap(_.generators)
      val abstractions = allSkills.flatMap(_.abstractions)
      (generators, abstractions)
    }

    new SourceGear {
      override val parsers = parserCollections.found
      override val lensSet = new LensSet((compiledLenses ++ parserGenerators).toSeq: _*)
      override val schemas = inflatedSchemas ++ parserAbstractions
      override val transformations = inflatedTransformations
      override val flatContext: FlatContext = _flatContext
      override val connectedProjectGraphs: Set[ProjectGraph] = connectedGraphs + simulatedGraph
      override val configHash: String = hashString
    }
  }

}