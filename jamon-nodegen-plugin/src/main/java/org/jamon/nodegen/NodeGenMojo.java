/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.nodegen;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Generate AST nodes.
 *
 * @goal generate-ast-nodes
 * @phase generate-source
 * @threadSafe
 */
public class NodeGenMojo extends AbstractMojo {
  public void execute() throws MojoExecutionException {
    try {
      getLog().info("Parsing node description file");
      Iterable<NodeDescriptor> nodes = NodesParser.parseNodes(new FileReader(nodeDescriptionFile));
      File target = new File(destinationDirectory, destinationPackage.replace('.',
        File.separatorChar));
      getLog().info("Initializing destination directory " + target);
      initializeTargetDir(target);
      getLog().info("Generating node classes");
      new NodeGenerator(destinationPackage).generateSources(nodes, target);
      getLog().info("Generating analysis adapter classes");
      AnalysisGenerator ag = new AnalysisGenerator(destinationPackage, target, nodes);
      ag.generateAnalysisInterface();
      ag.generateAnalysisAdapterClass();
      ag.generateDepthFirstAdapterClass();
    }
    catch (IOException e) {
      getLog().error(e);
      throw new MojoExecutionException(e.getMessage());
    }
    project.addCompileSourceRoot(destinationDirectory.getAbsolutePath());
  }

  private void initializeTargetDir(File targetDir) {
    targetDir.mkdirs();
    File[] files = targetDir.listFiles();
    for (int i = 0; i < files.length; i++) {
      files[i].delete();
    }
  }

  /**
   * @parameter expression="${project}"
   */
  private MavenProject project;

  /**
   * @parameter
   * @required
   */
  private File nodeDescriptionFile;

  /**
   * @parameter
   * @required
   */
  private File destinationDirectory;

  /**
   * @parameter default-value="org.jamon.node"
   */
  private String destinationPackage;
}
