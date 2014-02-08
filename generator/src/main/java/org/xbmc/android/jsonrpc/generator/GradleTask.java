package org.xbmc.android.jsonrpc.generator;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

import static org.xbmc.android.jsonrpc.generator.GradlePlugin.GeneratorPluginExtension;

/**
 * The Gradle task used by the plugin.
 */
public class GradleTask extends DefaultTask {

	@TaskAction
	public void javaTask() {
		final GeneratorPluginExtension config =(GeneratorPluginExtension)getProject().getExtensions().getByName("generator");
		Introspect.generate(new File(config.getSrcDir()), new File(config.getOutputDir()));
	}
}