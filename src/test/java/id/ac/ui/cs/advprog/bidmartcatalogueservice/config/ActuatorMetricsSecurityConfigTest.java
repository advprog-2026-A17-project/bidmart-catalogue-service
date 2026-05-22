package id.ac.ui.cs.advprog.bidmartcatalogueservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActuatorMetricsSecurityConfigTest {

    @Test
    void prometheusSecurity_WithoutCredentials_ConfiguresSuccessfully() throws Exception {
        ActuatorMetricsSecurityConfig config = new ActuatorMetricsSecurityConfig();
        HttpSecurity http = mock(HttpSecurity.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        
        when(http.build()).thenReturn(mock(org.springframework.security.web.DefaultSecurityFilterChain.class));

        SecurityFilterChain chain = config.prometheusSecurity(http, "", "");
        assertNotNull(chain);
    }

    @Test
    void prometheusSecurity_WithCredentials_ConfiguresSuccessfully() throws Exception {
        ActuatorMetricsSecurityConfig config = new ActuatorMetricsSecurityConfig();
        HttpSecurity http = mock(HttpSecurity.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        when(http.build()).thenReturn(mock(org.springframework.security.web.DefaultSecurityFilterChain.class));

        SecurityFilterChain chain = config.prometheusSecurity(http, "admin", "password123");
        assertNotNull(chain);
    }

    @Test
    void permitAllSecurity_ConfiguresSuccessfully() throws Exception {
        ActuatorMetricsSecurityConfig config = new ActuatorMetricsSecurityConfig();
        HttpSecurity http = mock(HttpSecurity.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        when(http.build()).thenReturn(mock(org.springframework.security.web.DefaultSecurityFilterChain.class));

        SecurityFilterChain chain = config.permitAllSecurity(http);
        assertNotNull(chain);
    }
}
