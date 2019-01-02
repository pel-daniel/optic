package com.opticdev.sdk.skills_sdk.lens

import com.opticdev.sdk.descriptions.{ChildrenRule, RawRule}
import com.opticdev.sdk.rules.{Any, Rule}
import play.api.libs.json.{JsObject, JsValue}

import scala.util.{Random, Try}


case class OMLensRuntimeComponent(tokenAt: OMFinder,
                                  processAs: RuntimeAnalysisTarget,
                                  options: JsObject = JsObject.empty,
                                  identifier: String = Random.alphanumeric.take(9).mkString)

  extends OMLensComponent {
  override def rules: Vector[Rule] = Vector(RawRule(tokenAt, "ANY"), ChildrenRule(tokenAt, Any))
  override def `type`: OMLensComponentType = NotSupported
  override def capabilities: OpticCapabilities = OpticCapabilities(generate = false, mutate = false, parse = true)
}


case class CallExpressionLocator(expression: OMFinder, arg: Int)

sealed trait RuntimeAnalysisTarget {def evaluate(input: JsValue): JsValue }
case object SchemaDef extends RuntimeAnalysisTarget {
  override def evaluate(input: JsValue): JsValue = input //@todo add functionality here (currently handled by helpers)
}

case object ObjectDef extends RuntimeAnalysisTarget {
  override def evaluate(input: JsValue): JsValue = input
}