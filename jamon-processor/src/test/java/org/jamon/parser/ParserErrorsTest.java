/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import org.junit.Test;

public class ParserErrorsTest extends AbstractParserTest {
  @Test
  public void testMaformedJavaTag() throws Exception {
    assertError("foo<%java >", 1, 4, "Reached end of file while looking for '%>'");
  }

  @Test
  public void testMalformedLiteralTag() throws Exception {
    assertError("<%LITERAL >", 1, 1, "Malformed tag");
  }

  @Test
  public void testMalformedExtendsTag() throws Exception {
    assertError("<%extends>", 1, 1, TopLevelParser.MALFORMED_EXTENDS_TAG_ERROR);
    assertError("<%extends /foo f>", 1, 15, AbstractParser.MALFORMED_TAG_ERROR);
  }

  @Test
  public void testMalformedReplacesTag() throws Exception {
    assertError("<%replaces>", 1, 1, TopLevelParser.MALFORMED_REPLACES_TAG_ERROR);
    assertError("<%replaces /foo f>", 1, 16, AbstractParser.MALFORMED_TAG_ERROR);
  }

  @Test
  public void testMalformedAnnotateTag() throws Exception {
    assertError("<%annotate>", 1, 1, TopLevelParser.MALFORMED_ANNOTATE_TAG_ERROR);
    assertError("<%annotate @Whatever>", 1, 1, AbstractBodyParser.PERCENT_GREATER_THAN_EOF_ERROR);
    assertError(
      "<%annotate @Whatever\n#huh %>",
      2, 1, TopLevelParser.UNRECOGNIZED_ANNOTATION_TYPE_ERROR);
    assertError(
      "<%annotate @Whatever\n# %>",
      2, 1, TopLevelParser.UNRECOGNIZED_ANNOTATION_TYPE_ERROR);
    assertError(
      "<%annotate @Whatever\n#impl dimple %>",
      1, 1, TopLevelParser.MALFORMED_ANNOTATE_TAG_ERROR);
  }

  @Test
  public void testUnfinishedJavaTag() throws Exception {
    assertError("foo<%java>ab</%java", 1, 4, "Reached end of file while looking for '</%java>'");
  }

  @Test
  public void testUnfinishedLiteralTag() throws Exception {
    assertError("<%LITERAL>", 1, 1, "Reached end of file while looking for '</%LITERAL>'");
  }

  @Test
  public void testUnfinishedClassTag() throws Exception {
    assertError("<%class>", 1, 1, "Reached end of file while looking for '</%class>'");
  }

  @Test
  public void testCloseOnEscapeInString() throws Exception {
    assertError("<%java> \"\\", 1, 9, "Reached end of file while inside a java quote");
  }

  @Test
  public void testMalformedCloseTag() throws Exception {
    assertError("</%foo", 1, 1, "Malformed tag");
    assertError("</%foo >", 1, 1, "Malformed tag");
    assertError("<%def foo></%def >", 1, "<%def foo> ".length(), "Malformed tag");
  }

  @Test
  public void testWrongCloseTag() throws Exception {
    assertError("<%def bob></%foo>", 1, "<%def bob> ".length(), "Unexpected tag close </%foo>");
  }

  @Test
  public void testTopLevelCloseTag() throws Exception {
    assertError("</%def>", 1, 1, "Unexpected tag close </%def>");
  }

  @Test
  public void testUnexpectedCloseTags() throws Exception {
    assertError("</&>", 1, 1, AbstractBodyParser.UNEXPECTED_FRAGMENTS_CLOSE_ERROR);
    assertError("</|>", 1, 1, AbstractBodyParser.UNEXPECTED_NAMED_FRAGMENT_CLOSE_ERROR);
  }

  @Test
  public void testNoDefCloseTag() throws Exception {
    assertError("<%def a>abc", 1, 9, SubcomponentParser.makeError("def"));
  }

  @Test
  public void testNoMethodCloseTag() throws Exception {
    assertError("<%method a>abc", 1, 12, SubcomponentParser.makeError("method"));
  }

  @Test
  public void testNoOverrideCloseTag() throws Exception {
    assertError("<%override a>abc", 1, 14, SubcomponentParser.makeError("override"));
  }

  @Test
  public void testNestedDefs() throws Exception {
    assertError("<%def foo><%def bar></%def>", 1, 11,
      "<%def> sections only allowed at the top level of a document");
  }

  @Test
  public void testNestedMethods() throws Exception {
    assertError(
      "<%def foo><%method bar></%def>",
      1, 11, "<%method> sections only allowed at the top level of a document");
  }

  @Test
  public void testNestedOverrides() throws Exception {
    assertError(
      "<%method foo><%override bar></%method>",
      1, 14, "<%override> sections only allowed at the top level of a document");
  }

  @Test
  public void testNestedAbsMethods() throws Exception {
    assertError(
      "<%method foo><%absmeth bar></%method>",
      1, 14, "<%absmeth> sections only allowed at the top level of a document");
  }

  @Test
  public void testContentInAbsMethod() throws Exception {
    assertError("<%absmeth foo>bar", 1, 14, TopLevelParser.BAD_ABSMETH_CONTENT);
  }

  @Test
  public void testUnknownTag() throws Exception {
    assertError("<%foo>", 1, 1, "Unknown tag <%foo>");
    assertError("<%foo", 1, 1, "Malformed tag");
  }

  @Test
  public void testEmitErrors() throws Exception {
    assertError("<% foo #. %>", 1, 9, AbstractBodyParser.EMIT_ESCAPE_CODE_ERROR);
    assertError("<% foo", 1, 1, AbstractBodyParser.PERCENT_GREATER_THAN_EOF_ERROR);
    assertError("<% foo #aa %>", 1, 9, AbstractBodyParser.EMIT_MISSING_TAG_END_ERROR);
    assertError("<% \". %>", 1, 4, AbstractParser.EOF_IN_JAVA_QUOTE_ERROR);
  }

  @Test
  public void testClassInSubcomponent() throws Exception {
    assertError("<%def d><%class></%def>", 1, 9, AbstractBodyParser.CLASS_TAG_IN_SUBCOMPONENT);
  }

  @Test
  public void testExtendsInSubcomponent() throws Exception {
    assertError(
      "<%def d><%extends foo></%def>", 1, 9, AbstractBodyParser.EXTENDS_TAG_IN_SUBCOMPONENT);
  }

  @Test
  public void testImplementsInSubcomponent() throws Exception {
    assertError(
      "<%def foo>\n<%implements></%def>", 2, 1, AbstractBodyParser.IMPLEMENTS_TAG_IN_SUBCOMPONENT);
  }

  @Test
  public void testReplacesInSubcomponent() throws Exception {
    assertError(
      "<%def foo>\n<%replaces /foo></%def>", 2, 1, AbstractBodyParser.REPLACES_TAG_IN_SUBCOMPONENT);
  }

  @Test
  public void testReplaceableInSubcomponent() throws Exception {
    assertError(
      "<%def foo>\n<%replaceable></%def>",
      2, 1, AbstractBodyParser.REPLACEABLE_TAG_IN_SUBCOMPONENT);
  }

  @Test
  public void testParentArgsInSubcomponent() throws Exception {
    assertError(
      "<%def foo>\n<%xargs></%def>", 2, 1, AbstractBodyParser.PARENT_ARGS_TAG_IN_SUBCOMPONENT);
  }

  @Test
  public void testEscapeTagInSubcomponent() throws Exception {
    assertError(
      "<%def foo>\n<%escape #u></%def>", 2, 1, AbstractBodyParser.ESCAPE_TAG_IN_SUBCOMPONENT);
  }

  @Test
  public void testGenericTagInSubcomponent() throws Exception {
    assertError(
      "<%def foo>\n<%generic></%def>", 2, 1, AbstractBodyParser.GENERIC_TAG_IN_SUBCOMPONENT);
  }

  @Test
  public void testAnnotateTagInSubcomponent() throws Exception {
    assertError(
      "<%def foo>\n<%annotate></%def>", 2, 1, AbstractBodyParser.ANNOTATE_TAG_IN_SUBCOMPONENT);
  }

  @Test
  public void testImplementMissingSemi() throws Exception {
    assertError("<%implements>\nfoo.bar\n</%implements>", 3, 1, TopLevelParser.EXPECTING_SEMI);
  }

  @Test
  public void testMalformedImplementsOpen() throws Exception {
    assertError("<%implements foo>", 1, 1, AbstractParser.MALFORMED_TAG_ERROR);
  }

  @Test
  public void testMalformedImplementsClose() throws Exception {
    assertError("<%implements>\n<foo", 2, 1, TopLevelParser.EXPECTING_IMPLEMENTS_CLOSE);
  }

  @Test
  public void testMalformedImportsOpen() throws Exception {
    assertError("<%import foo>", 1, 1, AbstractParser.MALFORMED_TAG_ERROR);
  }

  @Test
  public void testMalformedImportsClose() throws Exception {
    assertError("<%import>\n<foo", 2, 1, TopLevelParser.EXPECTING_IMPORTS_CLOSE);
  }

  @Test
  public void testMalformedParentArgsClose() throws Exception {
    assertError("<%xargs>\n</%>", 2, 1, ParentArgsParser.MALFORMED_PARENT_ARGS_CLOSE);
  }

  @Test
  public void testMalformedWhileTag() throws Exception {
    assertError("<%while>", 1, 1, "Malformed <%while ...%> tag");
  }

  @Test
  public void testMalformedWhileCondition() throws Exception {
    assertError("<%while foo>", 1, 1, "Reached end of file while reading <%while ...%> tag");
  }

  @Test
  public void testElseWithoutIf() throws Exception {
    assertError("<%else>", 1, 1, AbstractBodyParser.ENCOUNTERED_ELSE_TAG_WITHOUT_PRIOR_IF_TAG);
  }

  @Test
  public void testElseIfWithoutIf() throws Exception {
    assertError(
      "<%elseif foo%>>", 1, 1, AbstractBodyParser.ENCOUNTERED_ELSEIF_TAG_WITHOUT_PRIOR_IF_TAG);
  }

  @Test
  public void testMultipleElseTags() throws Exception {
    assertError(
      "<%if foo%><%else>\n<%else></%if>",
      2, 1, IfParser.ENCOUNTERED_MULTIPLE_ELSE_TAGS_FOR_ONE_IF_TAG);
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(ParserErrorsTest.class);
  }
}
