package com.statikk.auth.repository;

import com.statikk.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth_repo_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop",
        "spring.flyway.enabled=false"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save user and find by email and username")
    void shouldSaveAndFindUser() {
        User user = User.builder()
                .username("deft")
                .email("deft@drx.gg")
                .password("hashed_password")
                .role("ROLE_USER")
                .enabled(true)
                .build();

        userRepository.save(user);

        Optional<User> byEmail = userRepository.findByEmail("deft@drx.gg");
        assertThat(byEmail).isPresent();
        assertThat(byEmail.get().getUsername()).isEqualTo("deft");

        assertThat(userRepository.existsByEmail("deft@drx.gg")).isTrue();
        assertThat(userRepository.existsByUsername("deft")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@drx.gg")).isFalse();
    }
}
