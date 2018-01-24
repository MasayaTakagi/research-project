/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author takagi masaya
 */
public class SingleGazeData {

    public int ID;
    public int matrix[] = new int[2];
    public LocalTime date;
    public String time;
    
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    public SingleGazeData(int ID, int matrix_x, int matrix_y, String time) {
        this.ID = ID;
        this.matrix[0] = matrix_x;
        this.matrix[1] = matrix_y;
        this.time = time;
        this.date = LocalTime.parse(time, dtf);
        
    }
    
    public int getID() {
        return this.ID;
    }

    public LocalTime getDate() {
        return this.date;
    }
    
    public String getTime() {
        return this.time;
    }
    
    public int[] getMatrix(){
        return this.matrix;
    }
}
