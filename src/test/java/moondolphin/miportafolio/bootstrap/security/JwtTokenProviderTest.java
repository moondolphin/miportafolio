package moondolphin.miportafolio.bootstrap.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "agenda-mislun-clave-secreta-2026-minimo-32-caracteres-ok";
    private static final long EXPIRATION = 86400000L;

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    // --- generarToken ---

    @Test
    void generarToken_retornaTokenNoNulo() {
        // Act
        String token = provider.generarToken("yesica", "USER", 1L);

        // Assert
        assertThat(token).isNotNull().isNotBlank();
    }

    // --- obtenerUsername ---

    @Test
    void obtenerUsername_retornaElUsernameCorrectoDelToken() {
        // Arrange
        String token = provider.generarToken("yesica", "USER", 1L);

        // Act
        String username = provider.obtenerUsername(token);

        // Assert
        assertThat(username).isEqualTo("yesica");
    }

    // --- obtenerRole ---

    @Test
    void obtenerRole_retornaElRoleCorrectoDelToken() {
        // Arrange
        String token = provider.generarToken("yesica", "ADMIN", 1L);

        // Act
        String role = provider.obtenerRole(token);

        // Assert
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void obtenerRole_distincionEntreRoles_userVsAdmin() {
        // Arrange
        String tokenUser = provider.generarToken("u1", "USER", 1L);
        String tokenAdmin = provider.generarToken("u2", "ADMIN", 2L);

        // Act + Assert
        assertThat(provider.obtenerRole(tokenUser)).isEqualTo("USER");
        assertThat(provider.obtenerRole(tokenAdmin)).isEqualTo("ADMIN");
    }

    // --- obtenerUserId ---

    @Test
    void obtenerUserId_retornaElIdCorrectoDelToken() {
        // Arrange
        String token = provider.generarToken("yesica", "USER", 42L);

        // Act
        Long userId = provider.obtenerUserId(token);

        // Assert
        assertThat(userId).isEqualTo(42L);
    }

    // --- esValido ---

    @Test
    void esValido_conTokenBienFormado_retornaTrue() {
        // Arrange
        String token = provider.generarToken("yesica", "USER", 1L);

        // Act + Assert
        assertThat(provider.esValido(token)).isTrue();
    }

    @Test
    void esValido_conTokenMalformado_retornaFalse() {
        // Act + Assert
        assertThat(provider.esValido("esto.no.es.un.jwt")).isFalse();
    }

    @Test
    void esValido_conTokenVacio_retornaFalse() {
        // Act + Assert
        assertThat(provider.esValido("")).isFalse();
    }

    @Test
    void esValido_conTokenExpirado_retornaFalse() {
        // Arrange — expiration -1 genera un token que ya nació vencido
        JwtTokenProvider providerExpirado = new JwtTokenProvider(SECRET, -1L);
        String tokenExpirado = providerExpirado.generarToken("yesica", "USER", 1L);

        // Act + Assert
        assertThat(provider.esValido(tokenExpirado)).isFalse();
    }

    @Test
    void esValido_conTokenFirmadoConOtraClave_retornaFalse() {
        // Arrange — token firmado con una clave diferente no debe ser aceptado
        JwtTokenProvider otraInstancia = new JwtTokenProvider(
                "otra-clave-completamente-distinta-2026-ok!!", EXPIRATION);
        String tokenAjeno = otraInstancia.generarToken("yesica", "USER", 1L);

        // Act + Assert
        assertThat(provider.esValido(tokenAjeno)).isFalse();
    }
}
