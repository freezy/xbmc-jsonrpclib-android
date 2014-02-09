package org.xbmc.android.jsonrpc.generator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

/**
 * A plugin so the generator can be used transparently in a Gradle build.
 *
 * @author freezy <freezy@xbmc.org>
 */
public class GradlePlugin implements Plugin<Project> {

	@Override
	public void apply(Project target) {
		target.getExtensions().create("generator", GeneratorPluginExtension.class);
		target.task("javaTask");
	}

	public static class GeneratorPluginExtension {
		public File outputDir;
		public File srcDir;
	}

}