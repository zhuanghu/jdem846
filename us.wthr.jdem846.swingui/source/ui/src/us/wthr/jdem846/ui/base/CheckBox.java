/*
 * Copyright (C) 2011 Kevin M. Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package us.wthr.jdem846.ui.base;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class CheckBox extends JCheckBox
{

	public CheckBox() {
		super();
	}

	public CheckBox(Action a) {
		super(a);
	}

	public CheckBox(Icon icon, boolean selected) {
		super(icon, selected);
	}

	public CheckBox(Icon icon) {
		super(icon);
	}

	public CheckBox(String text, boolean selected) {
		super(text, selected);
	}

	public CheckBox(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
	}

	public CheckBox(String text, Icon icon) {
		super(text, icon);
	}

	public CheckBox(String text) {
		super(text);
	}

}
