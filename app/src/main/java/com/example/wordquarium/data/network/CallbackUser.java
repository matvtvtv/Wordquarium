package com.example.wordquarium.data.network;

import com.example.wordquarium.data.model.PlayerModel;

public interface CallbackUser {
    void onSuccess (PlayerModel playerModel);
    void onError(Throwable throwable);
}