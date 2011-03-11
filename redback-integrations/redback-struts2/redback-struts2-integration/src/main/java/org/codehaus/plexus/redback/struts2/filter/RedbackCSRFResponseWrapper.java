package org.codehaus.plexus.redback.struts2.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Copied from Tomcat with the addition of escapeNonce(..) method.
 */
public class RedbackCSRFResponseWrapper
    extends HttpServletResponseWrapper
{
    protected String nonce;

    private Logger log = LoggerFactory.getLogger( RedbackCSRFResponseWrapper.class );

    public RedbackCSRFResponseWrapper( HttpServletResponse response )
    {
        super( response );
    }

    public RedbackCSRFResponseWrapper( HttpServletResponse response, String nonce )
    {
        super( response );
        this.nonce = nonce;
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl( String url )
    {
        return encodeRedirectURL( url );
    }

    @Override
    public String encodeRedirectURL( String url )
    {
        return addNonce( super.encodeRedirectURL( url ) );
    }

    @Override
    @Deprecated
    public String encodeUrl( String url )
    {
        return encodeURL( url );
    }

    @Override
    public String encodeURL( String url )
    {
        return addNonce( super.encodeURL( url ) );
    }

    /**
     * Return the specified URL with the nonce added to the query string.
     *
     * @param url URL to be modified
     */
    protected String addNonce( String url )
    {
        // check if there is already a nonce param present
        while( StringUtils.contains( url, RedbackCSRFFilter.CSRF_NONCE_REQUEST_PARAM ) )
        {
            // strip/remove if already present
            int start = StringUtils.indexOf( url, RedbackCSRFFilter.CSRF_NONCE_REQUEST_PARAM );
            int end = StringUtils.indexOf( url, '&', start );

            String csrfParam = "";
            // it's the last param
            if( end == -1 )
            {
                csrfParam = StringUtils.substring( url, start );
                if( url.charAt( start - 1 ) == '?' )
                {
                    csrfParam = "?" + csrfParam;
                }
                else if( url.charAt( start - 1 ) == '&' )
                {
                    csrfParam = "&" + csrfParam;
                }
            }
            else
            {
                // it's not the last parameter
                // always include the & at the end so there won't be any problems with the separators
                // for the succeeding parameters when the csrf param is stripped
                csrfParam = StringUtils.substring( url, start, end + 1 );
            }

            url = StringUtils.replace( url, csrfParam, "" );
        }

        log.debug( "'" + RedbackCSRFFilter.CSRF_NONCE_REQUEST_PARAM + "' stripped URL :: " + url );

        // append new nonce
        if ( ( url == null ) || ( nonce == null ) )
        {
            return ( url );
        }

        String path = url;
        String query = "";
        String anchor = "";

        int pound = path.indexOf( '#' );
        if ( pound >= 0 )
        {
            anchor = path.substring( pound );
            path = path.substring( 0, pound );
        }

        int question = path.indexOf('?');
        if ( question >= 0 )
        {
            query = path.substring( question );
            path = path.substring( 0, question );
        }

        StringBuilder sb = new StringBuilder( path );
        if ( query.length() >0 )
        {
            sb.append( query );
            sb.append( '&' );
        }
        else
        {
            sb.append( '?' );
        }

        sb.append( RedbackCSRFFilter.CSRF_NONCE_REQUEST_PARAM );
        sb.append( '=' );
        sb.append( escapeNonce( nonce ) );
        sb.append( anchor );

        log.debug( "URL with nonce : " + sb.toString() );

        return ( sb.toString() );
    }

    private String escapeNonce( String nonce )
    {
        String escapedNonce = "";

        escapedNonce = StringUtils.replace( nonce, "+", "%2B" );
        escapedNonce = StringUtils.replace( escapedNonce, "?", "%3F" );
        escapedNonce = StringUtils.replace( escapedNonce, "&", "%26" );
        escapedNonce = StringUtils.replace( escapedNonce, "=", "%3D" );
        escapedNonce = StringUtils.replace( escapedNonce, ",", "%2C" );

        return escapedNonce;
    }
}
