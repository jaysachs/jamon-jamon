package org.modusponens.jtt;

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class ImplGenerator extends BaseGenerator
{
    private List m_body = new ArrayList();
    private StringBuffer m_current = new StringBuffer();

    public ImplGenerator(Writer p_writer,
                         String p_packageName,
                         String p_className)
    {
        super(p_writer,p_packageName,p_className);
    }

    public void generateClassSource()
        throws IOException
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateConstructor();
        generateRender();
        generateOptionalArgs();
        generateEpilogue();
    }

    public void caseABodyComponent(ABodyComponent node)
    {
        m_current.append(node.getText().getText());
    }

    public void caseANewlineComponent(ANewlineComponent node)
    {
        m_current.append(node.getNewline().getText());
    }

    public void caseAJavaComponent(AJavaComponent node)
    {
        handleBody();
        StringBuffer buf = new StringBuffer();
        for (Iterator i = node.getAny().iterator(); i.hasNext(); /* */)
        {
            buf.append(((TAny)i.next()).getText());
        }
        m_body.add(buf.toString());
    }

    public void caseAJlineComponent(AJlineComponent node)
    {
        handleBody();
        m_body.add(node.getFragment().getText());
    }

    private void handleBody()
    {
        if (m_current.length() > 0)
        {
            m_body.add("    write(\""
                       + javaEscape(m_current.toString())
                       + "\");");
            m_current = new StringBuffer();
        }
    }

    public void caseAEmitComponent(AEmitComponent node)
    {
        handleBody();
        StringBuffer expr = new StringBuffer();
        TEscape escape = node.getEscape();
        if (escape == null)
        {
            expr.append("    write(");
        }
        else
        {
            char c = escape.getText().trim().charAt(1);
            switch (c)
            {
              case 'h': expr.append("    writeHtmlEscaped("); break;
              case 'u': expr.append("    writeUrlEscaped("); break;
              case 'n': expr.append("    writeUnEscaped("); break;
              case 'x': expr.append("    writeXmlEscaped("); break;
              default:
                throw new RuntimeException("Unknown escape " + c);
            }
        }
        expr.append("String.valueOf(");
        for (Iterator i = node.getAny().iterator(); i.hasNext(); /* */)
        {
            expr.append(((TAny)i.next()).getText());
        }
        expr.append("));");
        m_body.add(expr);
    }

    public void caseACallComponent(ACallComponent node)
    {
    }

    public void caseEOF(EOF node)
    {
        handleBody();
    }

    private String javaEscape(String p_string)
    {
        // assert p_string != null
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < p_string.length(); ++i)
        {
            char c = p_string.charAt(i);
            switch(c)
            {
              case '\n': s.append("\\n"); break;
              case '\t': s.append("\\t"); break;
              case '\"': s.append("\\\""); break;
              default: s.append(c);
            }
        }
        return s.toString();
    }

    private void generateDeclaration()
        throws IOException
    {
        print("public class ");
        print(getClassName());
        println("Impl");
        print("  extends ");
        println(BASE_TEMPLATE);
        print("  implements ");
        println(getClassName());
        println("{");
    }

    private void generateConstructor()
        throws IOException
    {
        print("  public ");
        print(getClassName());
        println("Impl(java.io.Writer p_writer,");
        println("        org.modusponens.jtt.TemplateManager p_templateManager)");
        println("  {");
        println("    super(p_writer, p_templateManager);");
        println("  }");
        println();

    }

    private static final String BASE_TEMPLATE =
        "org.modusponens.jtt.AbstractTemplate";

    private void generateRender()
        throws IOException
    {
        print("  public void render(");
        for (Iterator i = getRequiredArgs(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            print(getArgType(name));
            print(" ");
            print(name);
            if (i.hasNext())
            {
                print(", ");
            }
        }
        println(")");

        println("    throws java.io.IOException");
        println("  {");
        for (Iterator i = m_body.iterator(); i.hasNext(); /* */)
        {
            println(i.next());
        }
        println("  }");
    }

    private void generateOptionalArgs()
        throws IOException
    {
        for (Iterator i = getOptionalArgs(); i.hasNext(); /* */)
        {
            println();
            String name = (String) i.next();
            print("  public void set");
            print(capitalize(name));
            print("(");
            String type = getArgType(name);
            print(type);
            print(" p_");
            print(name);
            println(")");
            println("  {");
            print("    ");
            print(name);
            print(" = p_");
            print(name);
            println(";");
            println("  }");
            println();
            print("  private ");
            print(type);
            print(" ");
            print(name);
            print(" = ");
            print(getDefault(name));
            println(";");
        }
    }

    private void generateEpilogue()
        throws IOException
    {
        println();
        println("}");
    }

}