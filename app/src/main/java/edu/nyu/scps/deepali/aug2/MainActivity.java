package edu.nyu.scps.deepali.aug2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String OBJECT_TYPE = "Grocery";
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Parse.enableLocalDatastore(this);
        Parse.initialize(
                this,
                "LlW65BQG9SbRc3xKWfOyOCquCnWicE54BqhBhnQS", //app id from parse
                "ewcwVerosc83WXnD4qyAJ5xxxFm4kWZY1as8xuJC"  //client key from parse
        );

        listView = (ListView)findViewById(R.id.listView);
        TextView textView = (TextView)findViewById(R.id.empty);
        listView.setEmptyView(textView);   //Display this TextView when table has no contains.

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.title_update_or_delete);
                GroceryAdapter groceryAdapter = (GroceryAdapter)listView.getAdapter();
                final ParseObject parseObject = (ParseObject)groceryAdapter.getItem(position);
                String message = parseObject.getString("name") + "\n"
                        + parseObject.getObjectId() + "\n"
                        + parseObject.getCreatedAt() + "\n"
                        + parseObject.getUpdatedAt();
                builder.setMessage(message);

                //The dialog has three buttons:

                builder.setNeutralButton(R.string.button_cancel, null); //Make dialog disappear without doing anything.

                builder.setNegativeButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RefreshTask refreshTask = new RefreshTask() {
                            @Override
                            protected List<ParseObject> doInBackground(Void... params) {
                                try {
                                    parseObject.delete();
                                } catch (ParseException parseException) {
                                    Log.e("myTag", "delete", parseException);
                                }
                                return super.doInBackground(); //Return an up-to-date List<ParseObject>.
                            }
                        };

                        refreshTask.execute();
                    }
                });

                builder.setPositiveButton(R.string.button_update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.title_update);
                        GroceryAdapter groceryAdapter = (GroceryAdapter)listView.getAdapter();
                        final ParseObject parseObject = (ParseObject)groceryAdapter.getItem(position);
                        String message = parseObject.getString("name") + "\n"
                                + parseObject.getObjectId() + "\n"
                                + parseObject.getCreatedAt() + "\n"
                                + parseObject.getUpdatedAt();
                        builder.setMessage(message);

                        LayoutInflater inflater = getLayoutInflater();
                        View view = inflater.inflate(R.layout.edit, null);
                        builder.setView(view);

                        builder.setNegativeButton(R.string.button_cancel, null);
                        final AlertDialog alertDialog = builder.create();
                        EditText editText = (EditText) view.findViewById(R.id.editText);

                        editText.setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                if (event.getAction() == KeyEvent.ACTION_DOWN
                                        && keyCode == KeyEvent.KEYCODE_ENTER) {
                                    EditText editText = (EditText)v;
                                    Editable editable = editText.getText();
                                    String name = editable.toString();
                                    parseObject.put("name", name);

                                    RefreshTask refreshTask = new RefreshTask() {
                                        @Override
                                        protected List<ParseObject> doInBackground(Void... params) {
                                            try {
                                                parseObject.save();
                                            } catch (ParseException parseException) {
                                                Log.e("myTag", "save", parseException);
                                            }
                                            return super.doInBackground(); //Return an up-to-date List<ParseObject>.
                                        }
                                    };

                                    refreshTask.execute();
                                    alertDialog.dismiss(); //Must dismiss manually, because dialog has no OK button.
                                    return true;
                                }
                                return false;
                            }
                        });
                        alertDialog.show();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //Create an up-to-date PersonAdapter and plug it into the ListView.
        RefreshTask refreshTask = new RefreshTask();
        refreshTask.execute();

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

        if (id == R.id.action_append) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_new);
            builder.setMessage(R.string.message_new);

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.edit, null);
            builder.setView(view);

            builder.setNegativeButton(R.string.button_cancel, null);
            final AlertDialog alertDialog = builder.create();
            EditText editText = (EditText) view.findViewById(R.id.editText);

            editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN
                            && keyCode == KeyEvent.KEYCODE_ENTER) {

                        EditText editText = (EditText) v;
                        Editable editable = editText.getText();
                        final String name = editable.toString();

                        RefreshTask refreshTask = new RefreshTask() {
                            @Override
                            protected List<ParseObject> doInBackground(Void... params) {
                                ParseObject parseObject = new ParseObject(OBJECT_TYPE);
                                parseObject.put("name", name);
                                try {
                                    parseObject.save();
                                } catch (ParseException parseException) {
                                    Log.e("myTag", "save", parseException);
                                }

                                return super.doInBackground(); //Return an up-to-date List<ParseObject>.
                            }
                        };

                        refreshTask.execute();
                        alertDialog.dismiss(); //Must dismiss manually, because dialog has no OK button.
                        return true;
                    }
                    return false;
                }
            });

            alertDialog.show();
            return true;
        }

        if (id == R.id.action_delete_all) {
            RefreshTask refreshTask = new RefreshTask() {
                @Override
                protected List<ParseObject> doInBackground(Void... params) {
                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(OBJECT_TYPE);
                    try {
                        List<ParseObject> list = query.find();
                        if (list != null) {
                            ParseObject.deleteAll(list);
                        }
                    } catch (ParseException parseException) {
                        Log.e("myTag", "deleteAll", parseException);
                    }

                    return super.doInBackground(); //Return an up-to-date List<ParseObject>.
                }
            };

            refreshTask.execute();
            return true;
        }

        if (id == R.id.action_reset) {
            RefreshTask refreshTask = new RefreshTask() {
                @Override
                protected List<ParseObject> doInBackground(Void... params) {
                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(OBJECT_TYPE);
                    List<ParseObject> list = null;
                    try {
                        list = query.find();
                    } catch (ParseException parseException) {
                        Log.e("myTag", "find", parseException);
                    }
                    if (list != null) {
                        ParseObject.deleteAllInBackground(list);
                    }

                    String[] names = {
                            "Milk",
                            "Bread",
                            "Sugar",
                            "Salt"
                    };

                    for (int i = 0; i < names.length; ++i) {
                        ParseObject parseObject = new ParseObject(OBJECT_TYPE);
                        parseObject.put("name", names[i]);
                        try {
                            parseObject.save();
                        } catch (ParseException parseException) {
                            Log.e("myTag", "save", parseException);
                        }
                    }

                    return super.doInBackground(); //Return an up-to-date List<ParseObject>.
                }
            };

            refreshTask.execute();
            return true;
        }
        if (id == R.id.action_refresh) {
            RefreshTask refreshTask = new RefreshTask();
            refreshTask.execute();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Get an up-to-date list of all the rows in the table in the cloud.
    //(Do this in a second thread.)  When done, put the list into a new adapter
    //and display it in the ListView.  (Do this back in the UI thread.)

    private class RefreshTask extends AsyncTask<Void, Void, List<ParseObject>> {
        private Dialog progressDialog;

        //Get all the persons in the table.
        protected List<ParseObject> doInBackground(Void... params) {
            ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>(OBJECT_TYPE);
            parseQuery.orderByAscending("updatedAt");
            try {
                return parseQuery.find();
            } catch (ParseException parseException) {
                Log.e("myTag", "find", parseException);
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "", "Loading...", true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<ParseObject> parseObjects) {
            if (parseObjects != null) {
                GroceryAdapter groceryAdapter = new GroceryAdapter(MainActivity.this, parseObjects);
                listView.setAdapter(groceryAdapter);
            }
            progressDialog.dismiss();
        }
    }
}