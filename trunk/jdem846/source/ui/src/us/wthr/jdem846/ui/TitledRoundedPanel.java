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

package us.wthr.jdem846.ui;

import java.awt.Insets;

import javax.swing.JPanel;

import us.wthr.jdem846.ui.base.Panel;
import us.wthr.jdem846.ui.border.StandardTitledBorder;

@SuppressWarnings("serial")
public class TitledRoundedPanel extends Panel
{
	
	public TitledRoundedPanel(String title)
	{
		setBorder(new StandardTitledBorder(title));

	}
	

}