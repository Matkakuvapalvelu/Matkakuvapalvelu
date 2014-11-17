package wadp.service;

public class NofriendshipExistsException extends RuntimeException {
    public NofriendshipExistsException(String what) {
        super(what);
    }
}
