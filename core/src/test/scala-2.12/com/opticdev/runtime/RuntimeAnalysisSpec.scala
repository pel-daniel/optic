package com.opticdev.runtime

import better.files.File
import play.api.libs.json.JsObject

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._

class RuntimeAnalysisSpec extends RuntimeTestRig {
  val file = File("test-examples/resources/tmp/example_source/runtime/Example.js")
  lazy val f = fixture(file.pathAsString)
  lazy val wrappers = RuntimeManager.stageSourceWrappers(f.snapshot)
  lazy val fileChanges = RuntimeManager.collectFileChanges(wrappers.collect {case i if i.isSuccess => i.get}, f.snapshot)

  it("can find all AST Nodes that need to be wrapper") {
    assert(wrappers.collect{case i if i.isSuccess => i.get}.size == 2)
  }

  it("determines valid file patches to add wrappers") {
    assert(fileChanges.size == 1)

    val parserRandomInstanceId = f.snapshot.sourceGear.parsers.head.runtimeHelper.get.calleeToken
    assert(fileChanges.head.newContents == s"const ${parserRandomInstanceId} = require('optic-helper').addRuntimeFragment\n\n\nconst handler = (req, res) => {\n    const hello = {success: true, value: {firstName: 'a', lastName: 'c'}}\n    res.send(${parserRandomInstanceId}('168a61e7', 'Lyql2bQUx', true, {}, hello))\n}\n\n\nconst handler2 = (req, res) => {\n    const hello = {success: true, value: {firstName: 'a', lastName: 'c'}}\n    if (req.query.a) {\n        res.send(${parserRandomInstanceId}('e54b2c36', 'Lyql2bQUx', true, {}, hello))\n    } else {\n        res.send({them: true, those: false})\n    }\n}")
  }

  it("will setup for analysis and cleanup") {
    val before = file.contentAsString
    val result = RuntimeManager.newSession(f.project)
    Await.result(result, 10 seconds)

    val afterSetup = file.contentAsString
    assert(before != afterSetup)
    RuntimeManager.session.get.finish
    val afterCleanup = file.contentAsString
    assert(afterCleanup == before)
  }

  it("can collect runtime fragments while running") {
    val result = RuntimeManager.newSession(f.project)
    Await.result(result, 10 seconds)

    RuntimeManager.receiveFragment(RuntimeValueFragment("1", "hijklmnop", JsObject.empty))
    RuntimeManager.receiveFragment(RuntimeValueFragment("2", "hijklmnop", JsObject.empty))
    RuntimeManager.receiveFragment(RuntimeValueFragment("3", "hijklmnop", JsObject.empty))

    val fragments = RuntimeManager.finish.get
    assert(fragments.runtimeFragments.size == 3)
  }

}
