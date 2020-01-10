package com.app.izoototest;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.izooto.NotificationHelperListener;
import com.izooto.Payload;
import com.izooto.iZooto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity implements NotificationHelperListener
{

    private static String CIPHER_NAME = "AES/CBC/PKCS5PADDING";
    private static int CIPHER_KEY_LEN = 16;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        iZooto.initialize(this).setNotificationReceiveListener(this).build();


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public static void main(String[] args) {

        String data = decrypt("b07dfa9d56fc64df", "6Q+BXzyxAj+TbJcvFfsnMOl43Y1v0I0psyKaiDMK+gSLuaPjsFFUi+Ph1euQxhwSxHvU4oEJD5kQ1iXFrCW8MGaZRGFwWjJdwfaUsKTGal5YQ1x+ToX+IX0AmyN7rFxLcG5pHeYoAwmSTn9+olzEMMzluowtSGKveDEbMj8ZHLQlVoCTg/kdr1WXG1S9bS6q6cU2l5+kcYnc5ObceTMaOQ==:ZWY1YTc0YWZhMGM4YjM5OQ==");
        Log.d("data", data);
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public static String decrypt(String key, String data) {
        try {
            if (key.length() < CIPHER_KEY_LEN) {
                int numPad = CIPHER_KEY_LEN - key.length();
                StringBuilder keyBuilder = new StringBuilder(key);

                for(int i = 0; i < numPad; ++i) {
                    keyBuilder.append("0");
                }

                key = keyBuilder.toString();
            } else if (key.length() > CIPHER_KEY_LEN) {
                key = key.substring(0, CIPHER_KEY_LEN);
            }

            String[] parts = data.split(":");
            IvParameterSpec iv = new IvParameterSpec(Base64.decode(parts[1], 0));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("ISO-8859-1"), "AES");
            Cipher cipher = Cipher.getInstance(CIPHER_NAME);
            cipher.init(2, skeySpec, iv);
            byte[] decodedEncryptedData = Base64.decode(parts[0], 0);
            byte[] original = cipher.doFinal(decodedEncryptedData);
            return new String(original);
        } catch (Exception var8) {
            var8.printStackTrace();
            return null;
        }
    }




    @Override
    public void onNotificationReceived(Payload payload) {
        Log.e("Received",payload.getTitle());

    }

    @Override
    public void onNotificationView(String s) {
        Log.e("NotificationClicked",s);

    }
}
