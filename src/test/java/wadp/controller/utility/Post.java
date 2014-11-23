package wadp.controller.utility;


import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.Callable;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class Post implements Callable<MockHttpServletRequestBuilder> {
    private final String url;

    public Post(String url) {
        this.url = url;
    }

    public MockHttpServletRequestBuilder call() throws Exception {
        return post(url);
    }
}