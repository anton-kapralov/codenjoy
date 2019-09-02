package com.codenjoy.dojo.config.oauth2;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 - 2019 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.config.meta.SSOProfile;
import com.codenjoy.dojo.services.ConfigProperties;
import com.codenjoy.dojo.services.dao.Registration;
import com.codenjoy.dojo.services.security.GameAuthorities;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

/**
 * @author Igor_Petrov@epam.com
 * Created at 5/17/2019
 */
@Component
@RequiredArgsConstructor
@SSOProfile
public class OAuth2UserAuthenticationConverter extends DefaultUserAuthenticationConverter {

    private final Registration registration;
    private final PasswordEncoder passwordEncoder;
    private final ConfigProperties properties;

    @Value("${registration.password.autogen-length}")
    private int autoGenPasswordLen;

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        Authentication authentication = super.extractAuthentication(map);
        if (authentication != null) {
            return authentication;
        }
        String email = (String) map.get("email");

        Registration.User applicationUser = registration.getUserByEmail(email)
                .orElseGet(() -> registerNewUser(map));

        return new UsernamePasswordAuthenticationToken(applicationUser, null, applicationUser.getAuthorities());
    }

    private Registration.User registerNewUser(Map<String, ?> map) {
        String email = (String) map.get("email");
        String playerName = (String) map.get("name");
        playerName = StringUtils.hasText(playerName) ? playerName : email;

        String autoGeneratedPassword = passwordEncoder.encode(randomAlphanumeric(autoGenPasswordLen));

        Registration.User newlyRegisteredUser = registration.register(playerName, email, playerName,
                autoGeneratedPassword, "{}", GameAuthorities.USER.roles());

        if (!properties.isEmailVerificationNeeded()) {
            registration.approve(newlyRegisteredUser.getCode());
        }

        return newlyRegisteredUser;
    }
}
