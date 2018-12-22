package com.opticdev.runtime

import com.opticdev.common.graph.path.FlatWalkablePath
import com.opticdev.core.compiler.{FinderStageOutput, ParserFactoryOutput}
import com.opticdev.sdk.skills_sdk.lens.{OMComponentWithPropertyPath, OMLensRuntimeComponent}

case class RuntimeSourceListener(runtimeComponents: Map[OMComponentWithPropertyPath[OMLensRuntimeComponent], FlatWalkablePath]) {
  def nonEmpty = runtimeComponents.nonEmpty
}

object RuntimeSourceListener {
  def from(finderStageOutput: FinderStageOutput, parserFactoryOutput: ParserFactoryOutput, runtimeComponents: Vector[OMComponentWithPropertyPath[OMLensRuntimeComponent]]): RuntimeSourceListener = {

    val componentsWithPaths = runtimeComponents.map { case c => {
      val finderPath = finderStageOutput.componentFinders.collectFirst { case (f, cs) if cs.contains(c) => f }.get
      val flatFinderPath = parserFactoryOutput.finderPathFlatten(finderPath)
      (c, flatFinderPath)
      }
    }.toMap

    RuntimeSourceListener(componentsWithPaths)
  }
}