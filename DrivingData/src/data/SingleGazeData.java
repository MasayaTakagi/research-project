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
    public int matrix[];
    public LocalDateTime date;

    public SingleGazeData(int ID, int matrix_x, int matrix_y, String date) {
        this.ID = ID;
        this.matrix[0] = matrix_x;
        this.matrix[1] = matrix_y;
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.date = LocalDateTime.parse(date, dtf);
    }
    
    public int getID() {
        return this.ID;
    }

    public LocalDateTime getDate() {
        return this.date;
    }
    
    public LocalTime getTime() {
        return this.date.toLocalTime();
    }
    
    public int[] getMatrix(){
        return this.matrix;
    }
}
