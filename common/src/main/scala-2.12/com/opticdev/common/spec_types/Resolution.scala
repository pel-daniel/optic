package com.opticdev.common.spec_types

trait Resolution {
  def title: String
  def message: String
  def docLink: Option[String] = None
}

case object AddTestCmd extends Resolution {
  override def title: String = "Optic doesn't know how to run your tests"
  override def message: String =
    """
      |Optic is able to learn a lot about your project from watching your tests run. Just add an entry to your 'optic.yml':
      |```yaml
      |testcmd: "npm run test"
      |```
    """.stripMargin
}