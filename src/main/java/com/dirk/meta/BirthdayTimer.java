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

package com.dirk.meta;

import com.dirk.DiscordConfiguration;
import com.dirk.models.entities.Birthday;
import com.dirk.repositories.BirthdayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

@Component
public class BirthdayTimer {
    private static final String BIRTHDAY_CHANNEL_NAME = "birthdays";
    private static final String BIRTHDAY_ROLE_NAME = "Birthday";

    private static final List<String> BIRTHDAY_MESSAGES = List.of(
            "Wooo! Today is {{birthdayUserPing}}'s birthday! :birthday: Congratulate them {{birthdayRolePing}}!",
            "Hieperdepiep hoera! Today we celebrate {{birthdayUserPing}}'s birthday! Go wish them a happy birthday {{birthdayRolePing}}! :partying_face:",
            "Would you look at that?! It's {{birthdayUserPing}}'s birthday today :balloon:! Wish them a happy birthday {{birthdayRolePing}}!",
            ":trumpet: Wow!! It's {{birthdayUserPing}}'s birthday! Time for a party :microphone:! Go wish them a happy birthday {{birthdayRolePing}}!",
            "Today is your day... yes, your day {{birthdayUserPing}}!! Happy birthday :birthday:! Go congratulate them {{birthdayRolePing}}!",
            "RING THE BELLS :bell: :bell: !! {{birthdayUserPing}} is celebrating their birthday today!! Go wish them a happy birthday {{birthdayRolePing}}!"
    );

    BirthdayRepository birthdayRepository;
    DiscordConfiguration discordConfiguration;

    @Autowired
    public BirthdayTimer(BirthdayRepository birthdayRepository, DiscordConfiguration discordConfiguration) {
        this.birthdayRepository = birthdayRepository;
        this.discordConfiguration = discordConfiguration;
    }

    // <second> <minute> <hour> <day-of-month> <month> <day-of-week> <year> <command>
    @Scheduled(cron = "0 0 0 * * *")
    public void birthdayTask() {
        List<Birthday> birthdayList = (List<Birthday>) birthdayRepository.findAll();

        for (Birthday birthday : birthdayList) {
            Calendar today = Calendar.getInstance();
            Calendar birthdayDate = Calendar.getInstance();
            birthdayDate.setTime(birthday.getBirthday());

            if (today.get(Calendar.DAY_OF_MONTH) == birthdayDate.get(Calendar.DAY_OF_MONTH) &&
                    today.get(Calendar.MONTH) == birthdayDate.get(Calendar.MONTH)) {

                discordConfiguration
                        .getDiscordApi()
                        .getServerById(birthday.getBirthdayId().getServerSnowflake())
                        .ifPresent(server -> server.getRolesByName(BIRTHDAY_ROLE_NAME).stream().findFirst()
                                .ifPresent(role -> server.getTextChannelsByName(BIRTHDAY_CHANNEL_NAME).stream().findFirst()
                                        .ifPresent(textChannel -> {
                                            Random random = new Random();
                                            String birthdayString = BIRTHDAY_MESSAGES.get(random.nextInt(BIRTHDAY_MESSAGES.size()));

                                            birthdayString = birthdayString.replace("{{birthdayUserPing}}", "<@" + birthday.getBirthdayId().getUserSnowflake() + ">")
                                                    .replace("{{birthdayRolePing}}", role.getMentionTag());

                                            textChannel.sendMessage(birthdayString);
                                        })));

            }
        }
    }
}

