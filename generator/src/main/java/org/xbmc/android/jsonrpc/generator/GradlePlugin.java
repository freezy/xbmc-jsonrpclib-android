package org.xbmc.android.jsonrpc.generator;

import org.gradle.api.Project;
import org.gradle.api.Plugin;

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

		private String outputDir;
		private String srcDir;

		public String getOutputDir() {
			return outputDir;
		}

		public void setOutputDir(String outputDir) {
			this.outputDir = outputDir;
		}

		public String getSrcDir() {
			return srcDir;
		}

		public void setSrcDir(String srcDir) {
			this.srcDir = srcDir;
		}
	}

}