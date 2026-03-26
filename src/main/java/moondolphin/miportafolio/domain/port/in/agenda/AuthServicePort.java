package moondolphin.miportafolio.domain.port.in.agenda;

public interface AuthServicePort {
    String login(String usernameOrEmail, String password);
    void cambiarPassword(Long userId, String passwordActual, String passwordNueva);
}
