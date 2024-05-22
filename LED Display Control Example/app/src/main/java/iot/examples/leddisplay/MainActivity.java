/**
 ******************************************************************************
 * @file    LED Display Control Example/MainActivity.java
 * @author  Adrian Wojcik
 * @version V1.0
 * @date    09-Apr-2020
 * @brief   LED display controller: main activity with display GUI
 ******************************************************************************
 */

package iot.examples.leddisplay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TableLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    SeekBar redSeekBar, greenSeekBar, blueSeekBar;
    View colorView;
    EditText urlText;

    int ledActiveColorA;
    int ledActiveColorR;
    int ledActiveColorG;
    int ledActiveColorB;

    int ledActiveColor;
    int ledOffColor;
    Vector<Integer> ledOffColorVec;

    Integer[][][] ledDisplayModel = new Integer[8][8][3];

    String url = "http://192.168.1.208/led_display.php";
    private RequestQueue queue;
    Map<String, String>  paramsClear = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ledOffColor = ResourcesCompat.getColor(getResources(), R.color.ledIndBackground, null);
        ledOffColorVec = intToRgb(ledOffColor);

        ledActiveColor = ledOffColor;

        ledActiveColorR = 0x00;
        ledActiveColorG = 0x00;
        ledActiveColorB = 0x00;

        clearDisplayModel();

        redSeekBar = (SeekBar)findViewById(R.id.seekBarR);
        redSeekBar.setMax(255);
        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {/* Auto-generated method stub */ }
            public void onStopTrackingTouch(SeekBar seekBar) {
                ledActiveColor = seekBarUpdate('R', progressChangedValue);
                colorView.setBackgroundColor(ledActiveColor);
            }
        });

        greenSeekBar = (SeekBar)findViewById(R.id.seekBarG);
        greenSeekBar.setMax(255);
        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {/* Auto-generated method stub */ }
            public void onStopTrackingTouch(SeekBar seekBar) {
                ledActiveColor = seekBarUpdate('G', progressChangedValue);
                colorView.setBackgroundColor(ledActiveColor);
            }
        });

        blueSeekBar = (SeekBar)findViewById(R.id.seekBarB);
        blueSeekBar.setMax(255);
        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {/* Auto-generated method stub */ }
            public void onStopTrackingTouch(SeekBar seekBar) {
                ledActiveColor = seekBarUpdate('B', progressChangedValue);
                colorView.setBackgroundColor(ledActiveColor);
            }
        });

        colorView = findViewById(R.id.colorView);

        urlText = findViewById(R.id.urlText);
        urlText.setText(url);

        queue = Volley.newRequestQueue(this);

        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                String data ="["+Integer.toString(i)+","+Integer.toString(j)+",0,0,0]";
                paramsClear.put(ledIndexToTag(i, j), data);
            }
        }
    }

    public int argbToInt(int _a, int _r, int _g, int _b){
        return  (_a & 0xff) << 24 | (_r & 0xff) << 16 | (_g & 0xff) << 8 | (_b & 0xff);
    }

    public Vector<Integer> intToRgb(int argb) {
        int _r = (argb >> 16) & 0xff;
        int _g = (argb >> 8) & 0xff;
        int _b = argb & 0xff;
        Vector<Integer> rgb = new Vector<>(3);
        rgb.add(0,_r);
        rgb.add(1,_g);
        rgb.add(2,_b);
        return rgb;
    }

    Vector<Integer> ledTagToIndex(String tag) {
        // Tag: 'LEDxy"
        Vector<Integer> vec = new Vector<>(2);
        vec.add(0, Character.getNumericValue(tag.charAt(3)));
        vec.add(1, Character.getNumericValue(tag.charAt(4)));
        return vec;
    }

    String ledIndexToTag(int x, int y) {
        return "LED" + Integer.toString(x) + Integer.toString(y);
    }

    String ledIndexToJsonData(int x, int y) {
        String _x = Integer.toString(x);
        String _y = Integer.toString(y);
        String _r = Integer.toString(ledDisplayModel[x][y][0]);
        String _g = Integer.toString(ledDisplayModel[x][y][1]);
        String _b = Integer.toString(ledDisplayModel[x][y][2]);
        return "["+_x+","+_y+","+_r+","+_g+","+_b+"]";
    }

    boolean ledColorNotNull(int x, int y) {
        return !((ledDisplayModel[x][y][0]==null)||(ledDisplayModel[x][y][1]==null)||(ledDisplayModel[x][y][2]==null));
    }

    public void clearDisplayModel() {
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ledDisplayModel[i][j][0] = null;
                ledDisplayModel[i][j][1] = null;
                ledDisplayModel[i][j][2] = null;
            }
        }
    }

    int seekBarUpdate(char color, int value) {
        switch(color) {
            case 'R': ledActiveColorR = value; break;
            case 'G': ledActiveColorG = value; break;
            case 'B': ledActiveColorB = value; break;
            default: /* Do nothing */ break;
        }
        ledActiveColorA = (ledActiveColorR+ledActiveColorG+ledActiveColorB)/3;
        return argbToInt(ledActiveColorA,  ledActiveColorR, ledActiveColorG, ledActiveColorB);
    }

    public void changeLedIndicatorColor(View v) {
        v.setBackgroundColor(ledActiveColor);
        String tag = (String)v.getTag();
        Vector<Integer> index = ledTagToIndex(tag);
        int x = (int)index.get(0);
        int y = (int)index.get(1);
        ledDisplayModel[x][y][0] = ledActiveColorR;
        ledDisplayModel[x][y][1] = ledActiveColorG;
        ledDisplayModel[x][y][2] = ledActiveColorB;
    }

    public void clearAllLed(View v) {
        TableLayout tb = (TableLayout)findViewById(R.id.ledTable);
        View ledInd;
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ledInd = tb.findViewWithTag(ledIndexToTag(i, j));
                ledInd.setBackgroundColor(ledOffColor);
            }
        }

        clearDisplayModel();
        sendClearRequest();
    }

    public Map<String, String>  getDisplayControlParams() {
        String led;
        String position_color_data;
        Map<String, String>  params = new HashMap<String, String>();
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(ledColorNotNull(i,j)) {
                    led = ledIndexToTag(i, j);
                    position_color_data = ledIndexToJsonData(i, j);
                    params.put(led, position_color_data);
                }
            }
        }
        return params;
    }

    public void sendControlRequest(View v)
    {
        url = urlText.getText().toString();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        if(!response.equals("ACK"))
                            Log.d("Response", "\n" + response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if(msg != null)
                            Log.d("Error.Response", msg);
                        else {
                            // TODO: error type specific code
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return getDisplayControlParams();
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(postRequest);
    }

    void sendClearRequest()
    {
        url = urlText.getText().toString();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        // TODO: check if ACK is valid
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if(msg != null)
                            Log.d("Error.Response", msg);
                        else {
                            // TODO: error type specific code
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return paramsClear;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(postRequest);
    }
}
