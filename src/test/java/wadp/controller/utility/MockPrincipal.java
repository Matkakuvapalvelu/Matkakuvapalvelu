package wadp.controller.utility;


import java.security.Principal;

public class MockPrincipal implements Principal {

    public MockPrincipal(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object another) {
        if (another == null || !(another instanceof MockPrincipal)) {
            return false;
        }

        if (this == another) {
            return true;
        }

        return name.equals(((MockPrincipal)another).getName());

    }

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String getName() {
        return name;
    }

    private String name;
};
