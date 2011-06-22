/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.restful.viewer2.resources.objects;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.restful.applib2.resources.ObjectResource;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.resources.ResourceAbstract;
import org.apache.isis.viewer.restful.viewer2.resources.objects.DomainObjectRep.Builder;
import org.apache.isis.viewer.restful.viewer2.util.UrlDecoderUtils;

@Path("/objects")
public class DomainObjectResourceImpl extends ResourceAbstract implements ObjectResource {

    @GET
    @Path("/{oid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String object(@PathParam("oid") final String oidEncodedStr) {
        init();
        final String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        if (objectAdapter == null) {
            throw new WebApplicationException(responseOfGone("could not determine object"));
        }

        Builder builder = DomainObjectRep.newBuilder(getResourceContext().repContext(), objectAdapter);
        
        List<ObjectAssociation> properties = objectAdapter.getSpecification().getAssociations(ObjectAssociationFilters.PROPERTIES);
        for (ObjectAssociation otoa : properties) {
            Consent visibility = otoa.isVisible(getSession(), objectAdapter);
            if(visibility.isAllowed()) {
                String id = otoa.getId();
                PropertyRep propertyRep = PropertyRep.newBuilder(getResourceContext().repContext(id), objectAdapter, (OneToOneAssociation)otoa).build();
                builder.withProperty(id, propertyRep);
            }
        }
        
        DomainObjectRep representation = builder.build();
        return asJson(representation);
    }

    @PUT
    @Path("/{oid}/property/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String modifyProperty(@PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId, @QueryParam("proposedValue") final String proposedValue) {
        return null;
    }

    @DELETE
    @Path("/{oid}/property/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String clearProperty(@PathParam("oid") final String oidStr, @PathParam("propertyId") final String propertyId){
        return null;
    }

    @GET
    @Path("/{oid}/collection/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String accessCollection(@PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId){
        return null;
    }

    @PUT
    @Path("/{oid}/collection/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String addToCollection(@PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @QueryParam("proposedValue") final String proposedValueOidStr){
        return null;
    }

    @DELETE
    @Path("/{oid}/collection/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String removeFromCollection(@PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @QueryParam("proposedValue") final String proposedValueOidStr){
        return null;
    }

    @POST
    @Path("/{oid}/action/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String invokeAction(@PathParam("oid") final String oidStr, @PathParam("actionId") final String actionId,
        final InputStream body){
        return null;
    }

}
