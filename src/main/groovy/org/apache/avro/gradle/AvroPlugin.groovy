package org.apache.avro.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin to compile Avro definitions to Java generated sources.
 */
class AvroPlugin implements Plugin<Project> {

  /** {@inheritDoc} */
  @Override
  void apply(final Project project) {
    project.task(type: AvroCompileTask, 'compileAvro')
    // TODO: There must be a better way than that:
    // project.afterEvaluate {
    //   project.tasks.compileAvro.execute()
    // }
  }
}
