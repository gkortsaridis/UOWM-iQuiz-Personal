package gr.gkortsaridis.iquizpersonal;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class EndquizActivity extends ActionBarActivity {

    private String[] myAnswers;
    private ArrayList<String> corrects;
    private long millisToFinish;
    private int totalTime;
    private String pointgame;
    private String feedback;
    private int mycorrectAnswers;
    private String myNickname , myOTP;
    double mypoints;

    TextView theScore , theTime , theStatus;

    AsyncTask<String, Void, String> httptask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endquiz);

        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        corrects = new ArrayList<>();
        mycorrectAnswers = 0;

        Intent intent = getIntent();
        myAnswers = intent.getStringArrayExtra("answers");
        corrects = intent.getExtras().getStringArrayList("corrects");

        millisToFinish = intent.getExtras().getLong("millis")/1000;
        totalTime = intent.getExtras().getInt("total");

        pointgame = intent.getExtras().getString("pointgame");
        feedback = intent.getExtras().getString("feedback");

        myNickname = intent.getExtras().getString("nickname");
        myOTP = intent.getExtras().getString("otp");

        for(int i=0; i<myAnswers.length; i++){
            if(myAnswers[i].equals(corrects.get(i))) mycorrectAnswers++;
        }

        theScore = (TextView) findViewById(R.id.theScoreTxt);
        theTime = (TextView) findViewById(R.id.theTimeTxt);

        if(feedback.equals("yes")) {
            if (pointgame.equals("yes")) {

                double timePoint = (double)millisToFinish/totalTime;
                mypoints = round(mycorrectAnswers * timePoint,3);

                theScore.setText("Your points are : " + mypoints);
                theTime.setText("Your time was: "+(totalTime-millisToFinish)+" seconds");
            }else {
                mypoints = ((double)mycorrectAnswers/corrects.size())*100;
                Log.i("mycorrectanswers",mycorrectAnswers+"");
                Log.i("Corrects.size",""+corrects.size());
                theScore.setText("You answered "+mycorrectAnswers+" correct question(s) out of "+corrects.size()+" ("+mypoints+"%)");
                theTime.setText("Your time was: "+(totalTime-millisToFinish)+" seconds");
            }
        }
        else{
            theTime.setText("");
            theScore.setText("Your answers have been recorded, but they are not visible.");
        }

        sendToServer();


    }

    public void sendToServer(){

        if (pointgame.equals("yes")) httptask = new HttpAsyncTask().execute("http://arch.icte.uowm.gr/game/iquizpersonalscore.php", "newresult", myOTP+":"+myNickname+":"+mypoints);
        else httptask = new HttpAsyncTask().execute("http://arch.icte.uowm.gr/game/iquizpersonalscore.php", "newresult", myOTP+":"+myNickname+":"+mypoints/100+":"+(totalTime-millisToFinish));
        String x;
        try {
            //Pairnoume tin apantisi tou server
            x = httptask.get();
            Log.i("PIRA", x);

            if(x.equals("ERROR")){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("There was an error");

                builder.setPositiveButton("Resend", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendToServer();
                    }

                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Intent intent = new Intent(EndquizActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();


                    }

                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            else Toast.makeText(this.getBaseContext(), "Your score has been submitted to our server", Toast.LENGTH_SHORT).show();

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void backToMainMenu(View view){
        Intent intent = new Intent(EndquizActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void quitgame(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private boolean isTablet() {
        return (this.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
