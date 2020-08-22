package com.beneluwux.models.entities;

import com.beneluwux.models.entities.embeddables.BirthdayId;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.util.Date;

@Entity
public class Birthday {
    @EmbeddedId
    private BirthdayId birthdayId;
    private Date birthday;

    public Birthday() {
    }

    public BirthdayId getBirthdayId() {
        return birthdayId;
    }

    public void setBirthdayId(BirthdayId birthdayId) {
        this.birthdayId = birthdayId;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}
