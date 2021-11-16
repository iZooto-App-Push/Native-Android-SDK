package com.izooto;

import android.content.Context;

public interface HMSTokenListener {

    void getHMSToken(Context context, HMSTokenGeneratorHandler hmsTokenGeneratorHandler);

    interface HMSTokenGeneratorHandler{
        void complete(String id);
        void failure(String errorMessage);
    }
}
