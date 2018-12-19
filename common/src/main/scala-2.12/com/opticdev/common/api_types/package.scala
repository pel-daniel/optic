package com.opticdev.common

import com.opticdev.common.api_types.{APIError, APIWarning, ApiIssue}

package object api {
  case class EndpointIdentifier(shortname: String, file: String, lineRange: Range)

  trait ApiSpecificationComponent {
    def issues: Vector[ApiIssue]
    def hasWarnings: Boolean = issues.exists(_.level == APIWarning)
    def hasErrors: Boolean = issues.exists(_.level == APIError)
    def hasIssues: Boolean = issues.nonEmpty

    def identifier: String
  }
}