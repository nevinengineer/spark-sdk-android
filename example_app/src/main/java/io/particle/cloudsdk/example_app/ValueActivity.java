package io.particle.cloudsdk.example_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class ValueActivity extends AppCompatActivity {

    private static final String ARG_VALUE = "ARG_VALUE";
    private static final String ARG_DEVICEID = "ARG_DEVICEID";

    private TextView tv, nameView, platformIdView, productIdView,
            ipAddressView, lastAppNameView, statusView, lastHeardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_value);
        nameView = (TextView) findViewById(R.id.name);
        productIdView = (TextView) findViewById(R.id.productId);
        platformIdView = (TextView) findViewById(R.id.platformId);
        ipAddressView = (TextView) findViewById(R.id.ipAddress);
        lastAppNameView = (TextView) findViewById(R.id.lastAppName);
        statusView = (TextView) findViewById(R.id.status);
        lastHeardView = (TextView) findViewById(R.id.lastHeard);
        tv = (TextView) findViewById(R.id.value);
        tv.setText(String.valueOf(getIntent().getIntExtra(ARG_VALUE, 0)));

        findViewById(R.id.refresh_button).setOnClickListener(v -> {
            //...
            // Do network work on background thread
            Async.executeAsync(ParticleCloud.get(ValueActivity.this), new Async.ApiWork<ParticleCloud, Object>() {
                @Override
                public Object callApi(@NonNull ParticleCloud ParticleCloud) throws ParticleCloudException, IOException {
                    ParticleDevice device = ParticleCloud.getDevice(getIntent().getStringExtra(ARG_DEVICEID));
                    //show device information
                    runOnUiThread(() -> {
                        nameView.setText("Name: " + device.getName());
                        productIdView.setText("Product id: " + device.getProductID());
                        platformIdView.setText("Platform id: " + device.getPlatformID());
                        ipAddressView.setText("Ip address: " + device.getIpAddress());
                        lastAppNameView.setText("Last app name: " + device.getLastAppName());
                        statusView.setText("Status: " + device.getStatus());
                        lastHeardView.setText("Last heard: " + device.getLastHeard());
                    });
                    Object variable;
                    try {
                        variable = device.getVariable("analogvalue");
                    } catch (ParticleDevice.VariableDoesNotExistException e) {
                        Toaster.l(ValueActivity.this, e.getMessage());
                        variable = -1;
                    }
                    return variable;
                }

                @Override
                public void onSuccess(@NonNull Object i) { // this goes on the main thread
                    tv.setText(i.toString());
                }

                @Override
                public void onFailure(@NonNull ParticleCloudException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static Intent buildIntent(Context ctx, Integer value, String deviceId) {
        Intent intent = new Intent(ctx, ValueActivity.class);
        intent.putExtra(ARG_VALUE, value);
        intent.putExtra(ARG_DEVICEID, deviceId);

        return intent;
    }


}
