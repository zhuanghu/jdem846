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

package us.wthr.jdem846.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Identifies a service class.
 * 
 * @author Kevin M. Gill
 *
 */
@Discoverable
@Retention(RetentionPolicy.RUNTIME)
public @interface Service
{
	/** A unique name for the service.
	 * 
	 * @return
	 */
	String name();
	
	/** Specifies whether the service thread should be configured as a deamon.
	 * 
	 * @return
	 */
	boolean deamon() default true;
	
	/** Specifies whether the service class is enable and should be activated during runtime.
	 * 
	 * @return
	 */
	boolean enabled() default true;
}