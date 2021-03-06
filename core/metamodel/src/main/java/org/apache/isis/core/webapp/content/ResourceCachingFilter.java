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

package org.apache.isis.core.webapp.content;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.commons.internal.exceptions._Exceptions.FluentException;

/**
 * Adapted from {@link http
 * ://www.digitalsanctuary.com/tech-blog/java/jboss/setting
 * -cache-headers-from-jboss.html}
 *
 * <p>
 * Usage:
 *
 * <pre>
 * &lt;filter>
 *   &lt;filter-name>ResourceCachingFilter&lt;/filter-name>
 *   &lt;filter-class>org.apache.isis.core.webapp.content.ResourceCachingFilter&lt;/filter-class>
 *   &lt;init-param>
 *     &lt;param-name>CacheTime&lt;/param-name>
 *     &lt;param-value>86400&lt;/param-value>
 *   &lt;/init-param>
 * &lt;/filter>
 * ...
 * &lt;filter-mapping>
 *   &lt;filter-name>ResourceCachingFilter&lt;/filter-name>
 *   &lt;url-pattern>*.css&lt;/url-pattern>
 * &lt;/filter-mapping>
 * &lt;filter-mapping>
 *   &lt;filter-name>ResourceCachingFilter&lt;/filter-name>
 *   &lt;url-pattern>*.png&lt;/url-pattern>
 * &lt;/filter-mapping>
 * &lt;filter-mapping>
 *   &lt;filter-name>ResourceCachingFilter&lt;/filter-name>
 *   &lt;url-pattern>*.jpg&lt;/url-pattern>
 * &lt;/filter-mapping>
 * &lt;filter-mapping>
 *   &lt;filter-name>ResourceCachingFilter&lt;/filter-name>
 *   &lt;url-pattern>*.jpeg&lt;/url-pattern>
 * &lt;/filter-mapping>
 * &lt;filter-mapping>
 *   &lt;filter-name>ResourceCachingFilter&lt;/filter-name>
 *   &lt;url-pattern>*.gif&lt;/url-pattern>
 * &lt;/filter-mapping>
 * &lt;filter-mapping>
 *   &lt;filter-name>ResourceCachingFilter&lt;/filter-name>
 *   &lt;url-pattern>*.svg&lt;/url-pattern>
 * &lt;/filter-mapping>
 * &lt;filter-mapping>
 *   &lt;filter-name>ResourceCachingFilter&lt;/filter-name>
 *   &lt;url-pattern>*.js&lt;/url-pattern>
 * &lt;/filter-mapping>
 * &lt;filter-mapping>
 *   &lt;filter-name>ResourceCachingFilter&lt;/filter-name>
 *   &lt;url-pattern>*.html&lt;/url-pattern>
 * &lt;/filter-mapping>
 * &lt;filter-mapping>
 *   &lt;filter-name>ResourceCachingFilter&lt;/filter-name>
 *   &lt;url-pattern>*.swf&lt;/url-pattern>
 * &lt;/filter-mapping>
 * </pre>
 */
public class ResourceCachingFilter implements Filter {

    /**
     * Attribute set on {@link HttpServletRequest} if the filter has been
     * applied.
     *
     * <p>
     * This is intended to inform other filters.
     */
    private static final String REQUEST_ATTRIBUTE = ResourceCachingFilter.class.getName() + ".resource";

    /**
     * To allow other filters to ask whether a request is mapped to the resource
     * caching filter.
     *
     * <p>
     * For example, the <tt>IsisSessionFilter</tt> uses this in order to skip
     * any session handling.
     */
    public static boolean isCachedResource(final HttpServletRequest request) {
        return request.getAttribute(REQUEST_ATTRIBUTE) != null;
    }

    /**
     * The Constant MILLISECONDS_IN_SECOND.
     */
    private static final int MILLISECONDS_IN_SECOND = 1000;

    /** The Constant POST_CHECK_VALUE. */
    private static final String POST_CHECK_VALUE = "post-check=";

    /** The Constant PRE_CHECK_VALUE. */
    private static final String PRE_CHECK_VALUE = "pre-check=";

    /** The Constant MAX_AGE_VALUE. */
    private static final String MAX_AGE_VALUE = "max-age=";

    /** The Constant ZERO_STRING_VALUE. */
    private static final String ZERO_STRING_VALUE = "0";

    /** The Constant NO_STORE_VALUE. */
    private static final String NO_STORE_VALUE = "no-store";

    /** The Constant NO_CACHE_VALUE. */
    private static final String NO_CACHE_VALUE = "no-cache";

    /** The Constant PRAGMA_HEADER. */
    private static final String PRAGMA_HEADER = "Pragma";

    /** The Constant CACHE_CONTROL_HEADER. */
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";

    /** The Constant EXPIRES_HEADER. */
    private static final String EXPIRES_HEADER = "Expires";

    /** The Constant LAST_MODIFIED_HEADER. */
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";

    /** The Constant CACHE_TIME_PARAM_NAME. */
    private static final String CACHE_TIME_PARAM_NAME = "CacheTime";

    /** The default for {@link #CACHE_TIME_PARAM_NAME}. */
    private static final String CACHE_TIME_PARAM_NAME_DEFAULT = "" + 86400;

    /** The reply headers. */
    private String[][] mReplyHeaders = { {} };

    /** The cache time in seconds. */
    private Long cacheTime = 0L;

    /**
     * Initializes the Servlet filter with the cache time and sets up the
     * unchanging headers.
     *
     * @param pConfig
     *            the config
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(final FilterConfig pConfig) {
        final ArrayList<String[]> newReplyHeaders = new ArrayList<String[]>();
        final String cacheTime = pConfig.getInitParameter(CACHE_TIME_PARAM_NAME);
        this.cacheTime = Long.parseLong(cacheTime != null ? cacheTime : CACHE_TIME_PARAM_NAME_DEFAULT);
        if (this.cacheTime > 0L) {
            newReplyHeaders.add(new String[] { CACHE_CONTROL_HEADER, MAX_AGE_VALUE + this.cacheTime.longValue() });
            newReplyHeaders.add(new String[] { CACHE_CONTROL_HEADER, PRE_CHECK_VALUE + this.cacheTime.longValue() });
            newReplyHeaders.add(new String[] { CACHE_CONTROL_HEADER, POST_CHECK_VALUE + this.cacheTime.longValue() });
        } else {
            newReplyHeaders.add(new String[] { PRAGMA_HEADER, NO_CACHE_VALUE });
            newReplyHeaders.add(new String[] { EXPIRES_HEADER, ZERO_STRING_VALUE });
            newReplyHeaders.add(new String[] { CACHE_CONTROL_HEADER, NO_CACHE_VALUE });
            newReplyHeaders.add(new String[] { CACHE_CONTROL_HEADER, NO_STORE_VALUE });
        }
        this.mReplyHeaders = new String[newReplyHeaders.size()][2];
        newReplyHeaders.toArray(this.mReplyHeaders);
    }

    /**
     * Do filter.
     *
     * @param servletRequest
     *            the request
     * @param servletResponse
     *            the response
     * @param chain
     *            the chain
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ServletException
     *             the servlet exception
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain) throws IOException, ServletException {
        // Apply the headers
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        for (final String[] replyHeader : this.mReplyHeaders) {
            final String name = replyHeader[0];
            final String value = replyHeader[1];
            httpResponse.addHeader(name, value);
        }
        if (this.cacheTime > 0L) {
            final long now = System.currentTimeMillis();
            final DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            httpResponse.addHeader(LAST_MODIFIED_HEADER, httpDateFormat.format(new Date(now)));
            httpResponse.addHeader(EXPIRES_HEADER, httpDateFormat.format(new Date(now + (this.cacheTime.longValue() * MILLISECONDS_IN_SECOND))));
        }
        httpRequest.setAttribute(REQUEST_ATTRIBUTE, true);

        // try to suppress java.io.IOException of kind 'client connection abort'
        // 1) the TCP protocol (by design) does not provide a means to check, whether a
        //    connection has been closed by the client
        // 2) the exception thrown and the exception message text are specific to the
        //    servlet-engine implementation, so we can only guess here
        try {
            chain.doFilter(servletRequest, servletResponse);
        } catch (IOException e) {
            FluentException.of(e)
            .suppressIf(this::isConnectionAbortException);
        }
    }

    /**
     * Destroy all humans!
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

    // -- HELPER

    private boolean isConnectionAbortException(IOException e) {
        // tomcat 9
        if(e.getMessage().contains("An established connection was aborted by the software in your host machine")) {
            return true;
        }
        // payara 4
        if(e.getMessage().contains("Connection is closed")) {
            return true;
        }

        return false;
    }

}
