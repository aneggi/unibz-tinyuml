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
package org.tinyuml.umldraw.shared;

import org.tinyuml.draw.SimpleConnection;
import org.tinyuml.model.Relation;
import org.tinyuml.model.UmlModelElement;

/**
 * This class implements a connection with a Note.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public final class NoteConnection extends BaseConnection {

  private static final long serialVersionUID = 5536230656063402250L;
  private static NoteConnection prototype;

  /**
   * Returns the prototype instance for this Connection.
   * @return the prototype instance
   */
  public static NoteConnection getPrototype() {
    if (prototype == null) prototype = new NoteConnection();
    return prototype;
  }

  /**
   * Constructor.
   */
  private NoteConnection() {
    setConnection(new SimpleConnection());
    setIsDashed(true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRelation(Relation aRelation) { }

  /**
   * {@inheritDoc}
   */
  @Override
  public UmlModelElement getModelElement() { return null; }
}
