package com.opticdev.sdk.skills_sdk.lens

import com.opticdev.common.{PackageRef, SchemaRef}
import com.opticdev.sdk.rules.ChildrenRuleTypeEnum
import com.opticdev.sdk.descriptions.PackageExportable
import com.opticdev.sdk.skills_sdk.compilerInputs.subcontainers.OMSubContainer
import com.opticdev.sdk.skills_sdk.compilerInputs.variables.OMVariable
import com.opticdev.sdk.skills_sdk.{LensRef, OMChildrenRuleType, OMSnippet}
import com.opticdev.sdk.skills_sdk.schema.OMSchema
import play.api.libs.json.JsObject

import scala.collection.immutable

case class OMLens(name: Option[String],
                  id: String,
                  snippet: OMSnippet,
                  value: Map[String, OMLensComponent],
                  variables: Map[String, OMLensVariableScopeEnum] = Map(),
                  containers: Map[String, OMChildrenRuleType] = Map(),
                  schema: Either[SchemaRef, OMSchema],
                  initialValue: JsObject = JsObject.empty,
                  language: String = "es7", //backwards compatibility
                  packageRef: PackageRef,
                  priority: Int = 1,
                  internal: Boolean = false
                 ) extends PackageExportable {

  require({
    val runtimeComponents = runtimeFieldComponentsCompilerInput
    val distinctExpressions = runtimeComponents.map(_.component.tokenAt).distinct
    runtimeComponents.size == distinctExpressions.size
  }, "Invalid OMLens. Runtime components must rely on unique expressions")

  def variablesCompilerInput: Vector[OMVariable] = variables.map(i=> OMVariable(i._1, i._2)).toVector

  def subcontainerCompilerInputs: Vector[OMSubContainer] = containers.map(i=> {
    val schemaComponents: Vector[OMComponentWithPropertyPath[OMLensSchemaComponent]] = value.collect {
      case (k: String, v: OMLensSchemaComponent) => OMComponentWithPropertyPath[OMLensSchemaComponent](Seq(k), v)
    }.toVector

    OMSubContainer(i._1, i._2, schemaComponents)
  }).toVector

  def schemaRef: SchemaRef = {
    if (schema.isLeft) {
      schema.left.get
    } else {
      schema.right.get.schemaRef
    }
  }

  def lensRef: LensRef = LensRef(Some(packageRef), id)

  def valueComponentsCompilerInput: Vector[OMComponentWithPropertyPath[OMLensComponent]] = {
    value.flatMap({
      case i if i._2.isInstanceOf[OMLensComputedFieldComponent] =>
        (i._2.asInstanceOf[OMLensComputedFieldComponent].codeComponents :+ OMComponentWithPropertyPath(Seq(i._1), i._2))
          .asInstanceOf[Vector[OMComponentWithPropertyPath[OMLensComponent]]]
      case i => Vector(OMComponentWithPropertyPath(Seq(i._1), i._2))
    }).toVector
  }

  def valueSchemaComponentsCompilerInput: Vector[OMComponentWithPropertyPath[OMLensSchemaComponent]] = {
    value.collect {
      case (k:String, v: OMLensSchemaComponent) => {
        OMComponentWithPropertyPath(Seq(k), v)
      }
    }.toVector
  }

  def assignmentComponentsCompilerInput: Vector[OMComponentWithPropertyPath[OMLensAssignmentComponent]] = {
    value.collect {
      case (k:String, v: OMLensAssignmentComponent) => {
        OMComponentWithPropertyPath(Seq(k), v)
      }
    }.toVector
  }

  def computedFieldComponentsCompilerInput: Vector[OMComponentWithPropertyPath[OMLensComputedFieldComponent]] = {
    value.collect {
      case (k:String, v: OMLensComputedFieldComponent) => {
        OMComponentWithPropertyPath(Seq(k), v)
      }
    }.toVector
  }

  def runtimeFieldComponentsCompilerInput: Vector[OMComponentWithPropertyPath[OMLensRuntimeComponent]] = {
    value.collect {
      case (k:String, v: OMLensRuntimeComponent) => {
        OMComponentWithPropertyPath(Seq(k), v)
      }
    }.toVector
  }

}


