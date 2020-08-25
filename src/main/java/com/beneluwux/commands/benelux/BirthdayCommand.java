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

package com.beneluwux.commands.benelux;

import com.beneluwux.helper.EmbedHelper;
import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandArgument;
import com.beneluwux.models.command.CommandArgumentType;
import com.beneluwux.models.command.CommandParameter;
import com.beneluwux.models.entities.Birthday;
import com.beneluwux.models.entities.embeddables.BirthdayId;
import com.beneluwux.repositories.BirthdayRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class BirthdayCommand extends Command {
    BirthdayRepository birthdayRepository;

    @Autowired
    public BirthdayCommand(BirthdayRepository birthdayRepository) {
        this.commandName = "birthday";
        this.description = "Set your birthday so that everyone with the Birthday role will get a notification for when it is your birthday in the Beneluwux server.";

        this.guildOnly = true;

        this.addCommandArgument(new CommandArgument("date", "Enter the date in dd/mm/yyyy format (01/05/1995).", CommandArgumentType.Date));

        this.birthdayRepository = birthdayRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        BirthdayId birthdayId = new BirthdayId(messageCreateEvent.getServer().get().getId(), messageCreateEvent.getMessageAuthor().getId());

        Birthday birthday = new Birthday();
        birthday.setBirthdayId(birthdayId);
        birthday.setBirthday((Date) commandParams.get(0).getValue());

        birthdayRepository.save(birthday);

        // Parse the date to a readable format
        String parsedDate = new SimpleDateFormat("dd MMMMM, yyyy").format(commandParams.get(0).getValue());

        messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed("Updated your birthday to " + parsedDate + ".", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
    }
}
