package com.learnteachcenter.ltcreikiclockv3.api.responses.Reiki;

import android.support.annotation.Nullable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki;

import java.util.List;

public class ReikisResponse {

    @SerializedName("count")
    @Expose()
    private int count;

    @SerializedName("reikis")
    @Expose()
    private List<Reiki> reikis;

    @Nullable
    public List<Reiki> getReikis() { return reikis; }

    @SerializedName("error")
    @Expose()
    private String error;

    public String getError() {
        return error;
    }
}
