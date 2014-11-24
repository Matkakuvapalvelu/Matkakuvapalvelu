package wadp.controller.utility;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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


    public static void makePost(MockMvc mockMvc, String postUrl, String redirectUrl) throws Exception {
        makePost(mockMvc, postUrl, redirectUrl, new HashMap<>());
    }

    public static void makePost(MockMvc mockMvc, String postUrl, String redirectUrl, Map<String, String> parameters) throws Exception {
        makeRequest(mockMvc, new Post(postUrl), redirectUrl, parameters);
    }


    public static void makeDelete(MockMvc mockMvc, String postUrl, String redirectUrl) throws Exception {
        makeRequest(mockMvc,  new Delete(postUrl), redirectUrl, new HashMap<>());
    }

    private static void makeRequest(MockMvc mockMvc, Callable<MockHttpServletRequestBuilder> func, String redirectUrl, Map<String, String> parameters) throws Exception {
        MockHttpSession session = buildSession();

        // post requires the csrf token
        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        CsrfToken csrfToken =  httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

        Map<String, Object> map = new HashMap<>();
        map.put("org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN",
                csrfToken);


        MockHttpServletRequestBuilder builder = func.call()
                .session(session);

        builder.param("_csrf", csrfToken.getToken());

        for (String key : parameters.keySet()) {
            builder.param(key, parameters.get(key));
        }

        mockMvc.perform(builder
                .sessionAttrs(map))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectUrl));
    }

}
