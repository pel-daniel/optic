package com.opticdev.sdk.skills_sdk.lens

import com.opticdev.common.SchemaRef
import com.opticdev.sdk.rules.{Any, Rule}
import com.opticdev.sdk.descriptions.enums.FinderEnums.StringEnums
import com.opticdev.sdk.descriptions.{ChildrenRule, Location, RawRule}
import com.opticdev.sdk.descriptions.enums.{BasicComponentType, LocationEnums}
import com.opticdev.sdk.skills_sdk.OMRange

trait OMLensComponent {
  def rules: Vector[Rule]
  def `type`: OMLensComponentType
  def capabilities: OpticCapabilities

  def canGenerate: Boolean = capabilities.generate
  def canMutate: Boolean = capabilities.mutate
  def canParse: Boolean = capabilities.parse

}

case class OMLensCodeComponent(`type`: OMLensComponentType, at: OMFinder) extends OMLensComponent {
    override def rules: Vector[Rule] = Vector(RawRule(at, "ANY"), ChildrenRule(at, Any))
  override def capabilities: OpticCapabilities = OpticCapabilities(generate = true, mutate = true, parse = true)
}

sealed trait OMLensComponentType
case object Token extends OMLensComponentType
case object Literal extends OMLensComponentType
case object ObjectLiteral extends OMLensComponentType
case object ArrayLiteral extends OMLensComponentType
case object NotSupported extends OMLensComponentType

sealed trait OMFinder {
  def toDebugString : String
}

case class OMLensNodeFinder(astType: String, range: OMRange) extends OMFinder {
  override def toDebugString: String = s"${astType} at [${range.start}, ${range.end}]"
}

//for debug only
case class OMStringFinder(rule: StringEnums, string: String, occurrence: Int = 0) extends OMFinder {
  def toDebugString : String = s"${rule.toDebugString} ${string} ${if (occurrence!=0) s"[${occurrence}]" else ""}"
}

//for debug only
case class OMRangeFinder(start: Int, end: Int) extends OMFinder {
  def toDebugString : String = s"Node at [${start}, ${end}]"
}