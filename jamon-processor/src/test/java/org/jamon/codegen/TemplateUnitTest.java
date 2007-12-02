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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.jamon.compiler.TemplateFileLocation;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgValueNode;
import org.jamon.node.GenericsBoundNode;
import org.jamon.node.GenericsParamNode;
import org.jamon.node.LocationImpl;
import org.jamon.node.ParentArgNode;
import org.jamon.node.ParentArgWithDefaultNode;

public class TemplateUnitTest
    extends TestCase
{

    public void testInheritanceDepth() throws Exception
    {
        TemplateUnit parent = new TemplateUnit("/parent", null);
        TemplateUnit child = new TemplateUnit("/child", null);
        TemplateUnit grandchild =
            new TemplateUnit("/grandchild", null);
        child.setParentDescription(new TemplateDescription(parent));
        grandchild.setParentDescription(new TemplateDescription(child));

        assertEquals(0, parent.getInheritanceDepth());
        assertEquals(1, child.getInheritanceDepth());
        assertEquals(2, grandchild.getInheritanceDepth());
    }

    public void testParentArgs() throws Exception
    {
        TemplateUnit parent = new TemplateUnit("/parent", null);
        TemplateUnit child = new TemplateUnit("/child", null);
        TemplateUnit grandchild =
            new TemplateUnit("/grandchild", null);

        RequiredArgument pr1 = new RequiredArgument("pr1", "int", null);
        RequiredArgument pr2 = new RequiredArgument("pr2", "int", null);
        RequiredArgument cr3 = new RequiredArgument("cr2", "int", null);
        OptionalArgument po1 = new OptionalArgument("po1", "int", "op1");
        OptionalArgument po2 = new OptionalArgument("po2", "int", "op2");
        OptionalArgument co3 = new OptionalArgument("co2", "int", "oc3");

        parent.addRequiredArg(pr1);
        parent.addRequiredArg(pr2);
        parent.addOptionalArg(po1);
        parent.addOptionalArg(po2);
        child.setParentPath(parent.getName());
        child.setParentDescription(new TemplateDescription(parent));

        org.jamon.api.Location loc = new LocationImpl(new TemplateFileLocation("x"), 1,1);
        child.addParentArg(new ParentArgNode(loc, new ArgNameNode(loc, "pr2")));
        child.addParentArg(new ParentArgWithDefaultNode(
            loc, new ArgNameNode(loc, "po2"), new ArgValueNode(loc, "oc2")));
        child.addRequiredArg(cr3);
        child.addOptionalArg(co3);

        checkArgList(new RequiredArgument[] {pr1, pr2, cr3},
                     child.getSignatureRequiredArgs());
        checkArgList(new RequiredArgument[] {cr3},
                     child.getDeclaredRenderArgs());
        checkArgSet(new AbstractArgument[] {pr2, cr3, po2, co3},
                     child.getVisibleArgs());
        checkArgSet(new OptionalArgument[] {po1, po2, co3},
                     child.getSignatureOptionalArgs());
        checkArgSet(new OptionalArgument[] {co3},
                     child.getDeclaredOptionalArgs());

        FragmentArgument f = new FragmentArgument(
            new FragmentUnit("f", child, new GenericParams(), null, null), null);
        child.addFragmentArg(f);
        checkArgSet(new AbstractArgument[] {pr2, cr3, po2, co3, f},
                    child.getVisibleArgs());

        grandchild.setParentDescription(new TemplateDescription(child));
        checkArgList(new RequiredArgument[] {pr1, pr2, cr3},
                     grandchild.getSignatureRequiredArgs());
        checkArgList(new RequiredArgument[0],
                     grandchild.getDeclaredRenderArgs());
        checkArgSet(new AbstractArgument[] {}, grandchild.getVisibleArgs());
        checkArgSet(new OptionalArgument[] {po1, po2, co3},
                    grandchild.getSignatureOptionalArgs());
        checkArgSet(new OptionalArgument[0],
                    grandchild.getDeclaredOptionalArgs());
    }

    public void testSignature()
        throws Exception
    {
        TemplateUnit unit = new TemplateUnit("/foo", null);
        TemplateUnit parent = new TemplateUnit("/bar", null);

        Set<String> sigs = new HashSet<String>();
        checkSigIsUnique(unit, sigs);

        RequiredArgument i = new RequiredArgument("i", "int",null);
        RequiredArgument j = new RequiredArgument("j", "Integer", null);
        OptionalArgument a = new OptionalArgument("a", "boolean", "true");
        OptionalArgument b = new OptionalArgument("b", "Boolean", "null");
        FragmentUnit f = new FragmentUnit("f", unit, new GenericParams(), null, null);
        FragmentUnit g = new FragmentUnit("g", unit, new GenericParams(), null, null);

        unit.addRequiredArg(i);
        checkSigIsUnique(unit, sigs);

        unit.addRequiredArg(j);
        checkSigIsUnique(unit, sigs);

        unit.addOptionalArg(a);
        checkSigIsUnique(unit, sigs);

        unit.addOptionalArg(b);
        checkSigIsUnique(unit, sigs);

        unit = new TemplateUnit("/foo", null);
        unit.setParentDescription(new TemplateDescription(parent));
        checkSigIsUnique(unit, sigs);

        unit = new TemplateUnit("/foo", null);
        parent.addRequiredArg(i);
        unit.setParentDescription(new TemplateDescription(parent));
        // suboptimal - if the parent's sig changes, so does the child's
        checkSigIsUnique(unit, sigs);

        unit.addFragmentArg(new FragmentArgument(f, null));
        checkSigIsUnique(unit, sigs);
        f.addRequiredArg(new RequiredArgument("x", "float", null));
        checkSigIsUnique(unit, sigs);
        unit.addFragmentArg(new FragmentArgument(g, null));

        org.jamon.api.Location loc = new LocationImpl(null, 1, 1);
        GenericsParamNode genericsParamNode = new GenericsParamNode(loc, "d");
        unit.addGenericsParamNode(genericsParamNode);
        checkSigIsUnique(unit, sigs);
        genericsParamNode.addBound(new GenericsBoundNode(loc, "String"));
        checkSigIsUnique(unit, sigs);
    }


    public void testDependencies()
        throws Exception
    {
        TemplateUnit unit = new TemplateUnit("/foo/bar", null);
        unit.addCallPath("/baz");
        unit.addCallPath("/foo/wazza");
        unit.setParentPath("/foo/balla");
        Collection<String> dependencies = unit.getTemplateDependencies();
        assertEquals(3, dependencies.size());
        assertTrue(dependencies.contains("/baz"));
        assertTrue(dependencies.contains("/foo/balla"));
        assertTrue(dependencies.contains("/foo/wazza"));
    }

    private TemplateUnit makeUnitWithContext(
        String p_path, String p_context, TemplateUnit p_parent)
    {
        TemplateUnit unit = new TemplateUnit(p_path, null);
        unit.setJamonContextType(p_context);
        if (p_parent != null)
        {
            unit.setParentPath(p_parent.getName());
            unit.setParentDescription(new TemplateDescription(p_parent));
        }
        return unit;
    }

    public void testSimpleUnitIsOriginatingJamonContext() throws Exception
    {
        assertFalse(new TemplateUnit("/foo/bar", null)
            .isOriginatingJamonContext());
        assertTrue(makeUnitWithContext("/foo/bar", "someContext", null)
            .isOriginatingJamonContext());
    }

    public void testChildOfContextTemplateIsOriginatingJamonContext()
    {
        assertFalse(makeUnitWithContext(
            "/foo/baz",
            "someContext",
            makeUnitWithContext("/foo/bar", "someContext", null))
            .isOriginatingJamonContext());
    }

    public void testChildOfContextlessTemplateIsOriginatingJamonContext()
    {
        assertTrue(
            makeUnitWithContext(
                "/foo/bar", "jamonContext", new TemplateUnit("/foo/baz", null))
            .isOriginatingJamonContext());
    }

    private void checkSigIsUnique(TemplateUnit p_unit, Set<String> p_set)
        throws Exception
    {
        String sig = p_unit.getSignature();
        assertTrue(! p_set.contains(sig));
        p_set.add(sig);
    }

    private void checkArgList(AbstractArgument[] p_expected,
                              Collection<? extends AbstractArgument> p_actual)
    {
        assertEquals(Arrays.asList(p_expected), new ArrayList<AbstractArgument>(p_actual));
    }

    private void checkArgSet(AbstractArgument[] p_expected,
                             Collection<? extends AbstractArgument> p_actual)
    {
        assertEquals(
            new HashSet<AbstractArgument>(Arrays.asList(p_expected)),
            new HashSet<AbstractArgument>(p_actual));
    }
}