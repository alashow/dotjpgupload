/*
 * Copyright 2014. Alashov Berkeli
 *
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tm.veriloft.dotjpgupload;
/**
 * Created by alashov on 16/01/15.
 */
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;

public class ApplicationLoader extends Application {
    public static volatile Context applicationContext = null;
    public static volatile Handler applicationHandler = null;
    private static volatile boolean applicationInited = false;

    public static void postInitApplication() {
        if (applicationInited) {
            return;
        }
        applicationInited = true;
        ApplicationLoader app = (ApplicationLoader) ApplicationLoader.applicationContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        postInitApplication();
        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig ) {
        super.onConfigurationChanged(newConfig);

    }
}
