/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.metamodel.facets.object.cached;

import org.apache.isis.core.metamodel.facets.MarkerFacet;

/**
 * Whether the instances of this class are cached.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * member with <tt>@Cached</tt>.
 *
 * <p>
 * Cached does not necessarily imply immutable. The idea though is that the
 * developer is indicating that the performance cost of obtaining all instances
 * of an instance is low; viewer implementations might be able to exploit this
 * information.
 *
 * <p>
 * Not yet implemented by any viewer.
 */
public interface CachedFacet extends MarkerFacet {

}
