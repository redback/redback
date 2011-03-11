package org.codehaus.plexus.redback.struts2.filter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CSRF filter that checks for the nonce in the request and verifies it against the values in the cache. It then adds
 * an encoded nonce to the response. Patterned after Tomcat's CSRFPreventionFilter.
 * 
 * The following are configurable, depending on what your application prefers:
 * 1. nonceCacheSize - number of nonce values to be stored before being cleared. Default value is <i>6</i>
 * 2. randomClass - Random impl that will be used to generate the nonce. Default value is <i>java.security.SecureRandom</i>
 * 3. excludedPaths - paths that would be won't be checked for the nonce. The following formats are accepted:
 *    - /path/** (all paths that match this pattern will be allowed if and only if the requested path has a
 *                file extension of: .png, .jpg, .gif, .css, .js, .ftl and .ico since such requests also go through the
 *                filters when rendering the page)
 *    - //path/** (all paths that match this pattern will be allowed)
 *
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @plexus.component role="javax.servlet.Filter" role-hint="csrf"
 */
public class RedbackCSRFFilter
    implements Filter
{
    private Logger log = LoggerFactory.getLogger( RedbackCSRFFilter.class );

    private String randomClass;

    private Random randomizer;

    private int nonceCacheSize = 6;

    private List<String> excludedPaths = new ArrayList<String>();

    static final String CSRF_NONCE_SESSION_ATTR_NAME = "csrf_nonce_cache";

    static final String CSRF_NONCE_REQUEST_PARAM = "csrf_nonce";

    static final String PARAM_NONCE_CACHE_SIZE = "nonceCacheSize";

    static final String PARAM_RANDOM_CLASS = "randomClass";

    static final String PARAM_EXCLUDED_PATHS = "excludedPaths";

    static final String EXCLUDED_PATHS_SEPARATOR = ",";

    static final String HTTP_GET_METHOD = "GET";

    private static final Pattern extensionPattern =
        Pattern.compile( "(\\.jpg$)|(\\.png$)|(\\.gif$)|(\\.css$)|(\\.js$)|(\\.ftl$)|(\\.ico$)", Pattern.CASE_INSENSITIVE );

    /**
     *
     * @param filterConfig
     * @throws ServletException
     */
    public void init( FilterConfig filterConfig )
        throws ServletException
    {

        // set configured cache size
        if( !StringUtils.isBlank( filterConfig.getInitParameter( PARAM_NONCE_CACHE_SIZE ) ) )
        {
            nonceCacheSize = Integer.parseInt( filterConfig.getInitParameter( PARAM_NONCE_CACHE_SIZE ) );
        }

        // set configured excluded urls
        if( !StringUtils.isBlank( filterConfig.getInitParameter( PARAM_EXCLUDED_PATHS ) ) )
        {
            String[] urls = StringUtils.splitByWholeSeparator(
                filterConfig.getInitParameter( PARAM_EXCLUDED_PATHS ), EXCLUDED_PATHS_SEPARATOR );
            
            excludedPaths = new ArrayList<String>( Arrays.asList( urls ) );
        }

        // set configured Random impl to be used
        randomClass = filterConfig.getInitParameter( PARAM_RANDOM_CLASS );

        if( StringUtils.isBlank( randomClass ) )
        {
            randomClass = "java.security.SecureRandom";
        }

        try
        {
            randomizer = ( Random ) ( Class.forName( randomClass ) ).newInstance();
        }
        catch( ClassNotFoundException e )
        {
            throw new ServletException( "Class '" + randomClass + "' for generating random tokens to prevent CSRF attacks not found.", e );
        }
        catch( InstantiationException e )
        {
            throw new ServletException( "Failed to instantiate class '" + randomClass + "' for generating random tokens to " +
                "prevent CSRF attacks.", e );
        }
        catch( IllegalAccessException e )
        {
            throw new ServletException( "Illegal access error encountered when instantiating class '" + randomClass + "' for generating " +
                "random token to prevent CSRF attacks.", e );
        }
    }

    /**
     * Filter requests. If nonce isn't in the cache, return SC_FORBIDDEN error. Add nonce to each response which will be used on
     * for verification in succeeding requests.
     *
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
        throws IOException, ServletException
    {
        ServletResponse wResponse;

        if( servletRequest instanceof HttpServletRequest &&  servletResponse instanceof HttpServletResponse )
        {
            HttpServletRequest req = ( HttpServletRequest ) servletRequest;
            HttpServletResponse res = ( HttpServletResponse ) servletResponse;

            boolean skipNonceCheck = false;
            boolean storeInCache = true;

            if ( HTTP_GET_METHOD.equals( req.getMethod() ) ) 
            {
                String path = req.getServletPath() + ( req.getPathInfo() != null ? req.getPathInfo() : "" );
                String[] paths = StringUtils.splitByWholeSeparator( path, "/" );

                String tmp = "";
                boolean alreadyFound = false;
                for( int i = 0; i < paths.length; i++ )
                {
                    tmp = tmp + "/" + paths[i] ;

                    // paths with two leading '/' means that the exception shouldn't just be limited to those
                    //      which ends in the values defined in extensionsPattern above (e.g. css, images and js files)
                    if( excludedPaths.contains( tmp + "/**" ) )
                    {
                        String requestedFile = StringUtils.substring( path, path.lastIndexOf( "/" ) + 1 );

                        // allow this only if the request is an image, css or js
                        Matcher mat = extensionPattern.matcher( requestedFile );
                        if( mat.find() )
                        {
                            log.debug( "Found a match in extensionPattern." );
                            skipNonceCheck = true;

                            // do not store in cache if request matches any of the file types defined in extension pattern
                            storeInCache = false;
                        }

                        alreadyFound = true;
                        break;
                    }
                    else if ( excludedPaths.contains( "/" + tmp + "/**" ) )
                    {
                        // allow all requests (specifically for handling repository browse in Archiva)
                        alreadyFound = true;
                        skipNonceCheck = true;
                        storeInCache = false;

                        break;
                    }
                }

                if ( !alreadyFound && excludedPaths.contains( path ) )
                {
                    skipNonceCheck = true;
                }
            }

            log.debug( "Skip nonceCheck :: " + skipNonceCheck );

            @SuppressWarnings("unchecked")
            LruCache<String> nonceCache = ( LruCache<String> ) req.getSession( true ).getAttribute( CSRF_NONCE_SESSION_ATTR_NAME );

            if( !skipNonceCheck )
            {
                String previousNonce = req.getParameter( CSRF_NONCE_REQUEST_PARAM );

                log.debug( "Nonce found in request: " + previousNonce );

                String baseUrl = req.getScheme() + "://" + req.getServerName() + ( req.getServerPort() > 0 ? ":" +
                    req.getServerPort() : "" ) + "/" +  StringUtils.stripStart( req.getContextPath(), "/" );

                log.debug( "Calculated base URL :: " + baseUrl );

                String requestURL = req.getRequestURL().toString();
                log.debug( "Request URL :: " + requestURL );
               
                //clear the nonce cache if url requested is the baseurl
                if( baseUrl.equalsIgnoreCase( requestURL ) ||
                    ( baseUrl + "/" ).equalsIgnoreCase( requestURL ) )
                {
                    log.debug( "Requested URL is the base URL. Setting nonceCache to null to reset it." );
                    nonceCache = null;
                }

                if ( nonceCache != null && !nonceCache.contains( decodeNonce( previousNonce ) ) )
                {
                    log.error( "Nonce not found in nonceCache! Request forbidden." );

                    log.debug( "nonceCache keys:" );
                    Set<String> keys = nonceCache.getKeys();
                    for( String key : keys )
                    {
                        log.debug( Base64.encodeBase64String( key.getBytes() ) );
                    }

                    res.sendError( HttpServletResponse.SC_FORBIDDEN );
                    return;
                }

                if ( nonceCache == null )
                {
                    nonceCache = new LruCache<String>( nonceCacheSize );
                    req.getSession().setAttribute( CSRF_NONCE_SESSION_ATTR_NAME, nonceCache );
                }
            }

            String newNonce = generateEncodedNonce();

            log.debug( "Generated encoded nonce: " + newNonce );
            
            if( storeInCache )
            {
                // store decodedNonce but set the encoded one in the response
                nonceCache.add( decodeNonce( newNonce ) );
            }

            wResponse = new RedbackCSRFResponseWrapper( res, newNonce  );
	 	}
        else
        {
            wResponse = servletResponse;
        }

        filterChain.doFilter( servletRequest, wResponse );
    }

    protected String generateEncodedNonce()
    {
        String encodedNonce;

        byte[] random =  new byte[16];
        randomizer.nextBytes( random );
        byte[] all = new byte[17];

        for( int i = 0; i < random.length; i++ )
        {
            all[i] = random[i];
        }

        // include time to ensure uniqueness
        byte time = ( byte ) System.currentTimeMillis();
        all[16] = time;

        // encode as string
        encodedNonce = Base64.encodeBase64String( all );

        return encodedNonce;
    }

    protected String decodeNonce( String encodedNonce )
    {
        byte[] nonceInBytes = Base64.decodeBase64( encodedNonce );

        String decodedNonce = "";
        if( nonceInBytes != null )
        {
            decodedNonce = new String( nonceInBytes );
        }

        return decodedNonce;
    }

    public void destroy()
    {
            
    }

    public String getRandomClass()
    {
        return randomClass;
    }

    public void setRandomClass( String randomClass )
    {
        this.randomClass = randomClass;
    }

    public int getNonceCacheSize()
    {
        return nonceCacheSize;
    }

    public void setNonceCacheSize( int nonceCacheSize )
    {
        this.nonceCacheSize = nonceCacheSize;
    }

    public List<String> getExcludedPaths()
    {
        return excludedPaths;
    }

    public void setExcludedPaths( List<String> excludedPaths )
    {
        this.excludedPaths = excludedPaths;
    }
    
    public void setRandomizer( Random randomizer )
    {
        this.randomizer = randomizer;
    }
}
