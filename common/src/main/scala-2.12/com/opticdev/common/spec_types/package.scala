package com.opticdev.common

import com.opticdev.common.spec_types.{ApiIssue, IncompleteTestCoverage}
package object api {

  trait ApiSpecificationComponent {
    def issues: Vector[ApiIssue]
    def hasWarnings: Boolean = issues.exists(_.level == SpecError)
    def hasErrors: Boolean = issues.exists(_.level == SpecWarning)
    def hasIssues: Boolean = issues.nonEmpty

    def identifier: String
  }

  trait SpecIssue {
      val title: String
      val message: String
      val doctag: String
      val level: Level
      val identifier: String
  }

  sealed trait Level
  case object SpecError extends Level
  case object SpecWarning extends Level

}