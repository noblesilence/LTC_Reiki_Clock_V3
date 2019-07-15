package com.learnteachcenter.ltcreikiclockv3.position.list;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class PositionListViewModelFactory implements ViewModelProvider.Factory {
    private String mParam;

    public PositionListViewModelFactory(String param) {
        mParam = param;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new PositionListViewModel(mParam);
    }
}
