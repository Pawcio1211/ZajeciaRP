package iot.examples.firexample;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private double sampleTime;
    private String st = "{temperatura[C]:24.45},{cisnienie[hPa]:1013},{wilgotnosc[%:44}";

    private GraphView chart; //!< GraphView object
    private LineGraphSeries[] signal;
    private RadioButton signalOption;
    private RadioButton signalOption1;
    private RadioButton signalOption2;

    private Timer filterTimer;


    private int sampleMax;

    private int k = 0; //!< Samples counter

    private boolean signalMock=false;
    private boolean signalMock1=false;
    private boolean signalMock2=false;

    /**** My FIR Low pass filter ***********************************************************/
    private MyFir filtermy = new MyFir(MyFirData.feedforward_coefficients, MyFirData.state);
    private MyFir filter = new MyFir(MyFirData.temp, MyFirData.press);
    /**** Server mock **********************************************************************/
    private ServerMock serverMock = new ServerMock(1.0 / MyFirData.sampletime);
    /**** Server (RPi) ********************************************************************/
    private ServerIoT server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ChartInit();

        signalOption = findViewById(R.id.op1);
        signalOption1 = findViewById(R.id.op11);
        signalOption2 = findViewById(R.id.op12);

        sampleTime = MyFirData.sampletime;
        sampleMax = (int)(chart.getViewport().getMaxX(false) / sampleTime);



        server = new ServerIoT("192.168.56.5", this);
    }

    /**
     * @brief 'RUN FIR DEMO' button onClick event handler
     * @param v run_demo View from activity_main
     */
    public void RunButton(View v) {
        signalMock = signalOption.isChecked();
        signalMock1 = signalOption1.isChecked();
        signalMock2 = signalOption2.isChecked();


        if(filterTimer == null) {
            k = 0;
            server.resetRequestCounter();

            signal[0].resetData(new DataPoint[]{});
            signal[1].resetData(new DataPoint[]{});
            signal[2].resetData(new DataPoint[]{});

            filterTimer = new Timer();
            TimerTask filterTimerTask = new TimerTask() {
                public void run() { FilterProcedure(); }
            };
            filterTimer.scheduleAtFixedRate(filterTimerTask, 0, (int)(sampleTime*1000));
        }
    }



    /**
     * @brief Demo of signal filtering procedure using the FIR filter.
     */
    private void  FilterProcedure() {
        if (k <= sampleMax) {
            // get signal
            final Double x;
            final Double y;
            final Double z;
            //tutaj przypisujesz wartość co sekundę więc raczej tutaj
            //powinieneś ogarniać przypisywanie wartości
            //Wyciąganie z stringa wartości masz na końcu te stringi i 2 wartość z
            // tablicy jest wartością którą musisz przekonwertować na dabla

            //        String tekst = "zad1:[{temperatura[C]:24.5},{cisnienie[hPa]:1512.551},{wilgotnosc}[%]:44.5]";
            //        String[] aray = tekst.split(",");
            //
            //        for(int i =0;i<aray.length;i++)
            //        {
            //            aray[i] = aray[i].replace("zad1:","");
            //            aray[i] = aray[i].replace("{","");
            //            aray[i] = aray[i].replace("}","");
            //
            //        }
            //        String[] Temp = aray[0].split(":");
            //        String[] Press = aray[1].split(":");
            //        String[] Wet = aray[2].split(":");
            //
            //    }
            if (signalMock) {
                // Ify warunkowe możesz tutaj zawrzeć konwersje lub Wyrzej w funkcji przycisku
                // tam masz funkcje która sprawdza w jakim, stanie jest przycisk
                // Jeszcze możesz ostatecznie jak wyciągniesz przypisywać wartości do tyh zmiennych jak tutaj masz
                // tegi x tam jest zapisana baza danych jak chcesz sprawdzić czy dobrze ci się zapisuje
                // ale jak chcesz tylkow yświetlac to nei potrzebujesz raczej tego nigdzie zapiisywać tylko
                // dodać tutaj pod x wartość i elo ;)

                x = MyFirData.temp[k];
            } else {
                // from server
                x = MyFirData.wet[k]*0.5;
            }

            if (signalMock1) {
                // from mock object
                y = MyFirData.press[k];
            } else {
                // from server
                y = MyFirData.press[k]*0.5;
            }

            if (signalMock2) {
                // from mock object
                z = MyFirData.wet[k];
            } else {
                // from server
                z = MyFirData.wet[k] * 0.5;
            }
            // display data (GraphView)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    signal[0].appendData(new DataPoint(k * sampleTime, x), false, sampleMax);
                    signal[1].appendData(new DataPoint(k * sampleTime, y), false, sampleMax);
                    signal[2].appendData(new DataPoint(k * sampleTime, z), false, sampleMax);

                }
            });
            // update time
            k++;
        } else {
            filterTimer.cancel();
            filterTimer = null;
        }
    }

    /**
     * @brief Chart initialization.
     */
    private void ChartInit() {
        // https://github.com/jjoe64/GraphView/wiki
        chart = (GraphView)findViewById(R.id.chart);
        signal = new LineGraphSeries[]{
                new LineGraphSeries<>(new DataPoint[]{}),
                new LineGraphSeries<>(new DataPoint[]{}),
                new LineGraphSeries<>(new DataPoint[]{})
        };
        chart.addSeries(signal[0]);
        chart.addSeries(signal[1]);
        chart.addSeries(signal[2]);

        chart.getViewport().setXAxisBoundsManual(true);
        chart.getViewport().setMinX(0.0);
        chart.getViewport().setMaxX(50.0);
        chart.getViewport().setYAxisBoundsManual(true);
        chart.getViewport().setMinY(-3.0);
        chart.getViewport().setMaxY(3.0);

        signal[0].setTitle("Temp");
        signal[0].setColor(Color.BLUE);
        signal[1].setTitle("Press");
        signal[1].setColor(Color.RED);
        signal[2].setTitle("Wet");
        signal[2].setColor(Color.GREEN);

        chart.getLegendRenderer().setVisible(true);
        chart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        chart.getLegendRenderer().setTextSize(30);

        chart.getGridLabelRenderer().setTextSize(20);
        chart.getGridLabelRenderer().setVerticalAxisTitle(Space(7) + "Amplitude [-]");
        chart.getGridLabelRenderer().setHorizontalAxisTitle(Space(11) + "Time [s]");
        chart.getGridLabelRenderer().setNumHorizontalLabels(9);
        chart.getGridLabelRenderer().setNumVerticalLabels(7);
        chart.getGridLabelRenderer().setPadding(35);
    }

    /**
     * @param n Number of spaces.
     * @retval String with 'n' spaces.
     */
    private String Space(int n) {
        return new String(new char[n]).replace('\0', ' ');
    }
}
