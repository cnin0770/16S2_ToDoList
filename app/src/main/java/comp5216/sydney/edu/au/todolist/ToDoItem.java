package comp5216.sydney.edu.au.todolist;

import com.google.common.base.Strings;
import com.orm.SugarRecord;

public class ToDoItem extends SugarRecord {
    public String todo;

    public ToDoItem(){}

    public ToDoItem(String ToDo){
        this.todo = ToDo;
    }
}
