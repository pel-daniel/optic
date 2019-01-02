package com.opticdev.common.spec_types

import com.opticdev.common.api.{Level, SpecIssue, SpecWarning}

trait ProjectIssue extends SpecIssue

case object NoTestCommand extends ProjectIssue {
  override val title: String = "Test Command not Configured"
  override val message: String = "No 'testcmd' defined in 'optic.yml'. This needs to be configured for Optic to learn from running your tests."
  override val level: Level = SpecWarning
  override val identifier: String = null
  override val doctag: String = "runtime-analysis"
}


case class IncompleteTestCoverage(apiType: String, file: String, lineRange: Range, contents: String) extends ProjectIssue {
  override val title: String = "Incomplete Test Coverage"
  override val message: String = s"${apiType} not reached at ${file}:${lineRange.start}\n--------\n${contents}\n--------\n"
  override val level: Level = SpecWarning
  override val identifier: String = null
  override val doctag: String = "incomplete-test-coverage"
}