package com.jhu.ads;

import java.util.ArrayList;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.ui.RectangleInsets;

public class Cost {
    
    /* Common */
    public static long COMMON_USERS_PER_INSTANCE = 1800;
    public static long UNCOMMON_USERS_PER_INSTANCE = 600;
    public static long MONTHS_IN_AN_YEAR = 12;
    public static long DAYS_IN_MONTH = 30;
    
    /* Build your own */
    public static long BUILD_COST_PER_INSTANCE = 3000; /* In dollars including electricity for UPGRADE_YEARS years */
    public static long UPGRADE_YEARS = 5;
    public static long COST_FOR_1_5_Mbps = 5; /* dollars per user per month */
    public static double commonUncommonRatio = 0.5;
    
    /* Rent */
    public static long RENT_COST_PER_INSTANCE = 860; /* In dollars per year, for medium utilization */
    
    // public static long MIN_CONCURRENT_USERS = 200000;
    public static long MAX_CONCURRENT_USERS = 10000000;
    public static long AVG_HOURS_PER_USER = 80; /* Per month */
    public static long TOTAL_USERS = 30000000;
    public static double BANDWIDTH_PER_USER = 1.5; /* Mbps */
    public static double COST_PER_GIGABYTE_TRANSFER = 0.04;
    public static long SCALE_USERS_BY = 1000000;
    
    public static ArrayList<Double> percentageList = new ArrayList<Double>();
    
    static {
        percentageList.add(0.80);
        percentageList.add(0.70);
        percentageList.add(0.67);
        percentageList.add(0.30);
        percentageList.add(0.25);
        percentageList.add(0.21);
        percentageList.add(0.17);
        percentageList.add(0.125);
        percentageList.add(0.1);
        percentageList.add(0.05);
    }
        
    public long computeBuildCost(long numUsers) {
        
        /* Infrastructure Cost */
        long commonMovieInstances = (long)((numUsers*commonUncommonRatio)/COMMON_USERS_PER_INSTANCE);
        long uncommonMovieInstances = (long)((numUsers*(1-commonUncommonRatio))/UNCOMMON_USERS_PER_INSTANCE);
        long totalInstances = commonMovieInstances + uncommonMovieInstances;
        long infrastructureCost = totalInstances * BUILD_COST_PER_INSTANCE;
        long infrastructureCostPerYear = infrastructureCost/UPGRADE_YEARS; /* Effective Per year cost */
        
        /* Network Cost */
        long networkCostPerYear = numUsers * COST_FOR_1_5_Mbps * MONTHS_IN_AN_YEAR;
        System.out.println("infrastructureCostPerYear = " + infrastructureCostPerYear + " networkCostPerYear = " + networkCostPerYear);
        return (infrastructureCostPerYear + networkCostPerYear);   
    }
    
    public long computeRentCost(long numBuildUsers) {
        
        long numRentUsers = MAX_CONCURRENT_USERS - numBuildUsers;
        
        /* Effective Infrastructure Cost */
        long commonMovieInstances = (long)((numRentUsers*commonUncommonRatio)/COMMON_USERS_PER_INSTANCE);
        long uncommonMovieInstances = (long)((numRentUsers*(1-commonUncommonRatio))/UNCOMMON_USERS_PER_INSTANCE);
        long effInfrastructureCostPerYear = ((commonMovieInstances + uncommonMovieInstances) * RENT_COST_PER_INSTANCE);
        
        /* Network Cost */
        long totalDataNeededPerScaleUsers = (long) ((24 * 3600 * BANDWIDTH_PER_USER * SCALE_USERS_BY)/(8*1024)); /* For one day */
        long moreUsers = numRentUsers;
        int startIndex = (int)numBuildUsers/1000000;
        int idx = startIndex;
        long totalDataPerDay = 0;
                
        while(moreUsers > 0) {
            totalDataPerDay += (totalDataNeededPerScaleUsers * percentageList.get(idx));
            moreUsers -= SCALE_USERS_BY;
            if(moreUsers % 1000000 == 0) { /* TODO: Cannot scale if 1000000 is not divisible by SCALE_USERS_BY */ 
                idx++;
            }
        }
        
        // System.out.println("totalDataNeededPerMonth= " + totalDataNeededPerMonth + " rentDataNeededPerMonth = " + rentDataNeededPerMonth);
        
        long costRentData = (long)(totalDataPerDay * DAYS_IN_MONTH * COST_PER_GIGABYTE_TRANSFER);
        long costRentDataPerYear = costRentData * MONTHS_IN_AN_YEAR;
        
        System.out.println("EffInfrastructureCostPerYear = " + effInfrastructureCostPerYear + " networkCostPerYear = " + costRentDataPerYear);
        return (effInfrastructureCostPerYear + costRentDataPerYear);
    }
    
    public static void main(String[] args) {
        Cost c = new Cost();
        
        CategoryTableXYDataset dataset = new CategoryTableXYDataset();
        
        for (long numUsers = 0; numUsers <= MAX_CONCURRENT_USERS; numUsers+=SCALE_USERS_BY) {
            System.out.println("[ BuildUsers: " + numUsers + ", RentUsers: " + (MAX_CONCURRENT_USERS - numUsers) +"] ");
            long buildCost = c.computeBuildCost(numUsers);
            long rentCost = c.computeRentCost(numUsers);
            System.out.println("BuildCost = " + buildCost/1000000
                                        + " RentCost = " + rentCost/1000000 
                                        + " TotalCost = " + ( (double)buildCost/1000000 + (double)rentCost/1000000) );
            
            dataset.add(numUsers/SCALE_USERS_BY, buildCost/1000000, "Build");
            dataset.add(numUsers/SCALE_USERS_BY, rentCost/1000000, "Rent");
            
            System.out.println();
        }
    
    
        XYPlot plot = new XYPlot(dataset, new NumberAxis("Users Served by Build Data Center(in Million)  TotalUsers="+MAX_CONCURRENT_USERS/1000000+" million"), new NumberAxis(
                "Total Cost(in Million)"), new StackedXYBarRenderer());
        
//        RectangleInsets offset = new RectangleInsets(0, 0, 0, 0);
//        plot.setAxisOffset(offset );
        
        JFreeChart chart = new JFreeChart(plot);
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("Cost Analysis");
        frame.setContentPane(chartPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
    

}
