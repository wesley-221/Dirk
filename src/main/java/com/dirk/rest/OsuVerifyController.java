/*
 * MIT License
 *
 * Copyright (c) 2020 Wesley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dirk.rest;

import com.dirk.DiscordConfiguration;
import com.dirk.commands.server_moderation.SetupVerificationCommand;
import com.dirk.models.OsuVerification;
import com.dirk.models.OsuMeHelper;
import com.dirk.models.OsuOauthHelper;
import com.dirk.repositories.OsuVerifyRepository;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;

@RestController
public class OsuVerifyController {
    OsuVerifyRepository osuVerifyRepository;
    DiscordConfiguration discordConfiguration;

    @Value("${osu.oauth.client_id}")
    String clientId;

    @Value("${osu.oauth.client_redirect}")
    String clientRedirectUri;

    @Value("${osu.oauth.client_secret}")
    String clientSecret;

    @Autowired
    public OsuVerifyController(OsuVerifyRepository osuVerifyRepository, DiscordConfiguration discordConfiguration) {
        this.osuVerifyRepository = osuVerifyRepository;
        this.discordConfiguration = discordConfiguration;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/verify/{secret}")
    public String index(HttpServletRequest request, HttpServletResponse response, @PathVariable String secret) {
        UUID userSecret = OsuVerification.getUserSecretFromURLSafeString(secret);
        OsuVerification osuVerification = osuVerifyRepository.getByUserSecret(userSecret);

        if (osuVerification != null) {
            request.getSession().setAttribute("secret", secret);

            try {
                response.sendRedirect("https://osu.ppy.sh/oauth/authorize?client_id=" + clientId + "&redirect_uri=" + clientRedirectUri + "&response_type=code");
            } catch (Exception ex) {
                return "Something went wrong.";
            }

            return "Found user!";
        }

        return "The link you opened is either invalid or expired.";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/verify")
    @Transactional
    public String index(HttpServletRequest request, @RequestParam String code) {
        String secret = (String) request.getSession().getAttribute("secret");

        UUID userSecret = OsuVerification.getUserSecretFromURLSafeString(secret);
        OsuVerification osuVerification = osuVerifyRepository.getByUserSecret(userSecret);

        // Check if there is an entry for the current user
        if (osuVerification != null) {
            // Check if the date is still valid
            if (osuVerification.getExpireDate().after(new Date())) {
                Server server = discordConfiguration.getDiscordApi().getServerById(osuVerification.getServerSnowflake()).orElse(null);

                if (server != null) {
                    User user = server.getMemberById(osuVerification.getUserSnowflake()).orElse(null);

                    if (user != null) {
                        final String OSU_OAUTH_URL = "https://osu.ppy.sh/oauth/token";

                        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

                        requestBody.add("client_id", this.clientId);
                        requestBody.add("client_secret", this.clientSecret);
                        requestBody.add("code", code);
                        requestBody.add("grant_type", "authorization_code");
                        requestBody.add("redirect_uri", this.clientRedirectUri);

                        // Request access token
                        OsuOauthHelper requestToken = WebClient
                                .create(OSU_OAUTH_URL)
                                .post()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromMultipartData(requestBody))
                                .accept(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToFlux(OsuOauthHelper.class)
                                .onErrorMap(e -> e)
                                .blockLast();

                        if (requestToken != null) {
                            // Request /me data
                            OsuMeHelper osuData = WebClient
                                    .create("https://osu.ppy.sh/api/v2/me")
                                    .get()
                                    .header("Authorization", "Bearer " + requestToken.getAccess_token())
                                    .accept(MediaType.APPLICATION_JSON)
                                    .retrieve()
                                    .bodyToFlux(OsuMeHelper.class)
                                    .onErrorMap(e -> e)
                                    .blockLast();

                            if (osuData != null) {
                                user.updateNickname(server, osuData.getUsername(), "Update username through osu! authentication");
                                user.sendMessage("✓ You have been successfully verified as **" + osuData.getUsername() + "**.");

                                server.getRolesByName(SetupVerificationCommand.VERIFIED_ROLE).stream().findFirst().ifPresent(user::addRole);

                                osuVerifyRepository.delete(osuVerification);

                                return "✓ You have been successfully verified as " + osuData.getUsername() + ". You can now close this window.";
                            }
                        }
                    }
                }
            }
        }

        return "The link you opened is either invalid or expired.";
    }
}
