package com.opticdev.common

import com.opticdev.common.spec_types.{ApiIssue, Resolution}

package object api {
  case class EndpointIdentifier(shortname: String, file: String, lineRange: Range)

  trait ApiSpecificationComponent {
    def issues: Vector[ApiIssue]
    def hasWarnings: Boolean = issues.exists(_.level == SpecError)
    def hasErrors: Boolean = issues.exists(_.level == SpecWarning)
    def hasIssues: Boolean = issues.nonEmpty

    def identifier: String
  }

  trait SpecIssue {
      val message: String
      val resolutions: Vector[Resolution]
      val level: Level
      val identifier: String
  }

  sealed trait Level
  case object SpecError extends Level
  case object SpecWarning extends Level
}