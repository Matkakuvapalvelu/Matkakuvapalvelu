package wadp.controller.utility;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ControllerTestHelpers {

    // create mock request so that user is authenticated correctly
    public static MockHttpSession buildSession() {
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

    public static MvcResult makeGet(MockMvc mockMvc, String getUrl, String viewName, String... expectedModelValueNames) throws Exception {
        MockHttpSession session = buildSession();
        return mockMvc
                .perform(get(getUrl)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(expectedModelValueNames))
                .andExpect(view().name(viewName))
                .andReturn();
    }


    public static void makePost(MockMvc mockMvc, String postUrl, String redirectUrl) throws Exception {
        makePost(mockMvc, postUrl, redirectUrl, new HashMap<>());
    }

    public static void makePost(MockMvc mockMvc, String postUrl, String redirectUrl, Map<String, String> parameters) throws Exception {
        makeRequest(mockMvc, new Post(postUrl), redirectUrl, parameters);
    }


    public static void makeDelete(MockMvc mockMvc, String postUrl, String redirectUrl) throws Exception {
        makeRequest(mockMvc, new Delete(postUrl), redirectUrl, new HashMap<>());
    }


    public static MvcResult makePostWithFile(MockMvc mockMvc,
                                        String url,
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


    private static void makeRequest(MockMvc mockMvc, Callable<MockHttpServletRequestBuilder> func, String redirectUrl, Map<String, String> parameters) throws Exception {
        MockHttpSession session = buildSession();

        MockHttpServletRequestBuilder builder = func.call()
                .session(session);

        Map<String, Object> map = initializeCsrfToken(builder);
        setPostParameters(parameters, builder);

        doRequest(mockMvc, redirectUrl, builder, map);
    }

    private static void doRequest(MockMvc mockMvc, String redirectUrl, MockHttpServletRequestBuilder builder, Map<String, Object> map) throws Exception {
        mockMvc.perform(builder
                .sessionAttrs(map))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectUrl));
    }

    private static Map<String, Object> initializeCsrfToken(MockHttpServletRequestBuilder builder) {
        // post requires the csrf token
        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        CsrfToken csrfToken =  httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

        Map<String, Object> map = new HashMap<>();
        map.put("org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN",
                csrfToken);

        builder.param("_csrf", csrfToken.getToken());
        return map;
    }

    private static void setPostParameters(Map<String, String> parameters, MockHttpServletRequestBuilder builder) {
        for (String key : parameters.keySet()) {
            builder.param(key, parameters.get(key));
        }
    }

}
