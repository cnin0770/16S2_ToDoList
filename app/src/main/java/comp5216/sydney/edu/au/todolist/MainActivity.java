package comp5216.sydney.edu.au.todolist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.text.AlteredCharSequence;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

public class MainActivity extends Activity {

        //define variables
        ListView listview;
        ArrayList<String> items;
        ArrayAdapter<String> itemsAdapter;
        EditText addItemEditText;
        public final int EDIT_ITEM_REQUEST_CODE = 647;


//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        //use "activity_main.xml" as the layout
//        setContentView(R.layout.activity_main);
//        //reference the "listview" variable to the id-"listview" in the layout
//        listview = (ListView) findViewById(R.id.listView);
//        addItemEditText = (EditText) findViewById(R.id.txtNewItem);
//        //create an ArrayList of String
//        items = new ArrayList<String>();
//        items.add("item one");
//        items.add("item two");
//        //Create an adapter for the list view using Android's built-in item layout
//        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
//        //connect the listview and the adapter
//        listview.setAdapter(itemsAdapter);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listview = (ListView) findViewById(R.id.listView);
        addItemEditText = (EditText) findViewById(R.id.txtNewItem);
        items = new ArrayList<String>();
        items.add("item one");
        items.add("item two");

        readItemsFromDatabase();

        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listview.setAdapter(itemsAdapter);
        setupListViewListener();
    }

    public void onAddItemClick(View view) {
        String toAddString = addItemEditText.getText().toString();
        if (toAddString != null && toAddString.length() > 0) {
            itemsAdapter.add(toAddString);
            addItemEditText.setText("");

            saveItemsToDatabase();

        }
    }

    private void setupListViewListener(){

        listview.setOnItemLongClickListener (new AdapterView.OnItemLongClickListener(){
            public boolean onItemLongClick (AdapterView <?> parent, View view,final int position,long rowId) {
                Log.i("MainActivity", "Long Clicked item " + position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_delete_title)
                        .setMessage(R.string.dialog_delete_msg)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                // deleting
                                items.remove(position);
                                itemsAdapter.notifyDataSetChanged();

                                saveItemsToDatabase();

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                //usr cancelled the dialog, non deleting
                            }
                        });
                builder.create().show();
                return true;
            }
        });

        listview.setOnItemClickListener (new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick (AdapterView <?> parent, View view, int position, long id) {
                String updateItem = (String) itemsAdapter.getItem(position);
                Log.i("MainActivity", "Clicked item " + position + ": " + updateItem);

                Intent intent = new Intent(MainActivity.this, EditToDoItemActivity.class);
                if (intent != null){
                    // in case of "extras"
                    intent.putExtra("item", updateItem);
                    intent.putExtra("position", position);
                    // brings up the 2nd activity
                    startActivityForResult(intent, EDIT_ITEM_REQUEST_CODE);
                    itemsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == EDIT_ITEM_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                //extract name value from result extras
                String editedItem = data.getExtras().getString("item");
                int position = data.getIntExtra("position", -1);
                items.set(position, editedItem);
                Log.i("Updated Item in list:", editedItem + ",position:" + position);
                Toast.makeText(this, "updated:" + editedItem, Toast.LENGTH_SHORT).show();
                itemsAdapter.notifyDataSetChanged();

                saveItemsToDatabase();

            }
        }
    }

    private void readItemsFromFile(){
        // retrieve app's private folder, being not accessible by other apps
        File filesDir = getFilesDir();
        // prepare a file to read the date
        File todoFile = new File (filesDir, "todo.txt");
        // if file does not exist, create one
        if (!todoFile.exists()){
            items = new ArrayList<String>();
        } else {
            try {
                // read and put it into ArrayList
                items = new ArrayList<String>(FileUtils.readLines(todoFile));
            }
            catch (IOException ex) {
                items = new ArrayList<String>();
            }
        }
    }

    private void saveItemsToFile(){
        File filesDir = getFilesDir();
        // using the dame file for reading. should use define a blobal string instead
        File todoFile = new File(filesDir, "todo.txt");
        try{
            //write list to file
            FileUtils.writeLines(todoFile, items);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void readItemsFromDatabase(){
        // read items from database
        List<ToDoItem> itemsFromORM = ToDoItem.listAll(ToDoItem.class);
        items = new ArrayList<String>();
        if (itemsFromORM != null & itemsFromORM.size() > 0) {
            for (ToDoItem item : itemsFromORM) {
                items.add(item.todo);
            }
        }
    }

    private void saveItemsToDatabase(){
        ToDoItem.deleteAll(ToDoItem.class);
        for (String todo: items){
            ToDoItem item = new ToDoItem(todo);
            item.save();
        }
    }
}