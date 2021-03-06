/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractNode;
import org.jamon.node.FragmentArgsNode;
import org.junit.Test;

public class FragmentArgsParserTest extends AbstractParserTest {
  private static String FRAG_END = "</%frag>";

  @Override
  protected AbstractNode parse(String text) throws IOException {
    final PositionalPushbackReader reader = makeReader(text);
    ParserErrorsImpl errors = new ParserErrorsImpl();
    try {
      FragmentArgsNode result = new FragmentArgsParser(reader, errors, START_LOC)
          .getFragmentArgsNode();
      if (errors.hasErrors()) {
        throw errors;
      }
      else {
        return result;
      }
    }
    catch (ParserErrorImpl e) {
      errors.addError(e);
      throw errors;
    }
  }

  private static FragmentArgsNode fragmentArgsNode(String name) {
    return new FragmentArgsNode(START_LOC, name);
  }

  @Test
  public void testNoArgs() throws Exception {
    assertEquals(fragmentArgsNode("foo"), parse(" foo>" + FRAG_END));
  }

  @Test
  public void testNoArgsConcise() throws Exception {
    assertEquals(fragmentArgsNode("foo"), parse(" foo/>"));
  }

  @Test
  public void testRequiredArgs() throws Exception {
    assertEquals(
      fragmentArgsNode("foo")
        .addArg(argNode(location(2, 1), "int", location(2, 5), "i"))
        .addArg(argNode(location(3, 1), "String", location(3, 8), "s")),
      parse(" foo>\nint i;\nString s;\n" + FRAG_END));
  }

  @Test
  public void testOptionalArgs() throws Exception {
    assertError(" foo>\nint i => 3;" + FRAG_END, 2, 7, FragmentArgsParser.NEED_SEMI);
  }

  @Test
  public void testMissingLabel() throws Exception {
    assertError(">" + FRAG_END, 1, 1, FragmentArgsParser.FRAGMENT_ARGUMENT_HAS_NO_NAME);
  }

  @Test
  public void testMissingLabelConcise() throws Exception {
    assertError("/>", 1, 1, FragmentArgsParser.FRAGMENT_ARGUMENT_HAS_NO_NAME);
  }

  @Test
  public void testMissingName() throws Exception {
    assertError(" foo>" + "\nf;" + FRAG_END, 2, 2, AbstractParser.NOT_AN_IDENTIFIER_ERROR);
  }

  @Test
  public void testMissingSemiAfterName() throws Exception {
    assertError(" foo>\nf a" + FRAG_END, 2, 4, FragmentArgsParser.NEED_SEMI);
  }

  @Test
  public void testBadArgCloseTag() throws Exception {
    assertError(" foo>\n<bar", 2, 1, AbstractParser.BAD_ARGS_CLOSE_TAG);
  }

  @Test
  public void testEofAfterArgStart() throws Exception {
    assertErrorTripple(
      " foo>\n",
      2, 1, AbstractParser.BAD_JAVA_TYPE_SPECIFIER,
      2, 1, AbstractParser.NOT_AN_IDENTIFIER_ERROR,
      2, 1, FragmentArgsParser.NEED_SEMI);
  }

  @Test
  public void testEofLookingForName() throws Exception {
    assertErrorPair(
      " foo>\na",
      2, 2, AbstractParser.NOT_AN_IDENTIFIER_ERROR,
      2, 2, FragmentArgsParser.NEED_SEMI);
  }

  @Test
  public void testEofLookingForPostNameSemi() throws Exception {
    assertError(" foo>\na b", 2, 4, FragmentArgsParser.NEED_SEMI);
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(FragmentArgsParserTest.class);
  }
}
