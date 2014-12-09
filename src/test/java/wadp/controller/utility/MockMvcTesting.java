package wadp.controller.utility;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;

import javax.xml.transform.Result;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MockMvcTesting {

    private MockMvc mockMvc;

    public MockMvcTesting(WebApplicationContext webAppContext, FilterChainProxy springSecurityFilter) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .addFilter(springSecurityFilter, "/*")
                .build();

        webAppContext.getServletContext().setAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppContext);
    }

    // create mock request so that user is authenticated correctly
    private MockHttpSession buildSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("USER"));

        // have to create principal or Authentication in userservice.getAuthentiatedUser() is null
        // that is, can't juste give username\password
        Principal principal = new MockPrincipal("loginuser");

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, "loginuser", grantedAuths);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return session;
    }

    public MvcResult makeGet(String getUrl, String viewName, String... expectedModelValueNames) throws Exception {
        MockHttpSession session = buildSession();

        return mockMvc
                .perform(get(getUrl)
                        .session(session))
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().attributeExists(expectedModelValueNames))
                .andExpect(view().name(viewName))
                .andReturn();
    }


    public MvcResult makeGetResponseBody(String getUrl, String viewName, String... expectedModelValueNames) throws Exception {
        MockHttpSession session = buildSession();
        return mockMvc
                .perform(get(getUrl)
                        .session(session))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }


    public MvcResult makePost(String postUrl, String redirectUrl) throws Exception {
        return makePost(postUrl, redirectUrl, new HashMap<>());
    }

    public MvcResult makePost(String postUrl, String redirectUrl, Map<String, String> parameters) throws Exception {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(status().is3xxRedirection());
        matchers.add(redirectedUrl(redirectUrl));

        return makeRequest(new Post(postUrl), parameters, matchers);
    }

    public MvcResult makePostExpectErrors(String postUrl,
                                          String view,
                                          Map<String, String> parameters,
                                          String... errors) throws Exception {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(status().is2xxSuccessful());
        matchers.add(model().attributeHasErrors(errors));
        matchers.add(view().name(view));
        return makeRequest(new Post(postUrl), parameters, matchers);

    }

    public MvcResult makeDelete(String postUrl, String redirectUrl) throws Exception {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(status().is3xxRedirection());
        matchers.add(redirectedUrl(redirectUrl));
        return makeRequest(new Delete(postUrl), new HashMap<>(), matchers);
    }


    public MvcResult makePostWithFile(String url,
                                        String redirectUrlPattern,
                                        ResultMatcher matcher,
                                        byte [] data, String type,
                                        Map<String, String> parameters) throws Exception {
        MockHttpSession session = buildSession();

        MockMultipartFile file = new MockMultipartFile("file", "imagefile", type, data);

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.fileUpload(url);
        Map<String, Object> map = initializeCsrfToken(builder);

        setPostParameters(parameters, builder);
        builder.session(session);

        builder.file(file);

        MvcResult result = mockMvc.perform(builder
                .sessionAttrs(map))
                .andExpect(matcher)
                .andReturn();

        if (!redirectUrlPattern.isEmpty()) {
            String location = result.getResponse().getHeader("Location");

            Pattern pattern = Pattern.compile(redirectUrlPattern);
            assertTrue(pattern.matcher(location).find());
        }
        return result;
    }



    private MvcResult makeRequest(Callable<MockHttpServletRequestBuilder> func,
                                  Map<String, String> parameters,
                                  List<ResultMatcher> matchers) throws Exception {
        MockHttpSession session = buildSession();

        MockHttpServletRequestBuilder builder = func.call()
                .session(session);

        Map<String, Object> map = initializeCsrfToken(builder);
        setPostParameters(parameters, builder);

        return doRequest(builder, map, matchers);
    }

    private MvcResult doRequest(MockHttpServletRequestBuilder builder,
                                Map<String, Object> map,
                                List<ResultMatcher> matchers) throws Exception {
        ResultActions actions = mockMvc.perform(builder
                .sessionAttrs(map));

        for (ResultMatcher matcher : matchers) {
            actions = actions.andExpect(matcher);
        }

        return actions.andReturn();
    }

    private Map<String, Object> initializeCsrfToken(MockHttpServletRequestBuilder builder) {
        // post requires the csrf token
        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        CsrfToken csrfToken =  httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

        Map<String, Object> map = new HashMap<>();
        map.put("org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN",
                csrfToken);

        builder.param("_csrf", csrfToken.getToken());
        return map;
    }

    private void setPostParameters(Map<String, String> parameters, MockHttpServletRequestBuilder builder) {
        for (String key : parameters.keySet()) {
            builder.param(key, parameters.get(key));
        }
    }


}
