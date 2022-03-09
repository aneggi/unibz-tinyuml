/**
 * Copyright 2007 Wei-ju Wu
 *
 * This file is part of TinyUML.
 *
 * TinyUML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * TinyUML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TinyUML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.tinyuml.ui;

import java.awt.Dimension;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * A toolbar manager for the sequence diagram editor.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class SequenceEditorToolbarManager extends AbstractToolbarManager {

  private ButtonGroup buttongroup;

  /**
   * Constructor.
   */
  public SequenceEditorToolbarManager() {
    buttongroup = new ButtonGroup();
    JToolBar toolbar = getToolbar();
    toolbar.add(createToggleButtonWithName(buttongroup, "select"));
    toolbar.add(createToggleButtonWithName(buttongroup, "lifeline"));
    toolbar.addSeparator(new Dimension(10, 10));
    toolbar.add(createToggleButtonWithName(buttongroup, "message"));
    toolbar.addSeparator(new Dimension(10, 10));
    toolbar.add(createToggleButtonWithName(buttongroup, "note"));
    toolbar.add(createToggleButtonWithName(buttongroup, "noteconnector"));
    doClick("SELECT_MODE");
  }

  /**
   * Creates the specified toggle button.
   * @param aButtonGroup an optional ButtonGroup to add to
   * @param name the toggle button name
   * @return the toggle button
   */
  private JToggleButton createToggleButtonWithName(ButtonGroup aButtonGroup,
    String name) {
    return createToggleButton(aButtonGroup, "sequencetoolbar." + name);
  }
}
