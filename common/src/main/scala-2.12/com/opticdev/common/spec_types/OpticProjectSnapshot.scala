package com.opticdev.common.spec_types

case class OpticProjectSnapshot(apiSpec: OpticAPISpec,
                           requestsSpec: Vector[String],
                           projectIssues: Vector[ProjectIssue],
                           name: String)
