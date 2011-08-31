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
package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.JsonUtils.readJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

public class JsonRepresentationTest_getNull_isNull {

    private JsonRepresentation jsonRepresentation;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(readJson("map.json"));
    }
    
    @Test
    public void forNullValue() throws JsonParseException, JsonMappingException, IOException {
        JsonRepresentation nullValue = jsonRepresentation.getNull("aNull");
        assertThat(nullValue, is(not(nullValue())));
        assertThat(nullValue.isNull(), is(true));
        
        Boolean isNull = jsonRepresentation.isNull("aNull");
        assertThat(isNull, is(true));
    }

    @Test
    public void isNull_forArray() throws JsonParseException, JsonMappingException, IOException {
        Boolean isNull = jsonRepresentation.isNull("anEmptyArray");
        assertThat(isNull, is(false));
    }

    @Test
    public void getNull_forArray() {
        try {
            jsonRepresentation.getNull("anEmptyArray");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'anEmptyArray' (an array) is not the null value"));
        }
    }

    @Test
    public void isNull_forNonExistent() {
        Boolean isNull = jsonRepresentation.isNull("nonExistent");
        assertThat(isNull, is(nullValue())); // ie don't know whether is null
    }

    @Test
    public void getNull_forNonExistent() {
        assertThat(jsonRepresentation.getArray("doesNotExist"), is(nullValue()));
    }

    @Test
    public void isNull_forMap() throws JsonParseException, JsonMappingException, IOException {
        Boolean isNull = jsonRepresentation.isNull("aSubMap");
        assertThat(isNull, is(false));
    }

    @Test
    public void forMap() {
        try {
            jsonRepresentation.getNull("aSubMap");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("'aSubMap' (a map) is not the null value"));
        }
    }
    
}
