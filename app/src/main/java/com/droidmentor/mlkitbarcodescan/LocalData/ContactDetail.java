package com.droidmentor.mlkitbarcodescan.LocalData;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by Jaison.
 */
public class ContactDetail
{
    @DatabaseField(generatedId=true)
    private int id;
    @DatabaseField
    private String text;
    @DatabaseField
    private Integer type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
