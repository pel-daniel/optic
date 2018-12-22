package com.opticdev.core.sourcegear.storage

import better.files.File
import com.opticdev.common.storage.DataDirectory
import com.opticdev.core.sourcegear.graph.objects.ObjectNode
import com.opticdev.runtime.RuntimeValueFragment
import play.api.libs.json.{JsArray, Json}

import scala.util.Try

object ProjectRuntimeFragmentStorage {

  implicit val projectRuntimeFragmentFormats = Json.format[RuntimeValueFragment]

  def addManyToStorage(runtimeValueFragment: Vector[RuntimeValueFragment], projectName: String): File = {
    val file = DataDirectory.runtimeFragments / projectName createIfNotExists(asDirectory = false)
    val jsArray = loadFromStorage(projectName).getOrElse(JsArray.empty)

    val newItems = runtimeValueFragment.map(Json.toJson[RuntimeValueFragment])

    val updated = jsArray ++ JsArray(newItems)

    file.write(updated.toString())
    file
  }

  def addToStorage(runtimeValueFragment: RuntimeValueFragment, projectName: String): File = {
    val file = DataDirectory.runtimeFragments / projectName createIfNotExists(asDirectory = false)
    val jsArray = loadFromStorage(projectName).getOrElse(JsArray.empty)

    val updated = jsArray :+ Json.toJson[RuntimeValueFragment](runtimeValueFragment)

    file.write(updated.toString())
    file
  }

  def loadFromStorage(projectName: String) : Try[JsArray] = Try {
    val file = DataDirectory.runtimeFragments / projectName createIfNotExists(asDirectory = false)
    Json.parse(file.contentAsString).as[JsArray]
  }

  def loadFromStorageAsRuntimeFragments(projectName: String) = {
    loadFromStorage(projectName)
      .map(i => i.value.map(Json.fromJson[RuntimeValueFragment]).toVector)
      .map(_.collect {case i if i.isSuccess => i.get})
  }

  def clearStorage(projectName: String) = {
    val file = DataDirectory.runtimeFragments / projectName
    file.delete(true)
  }

}
