package wadp.controller.utility;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.Callable;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class Delete implements Callable<MockHttpServletRequestBuilder> {
    private final String url;

    public Delete(String url) {
        this.url = url;
    }

    public MockHttpServletRequestBuilder call() throws Exception {
        return delete(url);
    }
}
