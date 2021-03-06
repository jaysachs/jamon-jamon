/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.integration;

import test.jamon.For;
import test.jamon.PrimitiveIf;
import test.jamon.While;

public class ControlFlowTest extends TestBase {
  public void testWhile() throws Exception {
    new While().render(getWriter());
    assertEquals("141516\n242526\n343536\n", getOutput());
  }

  public void testFor() throws Exception {
    new For().render(getWriter());
    assertEquals("141516\n242526\n343536\n", getOutput());
  }

  public void testIf() throws Exception {
    new PrimitiveIf().render(getWriter(), 0);
    assertEquals("i is 0\ni is 0\ni is 0", getOutput());

    resetWriter();

    new PrimitiveIf().render(getWriter(), 1);
    assertEquals("\ni is not 0\ni is positive", getOutput());

    resetWriter();

    new PrimitiveIf().render(getWriter(), -1);
    assertEquals("\ni is not 0\ni is negative", getOutput());
  }
}
