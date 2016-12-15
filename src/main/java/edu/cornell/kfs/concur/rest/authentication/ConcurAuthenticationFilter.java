package edu.cornell.kfs.concur.rest.authentication;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.service.ConcurAuthenticationService;

public class ConcurAuthenticationFilter implements Filter {

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String authorizationHeader = httpServletRequest.getHeader(ConcurConstants.AUTHORIZATION_PROPERTY);

        if (StringUtils.isBlank(authorizationHeader)) {          
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            final String encodedUserNameAndPasswordAsToken = authorizationHeader.replaceFirst(ConcurConstants.BASIC_AUTHENTICATION_SCHEME + " ", "");

            if (!getConcurAuthenticationService().isConcurTokenValid(encodedUserNameAndPasswordAsToken)) {
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    private ConcurAuthenticationService getConcurAuthenticationService() {
        return SpringContext.getBean(ConcurAuthenticationService.class);
    }

}
