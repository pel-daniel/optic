package com.opticdev.core.sourcegear

import com.opticdev.common.graph.AstGraph

package object snapshot {
  case class AstGraphAndContent(astGraph: AstGraph, fileContents: String)
}
