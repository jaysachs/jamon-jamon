/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.api.Location;

public class DefCallStatement extends AbstractInnerUnitCallStatement {
  DefCallStatement(
    String path, ParamValues params, DefUnit defUnit, Location location, String templateIdentifier) {
    super(path, params, defUnit, location, templateIdentifier);
  }

  @Override
  protected String getDefault(OptionalArgument arg) {
    return arg.getDefault();
  }
}
