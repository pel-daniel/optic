package com.opticdev

import better.files.File
import com.opticdev.common.graph.CommonAstNode
import com.opticdev.core.sourcegear.graph.model.{ExpandedModelNode, FlatModelNode, LinkedModelNode}
import com.opticdev.sdk.skills_sdk.lens.{OMComponentWithPropertyPath, OMLensRuntimeComponent}

package object runtime {

  case class WrapRequest(targetNode: CommonAstNode, hash: String, file: File, inferSchema: Boolean, componentWithPropertyPath: OMComponentWithPropertyPath[OMLensRuntimeComponent])

  case class TempFilePatch(file: File, newContents: String, originalContents: String)
}
