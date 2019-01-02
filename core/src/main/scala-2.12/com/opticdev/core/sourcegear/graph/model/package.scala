package com.opticdev.core.sourcegear.graph

import com.opticdev.common.SchemaRef
import com.opticdev.core.sourcegear.graph.edges.YieldsModel
import com.opticdev.core.sourcegear.graph.enums.AstPropertyRelationship
import com.opticdev.common.graph.CommonAstNode
import com.opticdev.core.sourcegear.SGContext
import com.opticdev.core.sourcegear.annotations.dsl.SetOperationNode
import com.opticdev.core.sourcegear.annotations.{NameAnnotation, SourceAnnotation, TagAnnotation}
import com.opticdev.core.sourcegear.gears.helpers.ModelField
import com.opticdev.sdk.skills_sdk.lens.{ArrayLiteral, Literal, ObjectLiteral, Token}
import com.opticdev.sdk.skills_sdk.lens.OMLensComponent

import scala.util.Try

package object model {
  type ModelAstMapping = Map[ModelKey, Set[AstMapping]]

  type ModelAstPair = (YieldsModel, CommonAstNode)

  sealed trait ModelKey

  case class Path(path: Seq[String]) extends ModelKey {
    override def toString: String = path.mkString(".")
  }

  object Path {
    def fromString(string: String) = Path(string.split("\\.").toSeq)
  }

  sealed trait AstMapping {
    val relationship : AstPropertyRelationship.Value
    def supportsComponentMapping(component: OMLensComponent) = {
      component.`type` match {
        case Token if relationship == AstPropertyRelationship.Token => true
        case Literal if relationship == AstPropertyRelationship.Literal => true
        case ObjectLiteral if relationship == AstPropertyRelationship.ObjectLiteral => true
        case ArrayLiteral if relationship == AstPropertyRelationship.ArrayLiteral => true
        case _ => false
      }
    }
  }

  case class NodeMapping(node: CommonAstNode, relationship : AstPropertyRelationship.Value) extends AstMapping
  case class ModelMapping(model: ModelNode) extends AstMapping {override val relationship = AstPropertyRelationship.Model}
  case class ModelVectorMapping(models: Vector[ModelNode]) extends AstMapping {override val relationship = AstPropertyRelationship.Model}
  case class ContainerMapping(containerRoot: CommonAstNode) extends AstMapping {override val relationship = AstPropertyRelationship.NoRelationship}
  case object NoMapping extends AstMapping {override val relationship = AstPropertyRelationship.NoRelationship}


  trait HasAnnotations {
    def schemaId : SchemaRef
    private var _annotations: ModelAnnotations = ModelAnnotations.empty
    def attachAnnotations(annotations: ModelAnnotations) = {
      _annotations = annotations.withSchema(schemaId)
    }
    def annotations: ModelAnnotations = _annotations

    def valueOverrides()(implicit sourceGearContext: SGContext): Vector[ModelField] = {
      val assignments = annotations.set.flatMap(_.assignments)
      import GraphImplicits._
      assignments.map {
        case i if i.isRef => Try {
          val objectValue = sourceGearContext.project.projectGraph
            .objectByName(i.ref.get)
            .map(_._2.apply(sourceGearContext))

          ModelField(i.keyPath, objectValue.get)
        }
        case i => Try(ModelField(i.keyPath, i.value.get))
      }.collect({case i if i.isSuccess => i.get})
    }
  }
}
