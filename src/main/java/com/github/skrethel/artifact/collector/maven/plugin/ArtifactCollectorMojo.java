package com.github.skrethel.artifact.collector.maven.plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Collect all artifacts.
 */
@Mojo(name = "collect", defaultPhase = LifecyclePhase.INSTALL, aggregator = true, requiresProject = true)
public class ArtifactCollectorMojo extends AbstractMojo {

	private static final String DEFAULT_PACKAGING = "rpm";
	/**
	 * Type of artifacts to collect. If not defined plugin will assume rpm packaging.
	 */
	@Parameter
	private List<String> targetPackaging;

	/**
	 * Directory name where all collected artifacts will be put.
	 * If path is relative, then it is relative parent build directory.
	 * If path is absolute then plugin assumes that it exists and
	 * can be written.
	 */
	@Parameter(defaultValue = "collected-artifacts", property = "targetDirectoryName", required = false)
	private String targetDirectoryName;

	/**
	 * Skip execution.
	 */
	@Parameter(defaultValue = "false", property = "skipCollecting", required = false)
	private boolean skip;

	@Component
	private transient MavenProject project;

	public void execute() throws MojoExecutionException {
		Log log = getLog();
		List<String> packaging;
		if (targetPackaging == null) {
			packaging = new ArrayList<String>(1);
			packaging.add(DEFAULT_PACKAGING);
		} else {
			packaging = targetPackaging;
		}
		log.info("Target packaging " + targetPackaging);
		if (skip) {
			log.debug("Skipping collection of artifacts");
			return;
		}
		File targetDirectoryFile = new File(targetDirectoryName);
		File outputDir;
		if (targetDirectoryFile.isAbsolute()) {
			outputDir = targetDirectoryFile;
			if (!outputDir.exists()) {
				throw new MojoExecutionException("Specified targetDirectoryName " + targetDirectoryName + " doesn't exists");
			}
			if (!outputDir.isDirectory()) {
				throw new MojoExecutionException("Specified targetDirectoryName " + targetDirectoryName + " is not a directory");
			}
		} else {
			MavenProject parent = getParent(project);
			while (parent != null) {
				MavenProject tmp = getParent(parent);
				if (tmp == null) {
					break;
				}
				parent = tmp;
			}
			if (parent == null) {
				log.debug("Parent not found, nothing to do");
				return;
			}
			outputDir = new File(parent.getBuild().getDirectory(), targetDirectoryName);
			log.debug("Found parent " + parent.getName() + ", output dir: " + outputDir);
		}
		if (packaging.contains(project.getPackaging())) {
			log.debug("Found requested artifact for project " + project.getName());
			try {
				FileUtils.mkdir(outputDir.getCanonicalPath());
			} catch (IOException e) {
				throw new MojoExecutionException("Unable to create directory " + outputDir.getAbsolutePath(), e);
			}
			File artifactFile = project.getArtifact().getFile();
			File destination = new File(outputDir, artifactFile.getName());
			try {
				log.info("Copying artifact " + artifactFile.getCanonicalPath() + " to " + destination.getCanonicalPath());
				FileUtils.copyFile(artifactFile, destination);
			} catch (IOException e) {
				throw new MojoExecutionException("Unable to copy file", e);
			}
		}
	}

	private MavenProject getParent(MavenProject project) {
		return project.getParent() == null || project.getParent().getFile() == null ? null : project.getParent();
	}
}
