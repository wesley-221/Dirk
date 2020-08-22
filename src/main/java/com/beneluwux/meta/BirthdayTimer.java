package com.beneluwux.meta;

import com.beneluwux.DiscordConfiguration;
import com.beneluwux.models.entities.Birthday;
import com.beneluwux.repositories.BirthdayRepository;
import org.javacord.api.entity.server.Server;
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
    @Autowired
    DiscordConfiguration discordConfiguration;

    @Autowired
    public BirthdayTimer(BirthdayRepository birthdayRepository) {
        this.birthdayRepository = birthdayRepository;
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

                Server server = discordConfiguration.getDiscordApi().getServerById(birthday.getBirthdayId().getServerSnowflake()).get();

                server.getRolesByName(BIRTHDAY_ROLE_NAME).stream().findFirst().ifPresent(role -> {
                    server.getTextChannelsByName(BIRTHDAY_CHANNEL_NAME).stream().findFirst().ifPresent(textChannel -> {
                        Random random = new Random();
                        String birthdayString = BIRTHDAY_MESSAGES.get(random.nextInt(BIRTHDAY_MESSAGES.size()));

                        birthdayString = birthdayString.replace("{{birthdayUserPing}}", "<@" + birthday.getBirthdayId().getUserSnowflake() + ">")
                                .replace("{{birthdayRolePing}}", role.getMentionTag());

                        textChannel.sendMessage(birthdayString);
                    });
                });
            }
        }
    }
}

