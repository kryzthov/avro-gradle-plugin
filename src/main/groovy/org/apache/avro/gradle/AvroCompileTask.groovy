package org.apache.avro.gradle

import java.io.File;

import org.apache.avro.Protocol
import org.apache.avro.Schema;
import org.apache.avro.compiler.idl.Idl
import org.apache.avro.compiler.idl.ParseException
import org.apache.avro.compiler.specific.SpecificCompiler
import org.apache.avro.generic.GenericData
import org.apache.maven.artifact.DependencyResolutionRequiredException
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

/** Compiler for Avro IDL, Avro schema descriptors and Avro protocol descriptors. */
class AvroCompileTask extends SourceTask {
  static final String IDL_EXTENSION = ".avdl"
  static final String PROTOCOL_EXTENSION = ".avpr"
  static final String SCHEMA_EXTENSION = ".avsc"

  String stringType = "CharSequence"
  String templateDirectory = "/org/apache/avro/compiler/specific/templates/java/classic/"
  File destinationDir

  AvroCompileTask() {
     super()
     include "**/*$PROTOCOL_EXTENSION", "**/*$SCHEMA_EXTENSION", "**/*$IDL_EXTENSION"
  }

  @TaskAction
  void compile() {
    if (source.empty) {
      throw new Exception("Avro source is empty")
    }

    source.each { File file ->
      logger.info("Compiling Avro source ${file.name} to ${destinationDir.name} "
        + "[string-type=${stringType}]")
      try {
        // First check if GenAvro needs to be run
        if (file.absolutePath.endsWith(IDL_EXTENSION)) {
          final ClassLoader loader = new URLClassLoader(
              project.configurations.runtime.collect { it.toURI().toURL() } as URL[])

          final Idl parser = new Idl(file, loader)
          final Protocol p = parser.CompilationUnit()
          final String json = p.toString(true)
          final Protocol protocol = Protocol.parse(json)
          final SpecificCompiler compiler = new SpecificCompiler(protocol)
          compiler.setStringType(GenericData.StringType.valueOf(stringType))
          compiler.setTemplateDir(templateDirectory)
          compiler.compileToDestination(file, destinationDir)

        } else if (file.name.endsWith(SCHEMA_EXTENSION)) {
          Schema.Parser parser = new Schema.Parser()
          Schema schema = parser.parse(file)
          SpecificCompiler compiler = new SpecificCompiler(schema);
          compiler.setStringType(GenericData.StringType.valueOf(stringType))
          compiler.setTemplateDir(templateDirectory)
          compiler.compileToDestination(file, destinationDir)

        } else if (file.name.endsWith(PROTOCOL_EXTENSION)) {
          Protocol protocol = Protocol.parse(file)
          SpecificCompiler compiler = new SpecificCompiler(protocol)
          compiler.setStringType(GenericData.StringType.valueOf(stringType))
          compiler.setTemplateDir(templateDirectory)
          compiler.compileToDestination(file, destinationDir)

        } else {
          throw new Exception("Avro compiler plugin cannot handle file ${file.name}.")
        }
      } catch (ParseException e) {
        throw new IOException(e)
      } catch (DependencyResolutionRequiredException e) {
        throw new IOException(e)
      }
    }
  }
}
