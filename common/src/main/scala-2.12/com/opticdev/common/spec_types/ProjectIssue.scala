package com.opticdev.common.spec_types

import com.opticdev.common.api.{Level, SpecIssue, SpecWarning}

trait ProjectIssue extends SpecIssue

case object NoTestCommand extends ProjectIssue {
  override val message: String = "No 'testcmd' defined in 'optic.yml'. This needs to be configured for Optic to learn from running your tests."
  override val resolutions: Vector[Resolution] = Vector(AddTestCmd)
  override val level: Level = SpecWarning
  override val identifier: String = null
}