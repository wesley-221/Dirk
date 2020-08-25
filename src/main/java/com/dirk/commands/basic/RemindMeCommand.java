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

package com.dirk.commands.basic;

import com.dirk.helper.EmbedHelper;
import com.dirk.meta.RemindMeComponent;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandArgument;
import com.dirk.models.command.CommandArgumentType;
import com.dirk.models.command.CommandParameter;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RemindMeCommand extends Command {
    RemindMeComponent remindMeComponent;

    @Autowired
    public RemindMeCommand(RemindMeComponent remindMeComponent) {
        this.commandName = "remindme";
        this.description = "Set a reminder that will be send to you in a direct message.";
        this.group = "Basic";

        this.commandArguments.add(new CommandArgument("time amount", "The amount of seconds/minutes/hours until you will get the notification.", CommandArgumentType.Integer));
        this.commandArguments.add(new CommandArgument("time unit", "The time unit to wait in. \nAccepted units: `seconds`, `minutes`, `hours`.", CommandArgumentType.SingleString));
        this.commandArguments.add(new CommandArgument("message", "The message to send after the given delay.", CommandArgumentType.String));

        this.remindMeComponent = remindMeComponent;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        CommandParameter timeAmount = commandParams.get(0);
        CommandParameter timeUnit = commandParams.get(1);
        CommandParameter message = commandParams.get(2);

        List<String> acceptedTimeUnits = List.of("second", "seconds", "minute", "minutes", "hour", "hours");

        // Check if parameters have been parsed correctly
        if (!timeAmount.isParsedCorrectly() || !timeUnit.isParsedCorrectly()) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed(this.getIncorrectCommandHelpFormat(), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        // Check if time unit is a proper time unit
        if (!acceptedTimeUnits.contains(timeUnit.getValue().toString())) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed(this.getIncorrectCommandHelpFormat(), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        long delay = 0;
        String timeUnitString = "";

        // Add delay in seconds
        if (timeUnit.getValue().equals("second") || timeUnit.getValue().equals("seconds")) {
            delay = ChronoUnit.MILLIS.between(LocalTime.now(), LocalTime.now().plusSeconds(Integer.toUnsignedLong((Integer) timeAmount.getValue())));
            timeUnitString = (Integer) timeAmount.getValue() > 1 ? "seconds" : "second";
        }
        // Add delay in minutes
        else if (timeUnit.getValue().equals("minute") || timeUnit.getValue().equals("minutes")) {
            delay = ChronoUnit.MILLIS.between(LocalTime.now(), LocalTime.now().plusMinutes(Integer.toUnsignedLong((Integer) timeAmount.getValue())));
            timeUnitString = (Integer) timeAmount.getValue() > 1 ? "minutes" : "minute";
        }
        // Add delay in hours
        else if (timeUnit.getValue().equals("hour") || timeUnit.getValue().equals("hours")) {
            delay = ChronoUnit.MILLIS.between(LocalTime.now(), LocalTime.now().plusHours(Integer.toUnsignedLong((Integer) timeAmount.getValue())));
            timeUnitString = (Integer) timeAmount.getValue() > 1 ? "hours" : "hour";
        }

        messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed("I will remind you in " + timeAmount.getValue() + " " + timeUnitString + "!", null).setAuthor("Reminder for " + messageCreateEvent.getMessageAuthor().getDiscriminatedName()));

        remindMeComponent.getScheduler().schedule(() -> messageCreateEvent.getMessageAuthor().asUser().ifPresent(user -> user.sendMessage(EmbedHelper.reminderEmbed((String) message.getValue()))), delay, TimeUnit.MILLISECONDS);
    }
}
