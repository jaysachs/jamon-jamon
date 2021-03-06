/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jamon.codegen.FragmentArgument;
import org.jamon.codegen.MethodUnit;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.TemplateDescription;
import org.jamon.compiler.FileTemplateSource;

public class TemplateDescriberTest extends TestCase {
  private TemplateDescriber describer;

  @Override
  public void setUp() throws Exception {
    File nonexistent = File.createTempFile("jamontest", null);
    nonexistent.deleteOnExit();
    describer =
      new TemplateDescriber(new FileTemplateSource(nonexistent), getClass().getClassLoader());
  }

  public void testJamonContext() throws Exception {
    TemplateDescription desc =
      describer.getTemplateDescription("/test/jamon/context/ContextCallee", null);
    assertEquals("org.jamon.integration.TestJamonContext", desc.getJamonContextType());
  }

  public void testNoJamonContext() throws Exception {
    TemplateDescription desc = describer.getTemplateDescription("/test/jamon/ClassOnly", null);
    assertNull("", desc.getJamonContextType());
  }

  public void testArgumentIntrospection() throws Exception {
    TemplateDescription desc = describer.getTemplateDescription("/test/jamon/ClassOnly", null);
    NameType.checkArgs(
      desc.getRequiredArgs(), new NameType("i", "int"), new NameType("j", "Integer"));
    NameType.checkArgs(desc.getOptionalArgs(), new NameType("foo", "String"));
  }

  public void testFragmentUnitIntrospection() throws Exception {
    List<FragmentArgument> fragmentUnitIntfs = describer.getTemplateDescription(
      "/test/jamon/ClassOnly", null).getFragmentInterfaces();

    assertEquals(2, fragmentUnitIntfs.size());
    FragmentArgument f2 = fragmentUnitIntfs.get(0);
    FragmentArgument f1 = fragmentUnitIntfs.get(1);

    assertEquals("f1", f1.getName());
    assertEquals("f2", f2.getName());
    NameType.checkArgs(
      f1.getFragmentUnit().getRequiredArgs(),
      new NameType("k", "int"),
      new NameType("m", "Boolean[]"),
      new NameType("a1", "String"),
      new NameType("a4", "String"),
      new NameType("a2", "String"),
      new NameType("a3", "String"),
      new NameType("a5", "String") );

    NameType.checkArgs(f2.getFragmentUnit().getRequiredArgs());
  }

  public void testMethodUnitIntrospection() throws Exception {
    Map<String, MethodUnit> methods = describer.getTemplateDescription("/test/jamon/ClassOnly",
      null).getMethodUnits();
    assertEquals(1, methods.size());
    MethodUnit method = methods.get("m");

    assertNotNull(method);
    assertEquals("m", method.getName());
    NameType.checkArgs(method.getSignatureRequiredArgs(), new NameType("mi", "int"));
    NameType.checkArgs(method.getSignatureOptionalArgs(), new NameType("mj", "int"));

    Collection<FragmentArgument> fragments = method.getFragmentArgs();
    assertEquals(1, fragments.size());
    FragmentArgument frag = fragments.iterator().next();
    assertEquals("mf", frag.getName());
    NameType.checkArgs(frag.getFragmentUnit().getRequiredArgs(), new NameType("mk", "int"));
  }

  public void checkGenericIntrospection() throws Exception {
    assertEquals(
      3,
      describer.getTemplateDescription("test/jamon/ClassOnly", null).getGenericParamsCount());
  }

}
