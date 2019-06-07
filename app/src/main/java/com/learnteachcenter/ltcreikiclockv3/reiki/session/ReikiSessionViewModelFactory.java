package com.learnteachcenter.ltcreikiclockv3.reiki.session;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class ReikiSessionViewModelFactory implements ViewModelProvider.Factory {
    private String mParam;

    public ReikiSessionViewModelFactory(String param) {
        mParam = param;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new ReikiSessionViewModel(mParam);
    }
}
