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
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ProxyGenerator extends AbstractSourceGenerator
{
    public ProxyGenerator(TemplateDescriber p_describer,
                          TemplateUnit p_templateUnit)
    {
        super(p_describer, p_templateUnit);
    }

    public void generateSource(OutputStream p_out)
        throws java.io.IOException
    {
        m_writer = new CodeWriter(p_out);
        generateHeader();
        generatePrologue();
        generateImports();
        generateAnnotations();
        generateDeclaration();
        generateConstructors();
        generateIntf();
        generateImplData();
        if (m_templateUnit.getJamonContextType() != null)
        {
            generateJamonContextSetter();
        }
        generateOptionalArgs();
        generateFragmentInterfaces(false);
        if (! m_templateUnit.isParent())
        {
            generateConstructImplReflective();
            generateConstructImplDirect();
            generateMakeRenderer();
            generateRender();
            generateRenderNoFlush();
        }
        if (m_templateUnit.isParent())
        {
            generateParentRendererClass();
        }
        if (m_templateUnit.hasParentPath() && ! m_templateUnit.isParent())
        {
            generateMakeParentRenderer();
        }
        generateEpilogue();
        m_writer.finish();
    }

    private void generateImports()
    {
        m_templateUnit.printImports(m_writer);
    }

    private String getClassName()
    {
        return PathUtils.getIntfClassName(m_templateUnit.getName());
    }

    private String getPackageName()
    {
        return PathUtils.getIntfPackageName(m_templateUnit.getName());
    }


    private void generateHeader()
    {
        m_writer.println("// Autogenerated Jamon proxy");
        m_writer.println("// "
                         + m_describer.getExternalIdentifier(
                             m_templateUnit.getName()).replace('\\','/'));
        m_writer.println();
    }

    private void generatePrologue()
    {
        String pkgName = getPackageName();
        if (pkgName.length() > 0)
        {
            m_writer.println("package " + pkgName + ";");
            m_writer.println();
        }
    }


    private void generateConstructors()
    {
        m_writer.println();
        m_writer.println
            ("public " + getClassName()
             + "(" + ClassNames.TEMPLATE_MANAGER + " p_manager)");
        m_writer.openBlock();
        m_writer.println(" super(p_manager);");
        m_writer.closeBlock();

        m_writer.println();

        // We require a pass-through constructor for child templates and for replacement templates,
        // meaning that every proxy needs one.
        m_writer.println("protected " + getClassName() + "(String p_path)");
        m_writer.openBlock();
        m_writer.println("super(p_path);");
        m_writer.closeBlock();
        m_writer.println();

        if (! m_templateUnit.isParent())
        {
            m_writer.println("public " + getClassName() + "()");
            m_writer.openBlock();
            m_writer.println(" super(\"" + m_templateUnit.getName() + "\");");
            m_writer.closeBlock();
            m_writer.println();
        }
    }

    private void generateFragmentInterfaces(boolean p_inner)
    {
        for (FragmentArgument farg: m_templateUnit.getDeclaredFragmentArgs())
        {
            farg.getFragmentUnit().printInterface(m_writer, "public", !p_inner);
            m_writer.println();
        }
        m_writer.println();
    }

    private void generateAnnotations()
    {
        m_writer.print("@" + ClassNames.TEMPLATE_ANNOTATION);
        m_writer.openList("(", true);
        m_writer.printListElement("signature = \"" + m_templateUnit.getSignature() + "\"");
        if (m_templateUnit.getGenericParams().getCount() > 0) {
            m_writer.printListElement(
                "genericsCount = " + m_templateUnit.getGenericParams().getCount());
        }
        if (m_templateUnit.getInheritanceDepth() > 0) {
            m_writer.printListElement("inheritanceDepth = " + m_templateUnit.getInheritanceDepth());
        }
        if (m_templateUnit.getJamonContextType() != null) {
            m_writer.printListElement(
                "jamonContextType = \"" + m_templateUnit.getJamonContextType() + "\"");
        }
        generateArguments(m_templateUnit);
        generateMethodAnnotations();
        generateAbstractMethodAnnotations();
        m_writer.closeList(")");
        m_writer.println();
    }

    private void generateMethodAnnotations()
    {
        if (!m_templateUnit.getSignatureMethodUnits().isEmpty())
        {
            m_writer.printListElement("methods = ");
            m_writer.openList("{", true);
            for(MethodUnit methodUnit: m_templateUnit.getSignatureMethodUnits())
            {
                m_writer.printListElement("@" + ClassNames.METHOD_ANNOTATION);
                m_writer.openList("(", true);
                m_writer.printListElement("name = \"" + methodUnit.getName() + "\"");
                generateArguments(methodUnit);
                m_writer.closeList(")");
            }
            m_writer.closeList("}");
        }
    }

    private void generateAbstractMethodAnnotations()
    {
        if (!m_templateUnit.getAbstractMethodNames().isEmpty())
        {
            m_writer.printListElement("abstractMethodNames = ");
            m_writer.openList("{", false);
            for (String methodName: m_templateUnit.getAbstractMethodNames())
            {
                m_writer.printListElement("\"" + methodName + "\"");
            }
            m_writer.closeList("}");
        }
    }

    private void generateArguments(Unit p_unit)
    {
        generateArgumentAnnotations(
            "requiredArguments", p_unit.getSignatureRequiredArgs());
        generateArgumentAnnotations(
            "optionalArguments", p_unit.getSignatureOptionalArgs());
        generateFragmentAnnotations(p_unit.getFragmentArgs());
    }

    private void generateFragmentAnnotations(Collection<FragmentArgument> p_fargs)
    {
        if (! p_fargs.isEmpty())
        {
            m_writer.printListElement("fragmentArguments = ");
            m_writer.openList("{", true);
            for (FragmentArgument farg: p_fargs)
            {
                m_writer.printListElement("@" + ClassNames.FRAGMENT_ANNOTATION);
                m_writer.openList("(", true);
                m_writer.printListElement("name = \"" + farg.getName() + "\"");
                generateArgumentAnnotations("requiredArguments", farg.getFragmentUnit().getRequiredArgs());
                generateArgumentAnnotations("optionalArguments", farg.getFragmentUnit().getOptionalArgs());
                m_writer.closeList(")");
            }
            m_writer.closeList("}");
        }
    }

    private void generateArgumentAnnotations(
        String p_label,
        Collection<? extends AbstractArgument> p_args)
    {
        if (! p_args.isEmpty())
        {
            m_writer.printListElement(p_label + " = ");
            m_writer.openList("{", true);
            for(AbstractArgument argument: p_args)
            {
                m_writer.printListElement(
                    "@" + ClassNames.ARGUMENT_ANNOTATION + "(name = \""
                    + argument.getName() + "\", type = \"" + argument.getType() + "\")");
            }
            m_writer.closeList("}");
        }
    }

    private void generateDeclaration()
    {
        generateCustomAnnotations(m_templateUnit.getAnnotations(), AnnotationType.PROXY);
        m_writer.print("public ");
        if(m_templateUnit.isParent())
        {
            m_writer.print("abstract ");
        }
        m_writer.println(
            "class " + getClassName()
            + m_templateUnit.getGenericParams().generateGenericsDeclaration());

        m_writer.println("  extends " + m_templateUnit.getProxyParentClass());
        m_templateUnit.printInterfaces(m_writer);
        m_writer.openBlock();
    }

    private void generateConstructImplReflective()
    {
        m_writer.println();
        m_writer.println("@Override");
        m_writer.print(
            "public " + ClassNames.BASE_TEMPLATE + " constructImpl"
            + "(Class<? extends " + ClassNames.BASE_TEMPLATE + "> p_class)");
        m_writer.openBlock();
        m_writer.println("try");
        m_writer.openBlock();
        m_writer.println("return p_class");
        m_writer.indent();
        m_writer.println(".getConstructor(new Class [] { "
                         + ClassNames.TEMPLATE_MANAGER + ".class"
                         + ", ImplData.class })");
        m_writer.println(
            ".newInstance(new Object [] { getTemplateManager(), getTypedImplData()});");
        m_writer.outdent();
        m_writer.closeBlock();
        m_writer.println("catch (RuntimeException e)");
        m_writer.openBlock();
        m_writer.println("throw e;");
        m_writer.closeBlock();
        m_writer.println("catch (Exception e)");
        m_writer.openBlock();
        m_writer.println("throw new RuntimeException(e);");
        m_writer.closeBlock();
        m_writer.closeBlock();
    }

    private void generateConstructImplDirect()
    {
        m_writer.println();
        m_writer.println("@Override");
        m_writer.print("protected " + ClassNames.BASE_TEMPLATE
                       + " constructImpl()");
        m_writer.openBlock();
        m_writer.println(
            "return new "
            + PathUtils.getImplClassName(m_templateUnit.getName())
            + m_templateUnit.getGenericParams().generateGenericParamsList()
            + "(getTemplateManager(), getTypedImplData());");
        m_writer.closeBlock();
    }

    private void generateRender()
    {
        m_writer.print((m_templateUnit.isParent() ? "protected" : "public")
                       + " void render");
        m_writer.openList();
        m_writer.printListElement(ArgNames.WRITER_DECL);
        m_templateUnit.printRenderArgsDecl(m_writer);
        m_writer.closeList();
        m_writer.println();

        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.print("renderNoFlush");
        m_writer.openList();
        m_writer.printListElement(ArgNames.WRITER);
        m_templateUnit.printRenderArgs(m_writer);
        m_writer.closeList();
        m_writer.println(";");
        m_writer.println(ArgNames.WRITER + ".flush();");
        m_writer.closeBlock();
    }

    private void generateRenderNoFlush()
    {
        m_writer.print((m_templateUnit.isParent() ? "protected" : "public")
                       + " void renderNoFlush");
        m_writer.openList();
        m_writer.printListElement(ArgNames.WRITER_DECL);
        m_templateUnit.printRenderArgsDecl(m_writer);
        m_writer.closeList();
        m_writer.println();

        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        if (! m_templateUnit.getRenderArgs().isEmpty())
        {
            m_writer.println(
                "ImplData"
                + m_templateUnit.getGenericParams().generateGenericParamsList()
                + " implData = getTypedImplData();");
            for (AbstractArgument arg: m_templateUnit.getRenderArgs())
            {
                m_writer.println("implData." + arg.getSetterName()
                                 + "(" + arg.getName() + ");");
            }
        }
        if (m_templateUnit.getGenericParams().getCount() > 0) {
            m_writer.print("@SuppressWarnings(\"unchecked\") ");
        }
        m_writer.println(
            "Intf instance = (Intf) getTemplateManager().constructImpl(this);"
            );

        m_writer.println("instance.renderNoFlush(" + ArgNames.WRITER + ");");
        m_writer.println("reset();");
        m_writer.closeBlock();
        m_writer.println();
    }

    private void generateMakeRenderer()
    {
        m_writer.print("public " + ClassNames.RENDERER + " makeRenderer");
        m_writer.openList();
        m_templateUnit.printRenderArgsDecl(m_writer);
        m_writer.closeList();
        m_writer.println();

        m_writer.openBlock();
        m_writer.print(  "return new " + ClassNames.ABSTRACT_RENDERER + "() ");
        m_writer.openBlock();
        m_writer.println("@Override");
        m_writer.println("public void renderTo(" + ArgNames.WRITER_DECL + ")");
        m_writer.println(  "  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.print("render");
        m_writer.openList();
        m_writer.printListElement(ArgNames.WRITER);
        m_templateUnit.printRenderArgs(m_writer);
        m_writer.closeList();
        m_writer.println(";");
        m_writer.closeBlock();
        m_writer.closeBlock(";");
        m_writer.closeBlock();
        m_writer.println();
    }

    private void generateImplData()
    {
        m_templateUnit.getGenericParams()
            .suppressGenericHidingWarnings(m_writer);
        m_writer.println(
            "public static class ImplData"
            + m_templateUnit.getGenericParams().generateGenericsDeclaration());
        m_writer.print("  extends ");
        m_writer.println(implDataAncestor());
        if (m_templateUnit.isReplacing()) {
            m_writer.print("  implements ");
            m_writer.print(ClassNames.IMPL_DATA_COMPATIBLE);
            m_writer.println("<" + getReplacedImplDataClassName() + ">");
        }
        m_writer.openBlock();
        if (m_templateUnit.isReplacing()) {
            generatePopulateFrom();
        }
        if (m_templateUnit.isOriginatingJamonContext())
        {
            m_writer.println(
                "private " + m_templateUnit.getJamonContextType()
                + " m_jamonContext;");
            m_writer.println(
                "public " + m_templateUnit.getJamonContextType()
                + " getJamonContext()");
            m_writer.openBlock();
            m_writer.println("return m_jamonContext;");
            m_writer.closeBlock();
            m_writer.println(
                "public void setJamonContext("
                + m_templateUnit.getJamonContextType() + " p_jamonContext)");
            m_writer.openBlock();
            m_writer.println("m_jamonContext = p_jamonContext;");
            m_writer.closeBlock();
        }
        for (AbstractArgument arg: m_templateUnit.getDeclaredArgs())
        {
            arg.generateImplDataCode(m_writer);
        }
        m_writer.closeBlock();


        if (! m_templateUnit.isParent())
        {
            m_writer.println("@Override");
            m_writer.println("protected " + ClassNames.TEMPLATE + ".ImplData" + " makeImplData()");
            m_writer.openBlock();
            m_writer.println(
                "return new ImplData"
                + m_templateUnit.getGenericParams().generateGenericParamsList()
                             + "();");
            m_writer.closeBlock();
        }

        m_writer.println(
            "@SuppressWarnings(\"unchecked\") private ImplData"
            + m_templateUnit.getGenericParams().generateGenericParamsList()
            + " getTypedImplData()");
        m_writer.openBlock();
        m_writer.println(
            "return (ImplData"
            + m_templateUnit.getGenericParams().generateGenericParamsList()
            + ") getImplData();");
        m_writer.closeBlock();
    }

    private void generatePopulateFrom()
    {
        for (FragmentArgument farg: m_templateUnit.getFragmentArgs()) {
            generateFragmentDelegator(farg);
        }
        m_writer.print(
            "public void populateFrom(" + getReplacedImplDataClassName() + " implData) ");
        m_writer.openBlock();
        TemplateDescription replacedTemplateDescription =
            m_templateUnit.getReplacedTemplateDescription();
        for (RequiredArgument arg: m_templateUnit.getSignatureRequiredArgs()) {
            m_writer.println(arg.getSetterName() + "(implData." + arg.getGetterName() + "());");
        }
        Set<String> replacedTemplateOptionalArgNames =
            getOptionalArgNames(replacedTemplateDescription);
        for (OptionalArgument arg: replacedTemplateDescription.getOptionalArgs()) {
            if (replacedTemplateOptionalArgNames.contains(arg.getName())) {
                m_writer.println("if(implData." + arg.getIsNotDefaultName() + ") {");
                m_writer.println(
                    "  " + arg.getSetterName() + "(implData." + arg.getGetterName() + "());");
                m_writer.println("}");
            }
            else {
                m_writer.println(
                    arg.getSetterName() + "(implData." + arg.getGetterName() + "());");
            }
        }
        for (FragmentArgument farg: m_templateUnit.getFragmentArgs()) {
            m_writer.println(
                farg.getSetterName() + "(new " + getFragmentDelegatorName(farg)
                + "(implData." + farg.getGetterName() + "()));");
        }
        m_writer.closeBlock();
    }

    /**
     * Create a class to delegate from a fragment satisfying the interface for
     * the replaced template to a fragment satisfying the corresponding
     * interface in this template.
     * @param farg the fragment argument to create a delegator for.
     */
    private void generateFragmentDelegator(FragmentArgument farg)
    {
        String fragmentInterfaceName = "Intf.Fragment_" + farg.getName();
        String replacedFragmentInterfaceName =
            getReplacedProxyClassName() + "." + fragmentInterfaceName;
        FragmentUnit fragmentUnit = farg.getFragmentUnit();
        // name the fragment being converted "_frag_" to avoid name clashes with the parameters
        // passed to the fragment.
        m_writer.print(
            "private static class " + getFragmentDelegatorName(farg)
            + " implements " + fragmentInterfaceName);
        m_writer.openBlock();
        m_writer.println("private final " + replacedFragmentInterfaceName + " frag;");
        m_writer.println();

        m_writer.print(
            "public " + getFragmentDelegatorName(farg)
            + "(" + replacedFragmentInterfaceName + " frag)");
        m_writer.openBlock();
        m_writer.println("this.frag = frag;");
        m_writer.closeBlock();
        m_writer.println();
        m_writer.print("public void renderNoFlush");
        m_writer.openList();
        m_writer.printListElement(ArgNames.WRITER_DECL);
        fragmentUnit.printRenderArgsDecl(m_writer);
        m_writer.closeList();
        m_writer.println();
        m_writer.println("  throws java.io.IOException");
        m_writer.openBlock();
        m_writer.print("this.frag.renderNoFlush");
        m_writer.openList();
        m_writer.printListElement(ArgNames.WRITER);
        fragmentUnit.printRenderArgs(m_writer);
        m_writer.closeList();
        m_writer.println(";");
        m_writer.closeBlock();

        m_writer.print("public " + ClassNames.RENDERER + " makeRenderer");
        m_writer.openList();
        fragmentUnit.printRenderArgsDecl(m_writer);
        m_writer.closeList();
        m_writer.openBlock();
        m_writer.print("return this.frag.makeRenderer");
        m_writer.openList();
        fragmentUnit.printRenderArgs(m_writer);
        m_writer.closeList();
        m_writer.println(";");
        m_writer.closeBlock();

        m_writer.closeBlock();
        m_writer.println();
    }

    private String getFragmentDelegatorName(FragmentArgument farg) {
        return "Fragment_" + farg.getName() + "_Delegator";
    }

    private String getReplacedImplDataClassName()
    {
        return getReplacedProxyClassName() + ".ImplData";
    }

    private String getReplacedIntfClassName()
    {
        return getReplacedProxyClassName() + ".Intf";
    }

    private String getReplacedProxyClassName()
    {
        return PathUtils.getFullyQualifiedIntfClassName(
            m_templateUnit.getReplacedTemplatePath());
    }

    private static Set<String> getOptionalArgNames(
        TemplateDescription p_replacedTemplateDescription)
    {
        Set<String> replacedTemplateOptionalArgNames = new HashSet<String>();
        for (OptionalArgument arg: p_replacedTemplateDescription.getOptionalArgs()) {
            replacedTemplateOptionalArgNames.add(arg.getName());
        }
        return replacedTemplateOptionalArgNames;
    }

    private String implDataAncestor()
    {
        return m_templateUnit.hasParentPath() ?
            PathUtils.getFullyQualifiedIntfClassName(
                m_templateUnit.getParentPath()) + ".ImplData"
            : ClassNames.IMPL_DATA;
    }

    private void generateJamonContextSetter()
    {
        m_writer.println();
        if (! m_templateUnit.isOriginatingJamonContext())
        {
            m_writer.print("@Override ");
        }
        m_writer.print("public " );
        printFullProxyType();
        m_writer.println(
            " setJamonContext(" + m_templateUnit.getJamonContextType()
            + " p_jamonContext)");
        m_writer.openBlock();
        m_writer.println("getTypedImplData().setJamonContext(p_jamonContext);");
        m_writer.println("return this;");
        m_writer.closeBlock();
    }

    private void generateOptionalArgs()
    {
        for (OptionalArgument arg: m_templateUnit.getDeclaredOptionalArgs())
        {
            m_writer.println();
            m_writer.println("protected " + arg.getType() + " "
                             + arg.getName() + ";");
            m_writer.print("public final ");
            printFullProxyType();
            m_writer.println(
                " " + arg.getSetterName()
                + "(" + arg.getType() +" p_" + arg.getName() + ")");
            m_writer.openBlock();
            m_writer.println(
                "(" + "getTypedImplData()" + ")."
                + arg.getSetterName() + "(p_" + arg.getName() + ");");
            m_writer.println("return this;");
            m_writer.closeBlock();
        }
    }

    private void printFullProxyType()
    {
        String pkgName = getPackageName();
        if (pkgName.length() > 0)
        {
            m_writer.print(pkgName + ".");
        }
        m_writer.print(getClassName());
        m_writer.print(m_templateUnit.getGenericParams().generateGenericParamsList());
    }

    private void generateIntf()
    {
        m_templateUnit.getGenericParams()
            .suppressGenericHidingWarnings(m_writer);
        m_writer.println(
            "public interface Intf"
            + m_templateUnit.getGenericParams().generateGenericsDeclaration());
        m_writer.print("  extends "
                       + (m_templateUnit.hasParentPath()
                          ? PathUtils.getFullyQualifiedIntfClassName(
                              m_templateUnit.getParentPath()) + ".Intf"
                          : ClassNames.TEMPLATE_INTF));
        if (m_templateUnit.isReplacing()) {
            m_writer.print(", " + getReplacedIntfClassName());
        }
        m_writer.println();
        m_writer.openBlock();

        generateFragmentInterfaces(true);

        if(! m_templateUnit.isParent())
        {
            m_writer.println("void renderNoFlush(" + ArgNames.WRITER_DECL
                             + ") throws " + ClassNames.IOEXCEPTION + ";");
            m_writer.println();
        }
        m_writer.closeBlock();
    }

    private void generateParentRendererClass()
    {
        m_writer.println("public abstract class ParentRenderer");
        m_writer.openBlock();
        m_writer.println("protected ParentRenderer() {}");

        for (OptionalArgument arg:  m_templateUnit.getSignatureOptionalArgs())
        {
            m_writer.println();
            String name = arg.getName();
            m_writer.print("public final ParentRenderer ");
            m_writer.println(arg.getSetterName()
                             + "(" + arg.getType() +" p_" + name + ")");
            m_writer.openBlock();
            m_writer.println(getClassName() + ".this." + arg.getSetterName()
                             + "(" + "p_" + name + ");");
            m_writer.println("return this;");
            m_writer.closeBlock();
        }

        if (m_templateUnit.getJamonContextType() != null)
        {
            m_writer.print(
                "public final ParentRenderer setJamonContext("
                + m_templateUnit.getJamonContextType()
                + " p_jamonContext)");
            m_writer.openBlock();
            m_writer.println(
                getClassName() + ".this.setJamonContext(p_jamonContext);");
            m_writer.println("return this;");
            m_writer.closeBlock();
        }

        if (! m_templateUnit.hasParentPath())
        {
            m_writer.print("public void render");
            m_writer.openList();
            m_writer.printListElement(ArgNames.WRITER_DECL);
            m_templateUnit.printDeclaredRenderArgsDecl(m_writer);
            m_writer.closeList();
            m_writer.println();
            m_writer.print("  throws " + ClassNames.IOEXCEPTION);
            m_writer.openBlock();
            m_writer.print("renderNoFlush");
            m_writer.openList();
            m_writer.printListElement(ArgNames.WRITER);
            m_templateUnit.printDeclaredRenderArgs(m_writer);
            m_writer.closeList();
            m_writer.println(";");
            m_writer.println(ArgNames.WRITER + ".flush();");
            m_writer.closeBlock();

            m_writer.print("public void renderNoFlush");
            m_writer.openList();
            m_writer.printListElement(ArgNames.WRITER_DECL);
            m_templateUnit.printDeclaredRenderArgsDecl(m_writer);
            m_writer.closeList();
            m_writer.println();
            m_writer.print("  throws " + ClassNames.IOEXCEPTION);
            m_writer.openBlock();
            m_writer.print("renderChild");
            m_writer.openList();
            m_writer.printListElement(ArgNames.WRITER);
            m_templateUnit.printDeclaredRenderArgs(m_writer);
            m_writer.closeList();
            m_writer.println(";");
            m_writer.closeBlock();

            generateMakeRenderer();
        }
        else
        {
            generateMakeParentRenderer();
        }

        m_writer.print("protected abstract void renderChild");
        m_writer.openList();
        m_writer.printListElement(ArgNames.WRITER_DECL);
        m_templateUnit.printRenderArgsDecl(m_writer);
        m_writer.closeList();
        m_writer.println();
        m_writer.println("  throws " + ClassNames.IOEXCEPTION + ";");

        m_writer.closeBlock();
    }

    private void generateMakeParentRenderer()
    {
        String parentRendererClass =
            PathUtils.getFullyQualifiedIntfClassName(
                m_templateUnit.getParentPath()) + ".ParentRenderer";
        m_writer.print("public " + parentRendererClass
                       + " makeParentRenderer");
        m_writer.openList();
        m_templateUnit.printDeclaredRenderArgsDecl(m_writer);
        m_writer.closeList();
        m_writer.println();
        m_writer.openBlock();
        m_writer.print("return new " + parentRendererClass + "() ");
        m_writer.openBlock();
        m_writer.print("@Override protected void renderChild");
        m_writer.openList();
        m_writer.printListElement(ArgNames.WRITER_DECL);
        m_templateUnit.printParentRenderArgsDecl(m_writer);
        m_writer.closeList();
        m_writer.println();
        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.print(PathUtils
                       .getFullyQualifiedIntfClassName(getClassName()));
        if (m_templateUnit.isParent())
        {
            m_writer.print(".ParentRenderer.this.renderChild");
        }
        else
        {
            m_writer.print(".this.renderNoFlush");
        }
        m_writer.openList();
        m_writer.printListElement(ArgNames.WRITER);
        m_templateUnit.printRenderArgs(m_writer);
        m_writer.closeList();
        m_writer.println(";");
        m_writer.closeBlock();
        m_writer.closeBlock(";");
        m_writer.closeBlock();
    }

    private void generateEpilogue()
    {
        m_writer.println();
        m_writer.closeBlock();
    }
}
