package org.jamon.nodegen;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class NodesParser {
  private NodesParser() {}

  public static Iterable<NodeDescriptor> parseNodes(Reader nodesDescriptor) throws IOException {
    LineNumberReader reader = new LineNumberReader(nodesDescriptor);
    String line;
    Map<String, NodeDescriptor> nodes = new HashMap<String, NodeDescriptor>();

    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (!line.startsWith("#") && line.length() > 0) {
        NodeDescriptor node = new NodeDescriptor(line, nodes);
        nodes.put(node.getName(), node);
      }
    }
    return nodes.values();
  }

}
