package gr.gkortsaridis.iquizpersonal;


import android.os.AsyncTask;

public class HttpAsyncTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... urls) {
        String temp = BasicHTTP.GET(urls[0], urls[1], urls[2]);
        return temp;
    }
}
