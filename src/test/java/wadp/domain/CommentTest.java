
package wadp.domain;
import org.junit.Test;

import static org.junit.Assert.*;


public class CommentTest {

    @Test
    public void commentIsVisibleAtCreation() {
        Comment comment = new Comment();
        assertTrue(comment.isVisible());
    }

}
