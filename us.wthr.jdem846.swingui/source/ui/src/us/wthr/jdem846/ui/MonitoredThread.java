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

import java.util.LinkedList;
import java.util.List;

public class MonitoredThread extends Thread
{
	private List<ProgressListener> progressListeners = new LinkedList<ProgressListener>();
	
	
	
	public void addProgressListener(ProgressListener listener)
	{
		progressListeners.add(listener);
	}
	
	public void removeProgressListener(ProgressListener listener)
	{
		progressListeners.add(listener);
	}
	
	protected void fireOnStartListeners()
	{
		for (ProgressListener listener : progressListeners) {
			listener.onStart();
		}
	}
	
	protected void fireProgressListeners(double progress)
	{
		for (ProgressListener listener : progressListeners) {
			listener.onProgress(progress);
		}
	}
	
	protected void fireOnCompleteListeners()
	{
		for (ProgressListener listener : progressListeners) {
			listener.onComplete();
		}
	}
	
	public interface ProgressListener
	{
		public void onStart();
		public void onProgress(double progress);
		public void onComplete();
	}
	
}
