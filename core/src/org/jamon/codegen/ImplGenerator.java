/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;

import org.jamon.util.StringUtils;

public class ImplGenerator
{
    public ImplGenerator(Writer p_writer,
                         TemplateResolver p_resolver,
                         TemplateDescriber p_describer,
                         ImplAnalyzer p_analyzer)
    {
        m_writer = new IndentingWriter(p_writer);
        m_resolver = p_resolver;
        m_analyzer = p_analyzer;
        m_describer = p_describer;
    }

    public void generateSource()
        throws IOException
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateConstructor();
        generateInitialize();
        generateRender();
        generateOptionalArgs();
        generateDefs();
        generateEpilogue();
    }

    private final TemplateResolver m_resolver;
    private final IndentingWriter m_writer;
    private final TemplateDescriber m_describer;
    private final ImplAnalyzer m_analyzer;

    private final String getPath()
    {
        return m_analyzer.getPath();
    }

    private String getClassName()
    {
        return m_resolver.getImplClassName(getPath());
    }

    private void generateDeclaration()
        throws IOException
    {
        m_writer.println("public class " + getClassName());
        m_writer.println("  extends "+ ClassNames.BASE_TEMPLATE);
        m_writer.println("  implements "
                         + m_resolver.getFullyQualifiedIntfClassName(getPath())
                         + ".Intf");
        m_writer.openBlock();
        m_writer.println(m_analyzer.getClassContent());
    }

    private void generateInitialize()
        throws IOException
    {
        m_writer.println("protected void initializeDefaultArguments()");
        m_writer.println("  throws Exception");
        m_writer.openBlock();
        for (Iterator i = m_analyzer.getOptionalArgNames(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            m_writer.println(name + " = " + m_analyzer.getDefault(name) + ";");
        }
        m_writer.closeBlock();
        m_writer.println();
    }

    private void generateConstructor()
        throws IOException
    {
        m_writer.println("public " +  getClassName()
                       + "(" + ClassNames.TEMPLATE_MANAGER
                       + " p_templateManager, String p_path)");
        m_writer.openBlock();
        m_writer.println("super(p_templateManager, p_path);");
        m_writer.closeBlock();
        m_writer.println();
    }

    private void generatePrologue()
        throws IOException
    {
        String pkgName = m_resolver.getImplPackageName(getPath());
        if (pkgName.length() > 0)
        {
            m_writer.println("package " + pkgName + ";");
            m_writer.println();
        }
    }


    private void generateDefFargInterface(FargInfo p_fargInfo)
        throws IOException
    {
        m_writer.println("private static interface "
                         + p_fargInfo.getFargInterfaceName());
        m_writer.println("  extends org.jamon.AbstractTemplateProxy.Intf");
        m_writer.openBlock();
        m_writer.print  ("void render(");

        for (Iterator a = p_fargInfo.getArgumentNames(); a.hasNext(); /* */)
        {
            String argName = (String) a.next();
            m_writer.print(p_fargInfo.getArgumentType(argName));
            m_writer.print(" ");
            m_writer.print(argName);
            if (a.hasNext())
            {
                m_writer.print(", ");
            }
        }
        m_writer.println(")");
        m_writer.println("  throws java.io.IOException;");

        m_writer.print("public " + ClassNames.RENDERER + " makeRenderer(");
        for (Iterator a = p_fargInfo.getArgumentNames(); a.hasNext(); /* */)
        {
            String argName = (String) a.next();
            m_writer.print(p_fargInfo.getArgumentType(argName));
            m_writer.print(" ");
            m_writer.print(argName);
            if (a.hasNext())
            {
                m_writer.print(", ");
            }
        }
        m_writer.println(");");
        m_writer.closeBlock();
        m_writer.println();
    }


    private void generateDefs()
        throws IOException
    {
        for (Iterator d = m_analyzer.getDefNames().iterator(); d.hasNext(); /* */)
        {
            String name = (String) d.next();
            m_writer.println();

            for (Iterator f = m_analyzer.getFargNames(name);
                 f.hasNext();
                 /* */)
            {
                generateDefFargInterface(m_analyzer.getFargInfo((String)f.next()));
            }

            m_writer.print("private void __jamon_def__");
            m_writer.print(name);
            m_writer.print("(");
            int argNum = 0;
            for (Iterator a = m_analyzer.getRequiredArgNames(name);
                 a.hasNext();
                 /* */)
            {
                if (argNum++ > 0)
                {
                    m_writer.print(",");
                }
                String arg = (String) a.next();
                m_writer.print("final " + m_analyzer.getArgType(name,arg)
                               + " " + arg);
            }
            for (Iterator a = m_analyzer.getOptionalArgNames(name);
                 a.hasNext();
                 /* */)
            {
                if (argNum++ > 0)
                {
                    m_writer.print(",");
                }
                String arg = (String) a.next();
                m_writer.print(m_analyzer.getArgType(name,arg) + " " + arg);
            }
            m_writer.println(")");
            m_writer.print  ("  throws " + ClassNames.IOEXCEPTION);
            m_writer.openBlock();
            for (Iterator i = m_analyzer.getStatements(name).iterator();
                 i.hasNext();
                 /* */)
            {
                ((Statement)i.next()).generateSource(m_writer,
                                                     m_resolver,
                                                     m_describer,
                                                     m_analyzer);
            }
            m_writer.closeBlock();
            m_writer.println();
        }
    }

    private void generateRender()
        throws IOException
    {
        m_writer.print("public void render(");
        m_analyzer.getUnitInfo().printRequiredArgsDecl(m_writer);
        m_writer.println(")");

        m_writer.print  ("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        for (Iterator i = m_analyzer.getStatements().iterator(); i.hasNext(); /* */)
        {
            ((Statement)i.next()).generateSource(m_writer,
                                                 m_resolver,
                                                 m_describer,
                                                 m_analyzer);
        }
        m_writer.closeBlock();
    }

    private void generateOptionalArgs()
        throws IOException
    {
        for (Iterator i = m_analyzer.getOptionalArgNames(); i.hasNext(); /* */)
        {
            m_writer.println();
            String name = (String) i.next();
            String type = m_analyzer.getArgType(name);
            m_writer.println("public void set" + StringUtils.capitalize(name)
                             + "(" + type + " p_" + name + ")");
            m_writer.openBlock();
            m_writer.println(name + " = p_" + name + ";");
            m_writer.closeBlock();
            m_writer.println();
            m_writer.println("private " + type + " " + name + ";");
        }
    }


    private void generateEpilogue()
        throws IOException
    {
        m_writer.println();
        m_writer.closeBlock();
    }

    private void generateImports()
        throws IOException
    {
        for (Iterator i = m_analyzer.getImports(); i.hasNext(); /* */ )
        {
            m_writer.println("import " + i.next() + ";");
        }
        m_writer.println();
    }

}
