package edu.ahs.robotics.util;

import android.graphics.Bitmap;
import android.os.Environment;

import org.firstinspires.ftc.robotcore.internal.android.dx.util.Warning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Logger {

    /*
    INSTRUCTIONS FOR USING THE LOGGER:

    SETUP:
    None needed

    ADD DATA:
    In any file, use Logger.append(Logger.cats.(NAME),Data);
    Will add data to given field, not much more than that

    IMPORTANT: make sure that your data is converted to a string before appending
    you can do this in the append function, by using Integer.toString(int) or Double.toString(double)

    FINALIZE AND WRITE FILE:
    Use function logger.getinstance().stopWriting();
    Make sure to write this after your robot.executePlan();
    Otherwise, file will never be written and data is lost

    You can change names in logger class, default is "Data.csv"

    Directory will need to be: System.getProperty("user.dir"). Will put in working file (use when running mock tests)
    Directory will need to be: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
     (when actually running on robot and have phone plugged in)

    Make sure to save file or change name in between runs so that your file is not overwritten
    IF THIS IS NOT DONE DATA WILL BE LOST
     */
    private String fileName;
    private ArrayList<String> categories;
    private Map<String, ArrayList<String>> entriesByCategory;
    private int lastLine = 0;
    private static long startTime; //shared across all loggers for synchronization
    private boolean firstLine;

    static {
        startTime = System.currentTimeMillis();
    }

    public Logger(String fileName){
        this.fileName = fileName + ".csv";
        categories = new ArrayList<>();
        entriesByCategory = new HashMap<>();
    }

    private FileWriter csvWriter = null;

    public void startWriting() {
        firstLine = true;
        try {
            File file = new File(FTCUtilities.getLogDirectory(), fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            csvWriter = new FileWriter(file);
            csvWriter.flush();

         } catch (IOException e) {
            throw new Warning(e.getMessage());
        }
    }

    private void writeCats() throws IOException {
        csvWriter.append("Time, ");
        int i = 0;
        for(String cat : categories){ // write the categories first
           csvWriter.append(cat);
           if(i < categories.size() - 1){
               csvWriter.append(", ");
           }
           i++;
       }
        csvWriter.append("\n");
    }

    public void writeLine(){
        try {
            if(firstLine){
                writeCats();
                firstLine = false;
            }
            csvWriter.append(String.valueOf(System.currentTimeMillis() - startTime) + ", ");
            for (int i = 0; i < categories.size(); i++) {
                String category = categories.get(i);
                List<String> list = entriesByCategory.get(category);
                if (lastLine < list.size()) {
                    csvWriter.append(list.get(lastLine));
                } else {
                    csvWriter.append(" ");
                }
                if (i < categories.size() - 1) {
                    csvWriter.append(", ");
                }
            }
            csvWriter.append("\n");
            lastLine++;
            csvWriter.flush();
        } catch (Exception e){
            throw new Warning(e.getMessage());
        }
    }

    public void stopWriting() {
        try{
            csvWriter.close();
        } catch (IOException e) {
            throw new Warning(e.getMessage());
        }
    }



    public void append(String category, String data) {
        ArrayList<String> dataList = entriesByCategory.get(category);
        if (dataList == null) {
            dataList = new ArrayList<>();
            categories.add(category);
            entriesByCategory.put(category,dataList);
        }
        dataList.add(data);
    }

    /**
     * Saves a bitmap to the phone. Sets the file name using time & date information.
     * Saves can be found in the downloads folder of the phone.
     */
    public static void saveImage(Bitmap bitmap){
        Calendar now = Calendar.getInstance();
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        String fileName = "BotImg_"+ now.get(Calendar.DAY_OF_MONTH) + "_" + now.get(Calendar.HOUR_OF_DAY) + "_" + now.get(Calendar.MINUTE) + "_" + now.get(Calendar.SECOND) + now.get(Calendar.MILLISECOND) +".jpg";
        File img = new File(filePath, fileName);
        if (img.exists())
            img.delete();
        try {
            FileOutputStream out = new FileOutputStream(img);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();


        } catch (Exception e) {
            throw new Warning(e.getMessage());
        }

    }
}
