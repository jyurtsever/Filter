# Filter
Signal Processing for Turbulence Data

author: Joshua Yurtsever

This program is intended to process terbulance measurements from two different devices.
It accounts for the offset in time that these instruments start recording and
allows the user to choose which interval they want to examine.

To run the program, first download the jar file in this folder. 
Put the csv file you are using as data in the same folder as this jar file.
Then go to the directory that both files are in and run the following command: 

java -jar Filter.jar your-data-file-name.csv

The program defaults to using columns 3,4,5 and 15,16,17 as the x,y,z data for each device.
If you wish to change the columns, simply enter the columns you wish to use when you run the 
program in the following manner (as integers).

java -jar Filter.jar your-data-file-name.csv x1 y1 z1 x2 y2 z2 

The program will as you for the interval you wish to examine and output a graph of the processed
signals.
