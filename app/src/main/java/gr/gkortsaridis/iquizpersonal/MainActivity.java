package gr.gkortsaridis.iquizpersonal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;


public class MainActivity extends ActionBarActivity {

    EditText nickname , questionId;
    AsyncTask<String, Void, String> httptask;
    private final String webAddress = "https://arch.icte.uowm.gr/iexamsII/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nickname = (EditText) findViewById(R.id.nicknameInput);
        questionId = (EditText) findViewById(R.id.questIDinput);

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }

        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //An den iparxei sindesi sto internet
        if(!isConnected())
        {
            //Emfanizo to katallilo Dialod Box
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("No Internet Connection");
            builder.setMessage("Your device is not connected to the Internet");
            builder.setPositiveButton("WiFi Settings", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent settings = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(settings);
                }

            });
            builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }

            });
            AlertDialog alert = builder.create();
            alert.show();
        }


    }

    public void startGame(View view){

        if(isConnected()) {
            Intent intent;
            intent = new Intent(MainActivity.this, GameActivity.class);

            httptask = new HttpAsyncTask().execute(webAddress + "outputxml.php", "otpid", questionId.getText().toString());
            String x;
            try {
                //Pairnoume tin apantisi tou server
                x = httptask.get();
                Log.i("PIRA", x);

                if (x.equals("ERROR"))
                    Toast.makeText(this.getBaseContext(), "Not a valid Test ID", Toast.LENGTH_SHORT).show();
                else {
                    intent.putExtra("data", x);

                    httptask = new HttpAsyncTask().execute(webAddress + "settings.php", "otpid", questionId.getText().toString());
                    try {
                        //Pairnoume tin apantisi tou server
                        x = httptask.get();

                        if (x.equals("ERROR"))
                            Toast.makeText(this.getBaseContext(), "Not a valid Settings ID", Toast.LENGTH_SHORT).show();
                        else {
                            intent.putExtra("settings", x);
                            String myNick = nickname.getText().toString().replace(':', '+');
                            intent.putExtra("nickname", myNick);
                            intent.putExtra("otp", questionId.getText().toString());
                            startActivity(intent);
                        }


                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                }


            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        else Toast.makeText(this.getBaseContext(), "You are not connected to the Internet. Please connect and try again", Toast.LENGTH_SHORT).show();




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            //Intent about = new Intent(MainActivity.this , AboutActivity.class);
            //startActivity(about);
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);

            final SpannableString s = new SpannableString("iQuizPersonal is a self-test evaluation mobile application that is used together with the asynchronous examination suite iexams, by Dr. Dasygenis on his University courses\n\nDeveloped by Kortsaridis George\nSupervised by Minas Dasygenis\nhttp://arch.icte.uowm.gr");
            Linkify.addLinks(s, Linkify.ALL);

            builder.setTitle("iQuiz Personal");
            builder.setIcon(R.drawable.logo);
            builder.setMessage(s);

            android.support.v7.app.AlertDialog alert = builder.create();
            alert.show();

            // Make the textview clickable. Must be called after show()
            ((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isTablet() {
        return (this.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public boolean isConnected()
    {
        //Elegxei an i siskeui einai sindedemeni sto internet, me WIFI i me Data sindesi
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


}
