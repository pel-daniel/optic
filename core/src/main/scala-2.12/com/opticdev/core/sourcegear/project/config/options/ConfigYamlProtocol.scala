package com.opticdev.core.sourcegear.project.config.options

import better.files.File
import com.opticdev.common.{PackageRef, SchemaRef}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlArray, YamlBoolean, YamlFormat, YamlNumber, YamlObject, YamlReader, YamlString, YamlValue}
import net.jcazevedo.moultingyaml._
import org.yaml.snakeyaml.scanner.ScannerException

import scala.util.{Failure, Success, Try}

object ConfigYamlProtocol extends DefaultYamlProtocol {

  implicit object OCStringFormat extends YamlFormat[OCString] {
    override def read(yaml: YamlValue): OCString = Try(OCString(yaml.asInstanceOf[YamlString].value)).getOrElse(deserializationError("Invalid String Yaml"))
    override def write(obj: OCString): YamlValue = YamlString(obj.value)
  }

  implicit object OCNumberFormat extends YamlReader[OCNumber] {
    override def read(yaml: YamlValue): OCNumber = Try(OCNumber(yaml.asInstanceOf[YamlNumber].value)).getOrElse(deserializationError("Invalid Number Yaml"))
  }

  implicit object OCBooleanFormat extends YamlReader[OCBoolean] {
    override def read(yaml: YamlValue): OCBoolean = Try(OCBoolean(yaml.asInstanceOf[YamlBoolean].boolean)).getOrElse(deserializationError("Invalid Boolean Yaml"))
  }

  implicit object OCArrayFormat extends YamlReader[OCArray] {
    override def read(yaml: YamlValue): OCArray = Try(OCArray(yaml.asInstanceOf[YamlArray].elements.map(BaseYamlValueFormat.read):_*)).getOrElse(deserializationError("Invalid Array Yaml"))
  }

  implicit object OCObjectFormat extends YamlFormat[OCObject] {
    override def read(yaml: YamlValue): OCObject = Try(OCObject(yaml.asInstanceOf[YamlObject].fields.collect{
      case (k, v) if k.isInstanceOf[YamlString] => (k.asInstanceOf[YamlString].value, BaseYamlValueFormat.read(v))
    })).getOrElse(deserializationError("Invalid Object Yaml: "+ yaml.prettyPrint.trim()))
    override def write(obj: OCObject): YamlValue = ???
  }

  implicit object SchemaRefFormat extends YamlFormat[SchemaRef] {
    override def read(yaml: YamlValue): SchemaRef = {
      Try {
        val schema = SchemaRef.fromString(yaml.asInstanceOf[YamlString].value)
        require(schema.isSuccess && schema.get.packageRef.isDefined, s"Invalid abstraction reference '${yaml.prettyPrint
        .trim}'")
        schema
      } match {
        case s if s.isSuccess => s.get.get
        case f if f.isFailure => deserializationError(f.failed.get.getMessage)
      }
    }
    override def write(obj: SchemaRef): YamlValue = ???
  }

  implicit object PackageRefFormat extends YamlFormat[PackageRef] {
    override def read(yaml: YamlValue): PackageRef = {
      Try {
        val pr = PackageRef.fromString(yaml.asInstanceOf[YamlString].value)
        require(pr.isSuccess, s"Invalid Skill package reference '${yaml.prettyPrint
        .trim}'")
        pr
      } match {
        case s if s.isSuccess => s.get.get
        case f if f.isFailure => deserializationError(f.failed.get.getMessage)
      }
    }
    override def write(obj: PackageRef): YamlValue = ???
  }

  implicit object DefaultSettingsFormat extends YamlFormat[DefaultSettings] {
    override def read(yaml: YamlValue): DefaultSettings = {
      Try {
        OCObjectFormat.read(yaml)
      } match {
        case s if s.isSuccess => DefaultSettings(s.get)
        case f if f.isFailure => deserializationError("Default values must be Objects")
      }
    }
    override def write(obj: DefaultSettings): YamlValue = ???
  }

  implicit object BaseYamlValueFormat extends YamlFormat[OpticConfigValue] {
    override def read(yaml: YamlValue): OpticConfigValue = yaml match {
      case s: YamlString => OCStringFormat.read(yaml)
      case n: YamlNumber => OCNumberFormat.read(yaml)
      case b: YamlBoolean => OCBooleanFormat.read(yaml)
      case a: YamlArray => OCArrayFormat.read(yaml)
      case o: YamlObject => {
        o match {
          case objectRef if o.fields.keySet == Set(YamlString("$objectRef")) => OCObjectRefFormat.read(yaml)
          case _ => OCObjectFormat.read(yaml)
        }
      }
      case _ => null
    }

    override def write(obj: OpticConfigValue): YamlValue = ???
  }

  implicit object ConstantObjectFormat extends YamlFormat[ConstantObject] {
    override def read(yaml: YamlValue): ConstantObject = {
      Try {
        val asObject = yaml.asYamlObject
        val name = OCStringFormat.read(asObject.fields(YamlString("name"))).value
        val typeOption = Try(SchemaRefFormat.read(asObject.fields(YamlString("type")))).toOption
        val value =  BaseYamlValueFormat.read(asObject.fields(YamlString("value")))
        ConstantObject(name, typeOption, value)
      } match {
        case s if s.isSuccess => s.get
        case f if f.isFailure => deserializationError(f.failed.get.getMessage)
      }
    }
    override def write(obj: ConstantObject): YamlValue = ???
  }

  lazy implicit val OCObjectRefFormat = yamlFormat2(OCObjectRef)
  lazy implicit val pfFormat = yamlFormat9(ProjectFileInterface)
  lazy implicit val secondaryPfFormat = yamlFormat2(SecondaryProjectFileInterface)


  def parsePrimary(string: String): Try[ProjectFileInterface] = {
    val parse = for {
      yaml <- Try(string.parseYaml)
      pfInterface <- Try(yaml.convertTo[ProjectFileInterface](pfFormat))
    } yield pfInterface

    if (parse.isFailure) {
      Failure(parse.failed.get match {
        case de: DeserializationException => ProjectFileException(de.fieldNames.map(i=> s"""'${i}'""").mkString(",")+" "+de.msg)
        case se: ScannerException => ProjectFileException(se.getMessage)
        case _ => ProjectFileException("Parse Error")
      })
    } else {
      parse
    }
  }

  def parseSecondary(string: String): Try[SecondaryProjectFileInterface] = {
    val parse = for {
      yaml <- Try(string.parseYaml)
      pfInterface <- {
        if (yaml.isInstanceOf[YamlObject]) {
          Try(yaml.convertTo[SecondaryProjectFileInterface](secondaryPfFormat))
        } else Success(SecondaryProjectFileInterface(None, None))
      }
    } yield pfInterface

    if (parse.isFailure) {
      Failure(parse.failed.get match {
        case de: DeserializationException => ProjectFileException(de.fieldNames.map(i=> s"""'${i}'""").mkString(",")+" "+de.msg)
        case se: ScannerException => ProjectFileException(se.getMessage)
        case _ => ProjectFileException("Parse Error")
      })
    } else {
      parse
    }
  }

}