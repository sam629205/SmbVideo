package com.zh.zhvideoplayer.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Adolf Li on 2017/8/1.
 */

public class StarModel extends RealmObject{
    @PrimaryKey
    private String starJavbusId;
    private String name;

    public String getStarJavbusId() {
        return starJavbusId;
    }

    public void setStarJavbusId(String starJavbusId) {
        this.starJavbusId = starJavbusId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
