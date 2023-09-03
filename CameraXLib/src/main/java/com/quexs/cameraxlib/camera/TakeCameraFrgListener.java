package com.quexs.cameraxlib.camera;

import android.content.Intent;
import android.view.View;

/**
 * @author Quexs
 * @description:
 * @date: 2023/9/3 21:31
 */
public interface TakeCameraFrgListener {
     void onFragmentBackPressed();
     void onFragmentReplace(String frgName, View sharedElement, String sharedElementName);

     void onFragmentBackResult(Intent intent);
}
