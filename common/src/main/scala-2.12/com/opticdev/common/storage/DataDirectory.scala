package com.opticdev.common.storage

import better.files.File
import com.opticdev.common.PlatformConstants

object DataDirectory {
  val root = PlatformConstants.dataDirectory

  val packages = PlatformConstants.dataDirectory / "packages"
  val parserSkills = PlatformConstants.dataDirectory / "parser-skills"
  val parsers = PlatformConstants.dataDirectory / "parsers"
  val compiled = PlatformConstants.dataDirectory / "compiled"
  val sourcegear = PlatformConstants.dataDirectory / "sourcegear"
  val trainerScratch = PlatformConstants.dataDirectory / "trainer-scratch"
  val projectGraphs = PlatformConstants.dataDirectory / "project-graphs"
  val runtimeObjects = PlatformConstants.dataDirectory / "runtime-objects"

  def hasValidStructure = {
    root.isDirectory &&
      parsers.isDirectory &&
      packages.isDirectory &&
      parserSkills.isDirectory &&
      compiled.isDirectory &&
      sourcegear.isDirectory &&
      trainerScratch.isDirectory &&
      projectGraphs.isDirectory &&
      runtimeObjects.isDirectory
  }

  def buildDirectoryStructure = {
    root.createIfNotExists(asDirectory = true)
    parsers.createIfNotExists(asDirectory = true)
    parserSkills.createIfNotExists(asDirectory = true)
    packages.createIfNotExists(asDirectory = true)
    compiled.createIfNotExists(asDirectory = true)
    sourcegear.createIfNotExists(asDirectory = true)
    trainerScratch.createIfNotExists(asDirectory = true)
    projectGraphs.createIfNotExists(asDirectory = true)
    runtimeObjects.createIfNotExists(asDirectory = true)
  }

  def delete = root.delete(true)

  def reset = {
    delete
    buildDirectoryStructure
  }

  def init : File = {
    if (!hasValidStructure) {
      reset
    }

    DataDirectory.root
  }

  def emptyFolder(file: File): Unit = {
    file.delete(true)
    file.createIfNotExists(asDirectory = true)
  }

  def clearCaches = {
    emptyFolder(packages)
    emptyFolder(sourcegear)
  }

}
