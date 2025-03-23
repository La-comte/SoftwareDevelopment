package ru.iu3.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;

import ru.iu3.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements TransactionEvents {

    static {
        System.loadLibrary("myapplication");
        System.loadLibrary("mbedcrypto");
    }

    private ActivityMainBinding binding;
    private ActivityResultLauncher activityResultLauncher;
    private String pin;

    @Override
    public String enterPin(int ptc, String amount) {
        pin = new String();
        Intent it = new Intent(MainActivity.this, PinpadActivity.class);
        it.putExtra("ptc", ptc);
        it.putExtra("amount", amount);
        synchronized (MainActivity.this) {
            activityResultLauncher.launch(it);
            try {
                MainActivity.this.wait();
            } catch (Exception ex) {
                //todo: log error
            }
        }
        return pin;
    }


    @Override
    public void transactionResult(boolean result) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, result ? "ok" : "failed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int res = initRng();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback() {
                    @Override
                    public void onActivityResult(Object o) {
                        ActivityResult result = (ActivityResult) o;
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            // обработка результата
                            pin = data.getStringExtra("pin");

                            synchronized (MainActivity.this) {
                                MainActivity.this.notifyAll();
                            }
                        }
                    }
                }
        );
        byte[] trd = stringToHex("9F0206000000000100");
        transaction(trd);

    }

    public native boolean transaction(byte[] trd);

    public static byte[] stringToHex(String s) {
        byte[] hex;
        try {
            hex = Hex.decodeHex(s.toCharArray());
        } catch (DecoderException ex) {
            hex = null;
        }
        return hex;
    }


    public void onButtonClick(View v) {
        Intent it = new Intent(this, PinpadActivity.class);
        it.putExtra("amount", "100"); // пример значения
        it.putExtra("ptc", 0);              // пример количества попыток
//        startActivity(it);
        activityResultLauncher.launch(it);

    }


    /**
     * A native method that is implemented by the 'myapplication' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public static native int initRng();

    public static native byte[] randomBytes(int no);

    public static native byte[] encrypt(byte[] key, byte[] data);

    public static native byte[] decrypt(byte[] key, byte[] data);
}
