package org.jamon.nodegen;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class NodesGenerator
{
    private NodesGenerator() {}

    private static Iterable<NodeDescriptor> parseNodes(
        Reader p_nodesDescriptor) throws IOException
    {
        LineNumberReader reader = new LineNumberReader(p_nodesDescriptor);
        String line;
        Map<String, NodeDescriptor> nodes = 
            new HashMap<String, NodeDescriptor>();

        while ((line = reader.readLine()) != null)
        {
            line.trim();
            if (!line.startsWith("#") && line.length() > 0)
            {
                NodeDescriptor node = new NodeDescriptor(line, nodes); 
                nodes.put(node.getName(), node);
            }
        }
        return nodes.values();
    }
    
    private static void initializeDir(File p_srcDir)
    {
        p_srcDir.mkdirs();
        File[] files = p_srcDir.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            files[i].delete();
        }
    }

    /**
     * Generate Node source files
     * @param args The first argument is the node descriptor file.
     *              The second argument is the directory to put generated 
     *              files in. 
     **/
    public static void main(String[] args) throws IOException
    {
        Iterable<NodeDescriptor> nodes =
            parseNodes(new FileReader(new File(args[0])));
        File srcDir =
            new File(
                new File(new File(new File(args[1]), "org"), "jamon"),
                "node");
        initializeDir(srcDir);
        NodeGenerator.generateSources(nodes, srcDir);
        AnalysisGenerator.generateAnalysisInterface(
            new PrintWriter(new FileWriter(new File(srcDir, "Analysis.java"))),
            nodes);
        AnalysisGenerator.generateAnalysisAdapterClass(
            new PrintWriter(new FileWriter(
                new File(srcDir, "AnalysisAdapter.java"))),
            nodes);
        AnalysisGenerator.generateDepthFirstAdapterClass(
            new PrintWriter(
                new FileWriter(new File(srcDir, 
                                        "DepthFirstAnalysisAdapter.java"))),
                nodes);
    }

}