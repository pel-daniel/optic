package com.opticdev.core.sourcegear.accumulate

import com.opticdev.common.SchemaRef
import com.opticdev.common.graph.path.PropertyPathWalker
import com.opticdev.common.graph.{AstGraph, CommonAstNode}
import com.opticdev.core.sourcegear.SGContext
import com.opticdev.core.sourcegear.context.SDKObjectsResolvedImplicits._
import com.opticdev.core.sourcegear.gears.helpers.{LocationEvaluation, ModelField}
import com.opticdev.core.sourcegear.graph.GraphImplicits._
import com.opticdev.core.sourcegear.graph.model.{BaseModelNode, LinkedModelNode, ModelNode, ModelVectorMapping}
import com.opticdev.runtime.RuntimeValueFragment
import com.opticdev.sdk.skills_sdk.LensRef
import com.opticdev.sdk.skills_sdk.lens.{OMComponentWithPropertyPath, OMLensRuntimeComponent, OMLensSchemaComponent}
import play.api.libs.json._

import scala.util.Try

case class RuntimeFieldListener(runtimeComponent: OMComponentWithPropertyPath[OMLensRuntimeComponent], mapToSchema: SchemaRef, lensRef: LensRef) extends Listener {

  override val schema = None
  override def collect(implicit astGraph: AstGraph, modelNode: BaseModelNode, sourceGearContext: SGContext): Try[ModelField] = Try {

    val validFragments = sourceGearContext.project.runtimeFragments.filter {
      case RuntimeValueFragment(modelHash, runtimeComponentId, value) => modelHash == modelNode.hash && runtimeComponentId == runtimeComponent.component.identifier
    }

    require(validFragments.nonEmpty, "No runtime fragment found for model.")

    /* for now we aren't going to handle conflicts from multiple hits.
    *    for infer schema types we will eventually merge schemas
    *    for objects we'll show a conflict or just pick the first result.
    * */
    val valueFragment = validFragments.head
    ModelField(runtimeComponent.propertyPath, valueFragment.value)
  }
}
