package com.opticdev.core.sourcegear.storage

import java.io.FileNotFoundException
import java.nio.ByteBuffer

import better.files.File
import boopickle.Default._
import com.opticdev.common.storage.DataDirectory
import com.opticdev.core.sourcegear.CompiledLens
import com.opticdev.core.sourcegear.graph.objects.ObjectNode
import com.opticdev.core.sourcegear.serialization.PickleImplicits._
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import ObjectNode._
import scala.util.Try

object ProjectRuntimeObjectStorage {

  def addToStorage(objectNode: ObjectNode, projectName: String): File = {
    val file = DataDirectory.runtimeObjects / projectName createIfNotExists(asDirectory = false)
    val jsArray = loadFromStorage(projectName).getOrElse(JsArray.empty)

    val updated = jsArray :+ Json.toJson[ObjectNode](objectNode)

    file.write(updated.toString())
    file
  }

  def loadFromStorage(projectName: String) : Try[JsArray] = Try {
    val file = DataDirectory.runtimeObjects / projectName createIfNotExists(asDirectory = false)
    Json.parse(file.contentAsString).as[JsArray]
  }

  def loadFromStorageAsObjectNodes(projectName: String) = {
    loadFromStorage(projectName)
      .map(i => i.value.map(Json.fromJson[ObjectNode]).toVector)
      .map(_.collect {case i if i.isSuccess => i.get})
  }

  def clearStorage(projectName: String) = {
    val file = DataDirectory.runtimeObjects / projectName
    file.delete(true)
  }

}
