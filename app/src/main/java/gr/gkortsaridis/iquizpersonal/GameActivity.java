package gr.gkortsaridis.iquizpersonal;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;


public class GameActivity extends Activity implements View.OnClickListener{

    //Game Data
    private String data,settings;
    private ArrayList<String> questions;
    private ArrayList<String> answersA;
    private ArrayList<String> answersB;
    private ArrayList<String> answersC;
    private ArrayList<String> answersD;
    private ArrayList<String> answersE;
    private ArrayList<String> corrects;
    private String[] answeredbyme;
    private String feedback , pointgame;
    private int totalseconds;
    private long millisToFinish;
    private int totalQuestions , currentQuestion;
    private String myNickname, myOTP;

    //Game UI
    private Button ansA , ansB , ansC , ansD , ansE , nextQuest , endQuest;
    private TextView question , questionCount , remainingSeconds;

    //Other Game vars
    private Intent intent;
    private Toast t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //Getting the data from the MainActivity
        Bundle bundle = getIntent().getExtras();
        data = bundle.getString("data");
        settings = bundle.getString("settings");
        myNickname = bundle.getString("nickname");
        myOTP = bundle.getString("otp");

        //Setting up our Arraylists
        questions = new ArrayList<>();
        answersA = new ArrayList<>();
        answersB = new ArrayList<>();
        answersC = new ArrayList<>();
        answersD = new ArrayList<>();
        answersE = new ArrayList<>();
        corrects = new ArrayList<>();

        //Setting up the UI system
        ansA = (Button) findViewById(R.id.answerAbtn);
        ansB = (Button) findViewById(R.id.answerBbtn);
        ansC = (Button) findViewById(R.id.answerCbtn);
        ansD = (Button) findViewById(R.id.answerDbtn);
        ansE = (Button) findViewById(R.id.answerEbtn);
        nextQuest = (Button) findViewById(R.id.nextQuestBtn);
        endQuest = (Button) findViewById(R.id.endQuizBtn);
        question = (TextView) findViewById(R.id.theQuestionTxt);
        questionCount = (TextView) findViewById(R.id.questionCountTxt);
        remainingSeconds = (TextView) findViewById(R.id.remainingSecondsTxt);

        //Setting up Button ClickListeners
        ansA.setOnClickListener(this);
        ansB.setOnClickListener(this);
        ansC.setOnClickListener(this);
        ansD.setOnClickListener(this);
        ansE.setOnClickListener(this);
        nextQuest.setOnClickListener(this);
        endQuest.setOnClickListener(this);

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        //Log.i("Screen width", "" + width);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width / 2 - width/20 , height / 13);
        lp.setMargins(10,10,10,10);

        nextQuest.setLayoutParams(lp);
        endQuest.setLayoutParams(lp);

        //Getting our Data and Settings from the Server
        getData();
        getSettings();

        //Initial question - UI
        currentQuestion = 0;
        updateUI(currentQuestion);

        //Creating the endQuiz intent
        intent = new Intent(GameActivity.this , EndquizActivity.class);

        //Creating the countdown timer
        new CountDownTimer(totalseconds*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                remainingSeconds.setText("Απομένουν: " + (millisUntilFinished / 1000)/60 + ":" + (millisUntilFinished / 1000)%60);
                millisToFinish = millisUntilFinished;
            }
            public void onFinish() {
                intent.putExtra("answers",answeredbyme);
                intent.putStringArrayListExtra("corrects", corrects);
                intent.putExtra("millis", millisToFinish);
                intent.putExtra("total",totalseconds);
                intent.putExtra("pointgame",pointgame);
                intent.putExtra("feedback",feedback);
                intent.putExtra("nickname",myNickname);
                intent.putExtra("otp",myOTP);
                startActivity(intent);
                finish();
            }
        }.start();

    }

    //Simple Method to update all the UI,
    //according to the current question
    private void updateUI(int cnt){
        question.setText(questions.get(cnt));
        ansA.setText(answersA.get(cnt));
        ansB.setText(answersB.get(cnt));
        ansC.setText(answersC.get(cnt));
        ansD.setText(answersD.get(cnt));
        ansE.setText(answersE.get(cnt));
        questionCount.setText("Ερώτηση: "+(currentQuestion+1)+"/"+totalQuestions);
    }

    //Helps checking if the user has answered
    //all the questions
    private boolean checkAllAnswered(){
        for(int i=0; i<answeredbyme.length; i++){
            //Log.i("AnsweredByMe["+i+"]",answeredbyme[i]);
            if(answeredbyme[i].equals("")) return false;
        }
        return true;
    }

    //Filling the arraylists
    //with the data we got from the server
    private void getData(){

        XMLParser parser = new XMLParser();
        Document doc = parser.getDomElement(data); // getting DOM element
        NodeList nl = doc.getElementsByTagName("question");

        // looping through all item nodes <item>
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            questions.add(parser.getValue(e, "theQuestion"));
            answersA.add(parser.getValue(e, "ansA"));
            answersB.add(parser.getValue(e, "ansB"));
            answersC.add(parser.getValue(e, "ansC"));
            answersD.add(parser.getValue(e, "ansD"));
            answersE.add(parser.getValue(e, "ansE"));
            corrects.add(parser.getValue(e, "correct"));
        }

        totalQuestions = nl.getLength();

        answeredbyme = new String[totalQuestions];
        for(int i=0; i<totalQuestions; i++) answeredbyme[i]="";
    }

    //Filling the settings variables
    private void getSettings(){
        XMLParser parser = new XMLParser();
        Document doc = parser.getDomElement(settings); // getting DOM element

        NodeList nl = doc.getElementsByTagName("iquizpersonalparams");
        Element el0 = (Element) nl.item(0);
        feedback = parser.getValue(el0, "feedback");
        Log.i("Feedback",feedback);
        pointgame = parser.getValue(el0, "pointgame");
        Log.i("PointGame",pointgame);
        totalseconds = Integer.parseInt(parser.getValue(el0, "totaltime"));
        Log.i("Seconds",totalseconds+"");


    }

    //Changes the current question, and finds
    //which is the next unanswered question so far
    private void changeQuestAndUpdate(){

        if(!checkAllAnswered()) {

            int cnt = 0;
            for(int i=0; i<answeredbyme.length; i++)if(answeredbyme[i].equals(""))cnt++;
            if(cnt == 1)nextQuest.setEnabled(false);
            else nextQuest.setEnabled(true);

            if(feedback.equals("yes")) {
                if (answeredbyme[currentQuestion].equals(corrects.get(currentQuestion)) && !answeredbyme[currentQuestion].equals("")) {
                    if (t != null) t.cancel();
                    t = Toast.makeText(this.getBaseContext(), "Correct answer", Toast.LENGTH_SHORT);
                    t.show();
                }
                else if (!answeredbyme[currentQuestion].equals(corrects.get(currentQuestion)) && !answeredbyme[currentQuestion].equals("")) {
                    if (t != null) t.cancel();
                    t = Toast.makeText(this.getBaseContext(), "Wrong answer", Toast.LENGTH_SHORT);
                    t.show();
                }
            }


            currentQuestion++;
            if (currentQuestion < totalQuestions) {
                while (true) {
                    if (!answeredbyme[currentQuestion].equals("")) {
                        if (currentQuestion < totalQuestions) currentQuestion++;
                        else currentQuestion = 0;
                    } else break;
                }
            } else{
                currentQuestion = 0;
                while (true) {
                    if (!answeredbyme[currentQuestion].equals("")) {
                        if (currentQuestion < totalQuestions) currentQuestion++;
                        else currentQuestion = 0;
                    } else break;
                }
            }

            updateUI(currentQuestion);
        }
        else {
            intent.putExtra("answers",answeredbyme);
            intent.putStringArrayListExtra("corrects", corrects);
            intent.putExtra("millis",millisToFinish);
            intent.putExtra("total",totalseconds);
            intent.putExtra("pointgame",pointgame);
            intent.putExtra("feedback",feedback);
            intent.putExtra("nickname",myNickname);
            intent.putExtra("otp",myOTP);
            startActivity(intent);
            finish();
        }
    }

    //Checking to see if we have a tablet
    private boolean isTablet() {
        return (this.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    //Handling all the click events
    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.answerAbtn){

            answeredbyme[currentQuestion] = "A";
            changeQuestAndUpdate();
        }
        else if(view.getId() == R.id.answerBbtn){

            answeredbyme[currentQuestion] = "B";
            changeQuestAndUpdate();
        }
        else if(view.getId() == R.id.answerCbtn){

            answeredbyme[currentQuestion] = "C";
            changeQuestAndUpdate();
        }
        else if(view.getId() == R.id.answerDbtn){
            answeredbyme[currentQuestion] = "D";
            changeQuestAndUpdate();
        }
        else if(view.getId() == R.id.answerEbtn){
            answeredbyme[currentQuestion] = "E";
            changeQuestAndUpdate();
        }
        else if(view.getId() == R.id.nextQuestBtn){
            changeQuestAndUpdate();
        }
        else if(view.getId() == R.id.endQuizBtn){
            intent.putExtra("answers",answeredbyme);
            intent.putStringArrayListExtra("corrects", corrects);
            intent.putExtra("millis", millisToFinish);
            intent.putExtra("total",totalseconds);
            intent.putExtra("pointgame",pointgame);
            intent.putExtra("feedback",feedback);
            intent.putExtra("nickname",myNickname);
            intent.putExtra("otp",myOTP);
            startActivity(intent);
            finish();
        }

    }
}
