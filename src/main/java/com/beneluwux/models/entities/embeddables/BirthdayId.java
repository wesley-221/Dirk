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

package com.beneluwux.models.entities.embeddables;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BirthdayId implements Serializable {
    private String serverSnowflake;
    private String userSnowflake;

    public BirthdayId() {
    }

    public BirthdayId(String serverSnowflake, String userSnowflake) {
        this.serverSnowflake = serverSnowflake;
        this.userSnowflake = userSnowflake;
    }

    public BirthdayId(Long serverSnowflake, Long userSnowflake) {
        this.serverSnowflake = serverSnowflake.toString();
        this.userSnowflake = userSnowflake.toString();
    }

    public String getServerSnowflake() {
        return serverSnowflake;
    }

    public void setServerSnowflake(String serverSnowflake) {
        this.serverSnowflake = serverSnowflake;
    }

    public String getUserSnowflake() {
        return userSnowflake;
    }

    public void setUserSnowflake(String userSnowflake) {
        this.userSnowflake = userSnowflake;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BirthdayId that = (BirthdayId) o;

        return this.serverSnowflake.equals(that.serverSnowflake) && this.userSnowflake.equals(that.userSnowflake);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.serverSnowflake, this.userSnowflake);
    }

    @Override
    public String toString() {
        return "BirthdayId{" +
                "serverSnowflake='" + serverSnowflake + '\'' +
                ", userSnowflake='" + userSnowflake + '\'' +
                '}';
    }
}
