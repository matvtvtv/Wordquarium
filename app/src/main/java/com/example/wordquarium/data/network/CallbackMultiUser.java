package com.example.wordquarium.data.network;

import com.example.wordquarium.data.model.MultiUserModel;

public interface CallbackMultiUser {

    void onSuccess (MultiUserModel multiUserModel);
    void onError(Throwable throwable);
}
